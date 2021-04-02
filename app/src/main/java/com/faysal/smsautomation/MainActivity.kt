package com.faysal.smsautomation

import android.Manifest
import android.Manifest.permission.*
import android.R
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.View
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
import com.faysal.smsautomation.adapters.DeliveredMessageAdapter
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
    private val PERMISSION_REQUEST_CODE = 200
    lateinit var binding: ActivityMainBinding
    private var isPermissionGranted = false
    lateinit var apiService: ApiService
    lateinit var api2Service: ApiService

    lateinit var smsViewModel: SMSViewModel

    lateinit var listAdapter: DeliveredMessageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDependency()
        providePermission()
        setUpViewsWithData()


        //sendSMS("123423532","Hello World")


    }

    fun sendSMS(phoneNo: String, msg: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNo, null, msg, null, null)
            Toast.makeText(
                applicationContext, "Message Sent",
                Toast.LENGTH_LONG
            ).show()
        } catch (ex: Exception) {
            Toast.makeText(
                applicationContext, ex.message.toString(),
                Toast.LENGTH_LONG
            ).show()
            ex.printStackTrace()
        }
    }

    private fun deleteSMS(): Boolean {
        var isDeleted = false
        isDeleted = try {
            contentResolver.delete(Uri.parse("content://sms/"), null, null)
            true
        } catch (ex: java.lang.Exception) {
            false
        }
        return isDeleted
    }

    private fun initDependency() {
        apiService = NetworkBuilder.getApiService()
        api2Service = NetworkBuilder.getAnotherApiService()
        smsViewModel = ViewModelProviders.of(this).get(SMSViewModel::class.java)
        listAdapter = DeliveredMessageAdapter()

        handelIsDefaultApp()

    }




    private fun setUpViewsWithData() {
        setupSimInfo()
        setUpNetworkViews()
        setUpForButtonClickHandeler()
        setUpForLastsActivites()

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


        binding.btnSave.setOnClickListener {
            saveOperation()
        }

        binding.btnReset.setOnClickListener {

            binding.etDomainName.text.clear()
            binding.etVerificationCode.text.clear()

            SharedPref.clearSharedPreferences(this)
        }

        binding.btnStart.setOnClickListener {

            val service = SharedPref.getBoolean(this, Constants.BACKGROUND_SERVVICE)

            if (!service) {
                if (isAbaleToStartService) {
                    Util.showAlertMessage(binding.root, "Background service started successfully.")
                    SharedPref.putBoolean(this, Constants.BACKGROUND_SERVVICE, true)
                    binding.btnStart.apply {
                        text = "Stop"
                        setBackgroundColor(Color.parseColor("#E53935"))
                    }

                } else {
                    showErrorMessageOKCancel("Press the save button before start background service")
                }
            } else {
                SharedPref.putBoolean(this, Constants.BACKGROUND_SERVVICE, false)
                Util.showAlertMessage(binding.root, "Background Service has been stopped ")
                binding.btnStart.apply {
                    text = "Start"
                    setBackgroundColor(Color.parseColor("#20AD26"))
                }

            }


        }
    }

    private fun saveOperation() {
        if (!Util.isOnline(this)) {
            showErrorMessageOKCancel("Internet connection failed ! ")
            return
        }

        val loading: AlertDialog =
            SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        val domainName = binding.etDomainName.text.toString().trim()
        val verificationCode = binding.etVerificationCode.text.toString().trim()
        val service = binding.tvService.selectedItem?.toString()
        val interval = binding.tvInterval.text.toString().trim()

        var domStatus = false
        var vCodeStatus = false

        if (domainName.isNullOrEmpty()) {
            Util.showAlertMessage(
                binding.root,
                "Please enter domain name to save and start service."
            )
            return
        }

        if (verificationCode.isNullOrEmpty()) {
            Util.showAlertMessage(binding.root, "Please enter verification code.")
            return
        }

        if (service.isNullOrEmpty()) {
            Util.showAlertMessage(
                binding.root,
                "Invalid service amount"
            )
            return
        }

        if (interval.isNullOrEmpty()) {
            Util.showAlertMessage(binding.root, "Invalid interval amount")
            return
        }


        runBlocking {
            supervisorScope {
                try {
                    val responseDomain = async { api2Service.getDomainInfo(domainName) }.await()
                    val responseVerificationCode =
                        async { apiService.getVerificationCodeInfo(verificationCode) }.await()

                    if (responseDomain.isSuccessful) {
                        if (responseDomain.body()?.status == "1") {
                            domStatus = true
                        }
                    }

                    if (responseVerificationCode.isSuccessful) {
                        if (responseVerificationCode.body()?.status == 1) {
                            vCodeStatus = true
                        }
                    }

                } catch (t: Throwable) {
                    domStatus = false

                }
            }

        }


        if (domStatus == false) {
            showErrorMessageOKCancel("This domain not exist in Database.")
            // Util.showAlertMessage(binding.root,"This domain not exist in Database.")
            return
        }


        if (vCodeStatus == false) {
            showErrorMessageOKCancel("Verification code invalid .")
            return
        }


        SharedPref.putString(this, Constants.SHARED_DOMAIN_NAME, domainName)
        SharedPref.putString(this, Constants.SHARED_INTERVAL, interval)
        SharedPref.putString(this, Constants.SHARED_SERVICE, service)
        SharedPref.putString(this, Constants.SHARED_VERIFICATION_CODE, verificationCode)
        SharedPref.putBoolean(this, Constants.SHARED_SIM_1_ACTIVE, true)
        SharedPref.putBoolean(this, Constants.SHARED_SIM_2_ACTIVE, true)


        isAbaleToStartService = true

        Util.showAlertMessage(binding.root, "All the form information saved successfully")


        /*  if (!domainName.isUrlValid()){
              Util.showAlertMessage(binding.root,"Invalid domain name.")
          }*/


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
                        Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
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

    private fun providePermission() {

        if (!checkPermission()) {
            requestPermission();
        } else {
            isPermissionGranted = true
        }
    }

    private fun checkPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_SMS)
        val result1 =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.SEND_SMS)
        val result2 =
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_PHONE_STATE
            )
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(READ_SMS, SEND_SMS, READ_PHONE_STATE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                val readSmsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val sendSmsAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val phoneStateAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED
                if (readSmsAccepted && sendSmsAccepted && phoneStateAccepted)
                    isPermissionGranted = true
                else {
                    Snackbar.make(
                        binding.root,
                        "Permission Denied, You cannot access this app.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            arrayOf(READ_SMS, SEND_SMS),
                                            PERMISSION_REQUEST_CODE
                                        )
                                    }
                                })
                            return
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showErrorMessageOKCancel(message: String) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()

    }

    fun handelIsDefaultApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Telephony.Sms.getDefaultSmsPackage(this) != packageName) {

                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivity(intent)


                Toast.makeText(this, "Change default sms app", Toast.LENGTH_SHORT).show()
            } else {

            }
        }
    }





}