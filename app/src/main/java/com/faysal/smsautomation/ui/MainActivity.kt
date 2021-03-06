package com.faysal.smsautomation.ui

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.faysal.smsautomation.Models.*
import com.faysal.smsautomation.adapters.DeliveredMessageAdapter
import com.faysal.smsautomation.database.Activites
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.databinding.ActivityMainBinding
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.services.HandlerSMSWork
import com.faysal.smsautomation.services.WorkMessageSender
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import com.faysal.smsautomation.util.Util
import com.faysal.smsautomation.viewmodel.SMSViewModel
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    private val JOB_GROUP_NAME = "handel_sms_work"

    private var isAbaleToStartService: Boolean = false
    lateinit var binding: ActivityMainBinding
    lateinit var apiService: ApiService
    lateinit var api2Service: ApiService

    lateinit var smsViewModel: SMSViewModel

    lateinit var listAdapter: DeliveredMessageAdapter

    lateinit var smsDao: PhoneSmsDao

    lateinit var handler: Handler
    var apiDelayed : Long = 6 * 1000 //1 second=1000 milisecond, 5*1000=5seconds
    lateinit var runnable: Runnable


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

        smsDao = SmsDatabase.getInstance(this).phoneSmsDao()
        apiService = NetworkBuilder.getApiService(applicationContext)
        api2Service = NetworkBuilder.getAnotherApiService()
        smsViewModel = ViewModelProviders.of(this).get(SMSViewModel::class.java)
        listAdapter = DeliveredMessageAdapter()

        binding.tvNotice.isSelected = true

    }


    private fun setUpViewsWithData() {
        setupSimInfo()
        setUpNetworkViews()
        setUpForButtonClickHandeler()
        setUpForLastsActivites()
        setUpForSimInfoView()
        setupSmsServerOperation()


    }

    override fun onResume() {
        super.onResume()
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable { //do your function;
            val background_service = SharedPref.getBoolean(this, Constants.BACKGROUND_SERVICE)
            val verifyCode = Integer.valueOf(
                SharedPref.getString(
                    applicationContext,
                    Constants.SHARED_VERIFICATION_CODE
                )
            )
            if (background_service){
                setupNetworkCallForOutgoing(verifyCode)

                saveActivites(
                    Activites(
                        message = "Outgoing api triggered",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                        status = true
                    )
                )

            }

            handler.postDelayed(runnable, apiDelayed.toLong())
        }.also { runnable = it },
            apiDelayed.toLong()
        )
    }



    private fun saveActivites(activites: Activites) {
        val daos = SmsDatabase.getInstance(this).phoneSmsDao()
        GlobalScope.launch {
            try {
                daos.saveDeliveredMessage(activites)
            } catch (e: Exception) {
                Log.d(TAG, "Failed  " + e.message)
            }
        }
    }



    private fun setupNetworkCallForOutgoing(verifyCode: Int) {

        apiService.getOutgoingMessages(verifyCode).enqueue(object : Callback<OutgoingMessages> {
            override fun onResponse(
                call: Call<OutgoingMessages>,
                response: Response<OutgoingMessages>
            ) {
                val responseBody = response?.body()
                responseBody?.forEach {
                    sendOutgoingSms(it.url)
                }
            }

            override fun onFailure(call: Call<OutgoingMessages>, t: Throwable) {
                saveActivites(
                    Activites(
                        message = "Error ! Something happened in outgoing api",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                        status = true
                    )
                )
            }
        })
    }

    private fun sendOutgoingSms(reciverInfo: String) {

        val interval = SharedPref.getlong(applicationContext, Constants.SHARED_INTERVAL)
        val datas = Data.Builder().apply {
            putString("messageUrl", reciverInfo)
        }.build()


        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(WorkMessageSender::class.java)
                .setInitialDelay(interval, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setInputData(datas)
                .build()

        val workManager: WorkManager = WorkManager.getInstance(applicationContext);
        var work: WorkContinuation = workManager.beginUniqueWork(
            JOB_GROUP_NAME,
            ExistingWorkPolicy.APPEND,
            workRequest
        );
        work.enqueue();
    }

    private fun setupSmsServerOperation() {
        smsDao.getAllSmsByLiveData().observe(this, Observer { it ->
            it.forEach { sms ->
               sendSmsToBackgroundService(sms)
            }
        })
    }

    private fun sendSmsToBackgroundService(sms: PhoneSms) {

        val interval = SharedPref.getlong(this, Constants.SHARED_INTERVAL)


        val datas = Data.Builder().apply {
            putInt("smsid", sms.smsid)
            putString("simNo", sms.receiver_phone)
            putString("sender", sms.sender_phone)
            putString("datetime", sms.timestamp)
            putString("smsBody", sms.body)
        }.build()

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(HandlerSMSWork::class.java)
            .setInitialDelay(interval, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .setInputData(datas)
            .build()

        val workManager: WorkManager = WorkManager.getInstance(this);
        var work: WorkContinuation = workManager.beginUniqueWork(
            JOB_GROUP_NAME,
            ExistingWorkPolicy.APPEND,
            workRequest
        );
        work.enqueue();

        val smsNew = sms.apply {
            processRunning = true
        }

        updateSms(smsNew)

    }

    private fun updateSms(sms: PhoneSms) {
        val daos = SmsDatabase.getInstance(this).phoneSmsDao()
        GlobalScope.launch {
            try {
                daos.update(sms)
                Log.d(TAG, "SMS update successfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to update data into room")
            }
        }
    }


    private fun setUpForSimInfoView() {

        val sim1number = SharedPref.getString(this, Constants.SHARED_SIM_1_NUMBER)
        val sim2number = SharedPref.getString(this, Constants.SHARED_SIM_2_NUMBER)

        val sim1Switch = SharedPref.getBoolean(this, Constants.SHARED_SIM_1_ACTIVE)
        val sim2Switch = SharedPref.getBoolean(this, Constants.SHARED_SIM_2_ACTIVE)

        sim1number?.let {
            binding.etSim1.setText(sim1number)
        }

        sim2number?.let {
            binding.etSim2.setText(sim2number)
        }

        binding.switchSim1.isChecked = sim1Switch
        binding.switchSim2.isChecked = sim2Switch

    }


    private fun resetEverything() {

        GlobalScope.launch {
            try {
                smsDao.deleteEverything()
                Log.d(TAG, "SMS Delete everything successfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to delete all sms" + e.message)
            }
        }
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

        val service = SharedPref.getBoolean(this, Constants.BACKGROUND_SERVICE)

        if (service) {
            binding.btnStart.apply {
                text = "Stop"
                setBackgroundColor(Color.parseColor("#E53935"))
            }
            binding.tvStatus.text = "ON"
        } else {
            binding.btnStart.apply {
                text = "Start"
                setBackgroundColor(Color.parseColor("#20AD26"))
            }
            binding.tvStatus.text = "OFF"
        }



        binding.btnReset.setOnClickListener {
            resetEverything()
            SharedPref.clearSharedPreferences(this)
            startActivity(Intent(this, SplashScreen::class.java))
            finish()
        }

        binding.btnStart.setOnClickListener {

            startOperation()


        }
    }

    private fun startOperation() {
        val background_service = SharedPref.getBoolean(this, Constants.BACKGROUND_SERVICE)

        if (background_service) {
            SharedPref.putBoolean(this, Constants.BACKGROUND_SERVICE, false)
            Util.showAlertMessage(applicationContext, "Background Service has been stopped ")
            binding.btnStart.apply {
                text = "Start"
                setBackgroundColor(Color.parseColor("#20AD26"))
            }
            binding.tvStatus.text = "OFF"
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

        if (sim1number.isNullOrEmpty()) {
            binding.etSim1.error = "Enter sim 1 number"
            return
        }

        if (sim2number.isNullOrEmpty()) {
            binding.etSim2.error = "Enter sim 1 number"
            return
        }

        if (!sim1switch && !sim2switch) {
            Util.showAlertMessage(applicationContext, "Please enable a sim before start ")
            return
        }



        SharedPref.putlong(this, Constants.SHARED_INTERVAL, interval.toLong())
        SharedPref.putString(this, Constants.SHARED_SERVICE, service)


        SharedPref.putString(this, Constants.SHARED_SIM_1_NUMBER, sim1number)
        SharedPref.putString(this, Constants.SHARED_SIM_2_NUMBER, sim2number)

        SharedPref.putBoolean(this, Constants.SHARED_SIM_1_ACTIVE, sim1switch)
        SharedPref.putBoolean(this, Constants.SHARED_SIM_2_ACTIVE, sim2switch)

        Util.showSuccessMessage(applicationContext, "Background service started successfully.")
        SharedPref.putBoolean(this, Constants.BACKGROUND_SERVICE, true)

        binding.btnStart.apply {
            text = "Stop"
            setBackgroundColor(Color.parseColor("#E53935"))
        }
        binding.tvStatus.text = "ON"

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
                        Util.showAlertMessage(applicationContext, e.message.toString())
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
            if (!interval.isNullOrEmpty()){
                binding.tvInterval.text = interval
                SharedPref.putlong(this,Constants.SHARED_INTERVAL,interval.toLong())
            }
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
        Log.d(TAG, "onDestroy: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
        SharedPref.putBoolean(this, Constants.BACKGROUND_SERVICE, false)
        if (handler != null ){
            handler.removeCallbacks(runnable) //stop handler when activity not visible
        }
    }

   /* override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: $")
        SharedPref.putBoolean(this, Constants.BACKGROUND_SERVVICE, false)
        if (handler != null ){
            handler.removeCallbacks(runnable) //stop handler when activity not visible
        }
    }*/

    override fun onPause() {
        super.onPause()
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


}