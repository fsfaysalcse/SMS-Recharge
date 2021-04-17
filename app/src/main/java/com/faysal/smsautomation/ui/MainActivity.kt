package com.faysal.smsautomation.ui

import android.Manifest
import android.Manifest.permission.*
import android.R
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.faysal.smsautomation.Models.Company
import com.faysal.smsautomation.Models.Interval
import com.faysal.smsautomation.Models.Notice
import com.faysal.smsautomation.Models.Service
import com.faysal.smsautomation.util.Util
import com.faysal.smsautomation.adapters.DeliveredMessageAdapter
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.databinding.ActivityMainBinding
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import com.faysal.smsautomation.viewmodel.SMSViewModel
import com.google.android.material.snackbar.Snackbar
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.*
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    private var isAbaleToStartService: Boolean = false
    lateinit var binding: ActivityMainBinding
    lateinit var apiService: ApiService
    lateinit var api2Service: ApiService

    lateinit var smsViewModel: SMSViewModel

    lateinit var listAdapter: DeliveredMessageAdapter

    lateinit var smsDao: PhoneSmsDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDependency()
        setUpViewsWithData()


        //sendSMS("123423532","Hello World")


    }




    private fun initDependency() {
        apiService = NetworkBuilder.getApiService()
        api2Service = NetworkBuilder.getAnotherApiService()
        smsViewModel = ViewModelProviders.of(this).get(SMSViewModel::class.java)
        listAdapter = DeliveredMessageAdapter()

    }


    private fun setUpViewsWithData() {
        setupSimInfo()
        setUpNetworkViews()
        setUpForButtonClickHandeler()
        setUpForLastsActivites()
        setUpForSimInfoView()

    }

    private fun setUpForSimInfoView(){

        val sim1number = SharedPref.getString(this,Constants.SHARED_SIM_1_NUMBER)
        val sim2number = SharedPref.getString(this,Constants.SHARED_SIM_2_NUMBER)

        val sim1Switch = SharedPref.getBoolean(this,Constants.SHARED_SIM_1_ACTIVE)
        val sim2Switch = SharedPref.getBoolean(this,Constants.SHARED_SIM_2_ACTIVE)

        sim1number?.let {
            binding.etSim1.setText(sim1number)
        }

        sim2number?.let {
            binding.etSim2.setText(sim2number)
        }

        binding.switchSim1.isChecked = sim1Switch
        binding.switchSim2.isChecked = sim2Switch

    }


    private fun setUpForLastsActivites() {
        binding.activitesList.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
        }
        smsViewModel.alldeliverdSms.observe(this, Observer {
            listAdapter.setList(it)
        })
    }


    private fun setUpForButtonClickHandeler() {

        val service = SharedPref.getBoolean(this, Constants.BACKGROUND_SERVVICE)

        if (service) {
            binding.btnStart.apply {
                text = "Stop"
                setBackgroundColor(Color.parseColor("#E53935"))
            }
        } else {
            binding.btnStart.apply {
                text = "Start"
                setBackgroundColor(Color.parseColor("#20AD26"))
            }
        }



        binding.btnReset.setOnClickListener {
            SharedPref.clearSharedPreferences(this)
            startActivity(Intent(this, SplashScreen::class.java))
            finish()
        }

        binding.btnStart.setOnClickListener {

            startOperation()



        }
    }

    private fun startOperation() {
        val background_service = SharedPref.getBoolean(this, Constants.BACKGROUND_SERVVICE)

        if (background_service){
            SharedPref.putBoolean(this, Constants.BACKGROUND_SERVVICE, false)
            Util.showAlertMessage(applicationContext, "Background Service has been stopped ")
            binding.btnStart.apply {
                text = "Start"
                setBackgroundColor(Color.parseColor("#20AD26"))
            }
            return
        }


        if (!Util.isOnline(this)) {
            showErrorMessageOKCancel("Internet connection failed ! ")
            return
        }


        val service = binding.tvService.selectedItem.toString()
        val interval = binding.tvInterval.text.toString().trim()

        val sim1number = binding.etSim1.text.toString().trim()
        val sim2number = binding.etSim2.text.toString().trim()

        val sim1switch = binding.switchSim1.isChecked
        val sim2switch = binding.switchSim2.isChecked




        if (service.isNullOrEmpty()) {
            Util.showAlertMessage(
                applicationContext,
                "Invalid service !"
            )
            return
        }

        if (interval.isNullOrEmpty()) {
            Util.showAlertMessage(applicationContext, "Invalid interval !")
            return
        }

        if (sim1number.isNullOrEmpty()){
            binding.etSim1.error = "Enter sim 1 number"
            return
        }

        if (sim2number.isNullOrEmpty()){
            binding.etSim2.error = "Enter sim 1 number"
            return
        }

        if (!sim1switch && !sim2switch ){
            Util.showAlertMessage(applicationContext, "Please enable a sim before start ")
            return
        }



        SharedPref.putString(this, Constants.SHARED_INTERVAL, interval)
        SharedPref.putString(this, Constants.SHARED_SERVICE, service)


        SharedPref.putString(this, Constants.SHARED_SIM_1_NUMBER, sim1number)
        SharedPref.putString(this, Constants.SHARED_SIM_2_NUMBER, sim2number)

        SharedPref.putBoolean(this, Constants.SHARED_SIM_1_ACTIVE, sim1switch)
        SharedPref.putBoolean(this, Constants.SHARED_SIM_2_ACTIVE, sim2switch)

        Util.showSuccessMessage(applicationContext, "Background service started successfully.")
        SharedPref.putBoolean(this, Constants.BACKGROUND_SERVVICE, true)

        binding.btnStart.apply {
            text = "Stop"
            setBackgroundColor(Color.parseColor("#E53935"))
        }

    }


    private fun setUpNetworkViews() {
        if (Util.isOnline(this) == false) {
            showErrorMessageOKCancel("Internet connection failed ! ")
            return
        }

        var dialog: AlertDialog =
            SpotsDialog.Builder().setContext(this).setMessage("PLEASE WAIT").setCancelable(false)
                .build()
        dialog.show()

        lifecycleScope.launchWhenStarted {
            supervisorScope {
                try {
                    var intervalResponse = async { apiService.getInterval() }.await()
                    var companyResponse = async { apiService.getCompanyInfo() }.await()
                    var serviceResponse = async { api2Service.getServices() }.await()
                    var noticeResponse = async { api2Service.getNotice() }.await()


                    withContext(Dispatchers.Main) {
                        setupIntervalViews(intervalResponse)
                        setupCompanyInfoViews(companyResponse)
                        setupServiceViews(serviceResponse)
                        setupNoticeViews(noticeResponse)
                        dialog.dismiss()
                    }


                } catch (e: Throwable) {
                    withContext(Dispatchers.Main) {
                        Util.showAlertMessage(applicationContext,e.message.toString())
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun setupNoticeViews(response: Response<Notice>) {
        if (response.isSuccessful) {
            var notice = response.body()?.Response
            if (!notice.isNullOrEmpty()) binding.tvNotice.text = notice
        }
    }

    private fun setupServiceViews(response: Response<Service>) {
        if (response.isSuccessful) {
            var service = response.body()
            if (service != null) {
                val service_arrray: ArrayList<String> = ArrayList<String>()
                for (item in service) {
                    service_arrray.add(item.service)
                }
                binding.tvService.adapter = ArrayAdapter<String>(
                    this,
                    R.layout.simple_list_item_1,
                    service_arrray
                )
            }

        }
    }

    private fun setupCompanyInfoViews(response: Response<Company>) {
        if (response.isSuccessful) {
            var company_name = response.body()?.company_name
            var version_name = response.body()?.version
            if (!company_name.isNullOrEmpty()) binding.tvComapnyName.text = company_name
            if (!version_name.isNullOrEmpty()) binding.tvVersionCode.text = version_name
        }
    }

    private fun setupIntervalViews(response: Response<Interval>) {
        if (response.isSuccessful) {
            var interval = response.body()?.second
            if (!interval.isNullOrEmpty()) binding.tvInterval.text = interval
        }
    }

    private fun setupSimInfo() {
/*        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                val sManager =
                    getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val infoSim1 = sManager.getActiveSubscriptionInfoForSimSlotIndex(0)
                val infoSim2 = sManager.getActiveSubscriptionInfoForSimSlotIndex(1)

                val subscriptions: List<SubscriptionInfo> =
                    sManager.getActiveSubscriptionInfoList()


                for (subscriptionInfo in subscriptions) {
                    Log.d(TAG, subscriptionInfo.number)
                }


                if (infoSim1 != null) binding.tvSim1.text =
                    infoSim1.number else binding.sim1Layout.visibility = View.GONE
                if (infoSim2 != null) binding.tvSim2.text =
                    infoSim2.number else binding.sim2Layout.visibility = View.GONE
            }


        }*/
    }




    private fun showErrorMessageOKCancel(message: String) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        SharedPref.putBoolean(this,Constants.BACKGROUND_SERVVICE,false)
    }

    override fun onPause() {
        super.onPause()
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


}