package com.faysal.smsautomation

import android.Manifest
import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.faysal.smsautomation.Models.Company
import com.faysal.smsautomation.Models.Interval
import com.faysal.smsautomation.Models.Notice
import com.faysal.smsautomation.Models.Service
import com.faysal.smsautomation.databinding.ActivityMainBinding
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import com.google.android.material.snackbar.Snackbar
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.*
import me.everything.providers.android.telephony.Sms
import me.everything.providers.android.telephony.TelephonyProvider
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    private val PERMISSION_REQUEST_CODE = 200
    lateinit var binding: ActivityMainBinding
    private var isPermissionGranted = false
    lateinit var apiService: ApiService
    lateinit var api2Service: ApiService



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // initDependency()
       // providePermission()
      //  setUpViewsWithData()

        smsOperation()


    }

    private fun smsOperation(){
        val telephonyProvider = TelephonyProvider(applicationContext)
        val smses: List<Sms> = telephonyProvider.getSms(TelephonyProvider.Filter.ALL).getList()

        Log.d(TAG, "smsOperation: " + smses.size)
        deleteSMS()

        for (sms in smses){
           // Log.d(TAG, "smsOperation: "+sms.address +" ")
           // deleteSms(sms.id, sms.threadId)
        }

    }

    fun deleteSms(smsId: Long, thread_id: Int): Boolean {
        var isSmsDeleted = false
        isSmsDeleted = try {
            val thread = Uri.parse("content://sms")
            contentResolver.delete(
                thread, "thread_id=? and _id=?", arrayOf(
                    java.lang.String.valueOf(
                        thread_id
                    ), java.lang.String.valueOf(smsId)
                )
            )
            true
        } catch (ex: Exception) {
            false
        }
        return isSmsDeleted
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
    }

    private fun setUpViewsWithData() {
        setupSimInfo()
        setUpNetworkViews()
        setUpForButtonClickHandeler()

    }


    private fun setUpForButtonClickHandeler() {
        binding.btnSave.setOnClickListener {
            saveOperation()
        }
    }

    private fun saveOperation() {
        if (!Util.isOnline(this)){
            showErrorMessageOKCancel("Internet connection failed ! ")
            return
        }

        val loading: AlertDialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        val domainName = binding.etDomainName.text.toString().trim()
        val verificationCode = binding.etVerificationCode.text.toString().trim()
        val service = binding.tvService.text.toString().trim()
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
        SharedPref.putBoolean(this, Constants.SHARED_SIM_1_ACTIVE, true)
        SharedPref.putBoolean(this, Constants.SHARED_SIM_2_ACTIVE, true)
        SharedPref.putBoolean(this, Constants.BACKGROUND_SERVVICE, true)


        Util.showAlertMessage(binding.root, "Background service started successfully >>")

        /*  if (!domainName.isUrlValid()){
              Util.showAlertMessage(binding.root,"Invalid domain name.")
          }*/


    }


    private fun setUpNetworkViews() {
        if (Util.isOnline(this)==false){
            showErrorMessageOKCancel("Internet connection failed ! ")
            return
        }

        var dialog: AlertDialog =
            SpotsDialog.Builder().setContext(this).setMessage("PLEASE WAIT").setCancelable(false).build()
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
            var service = response.body()?.get(0)?.service
            if (!service.isNullOrEmpty()) binding.tvService.text = service
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
        if (ActivityCompat.checkSelfPermission(
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


        }
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
                if (readSmsAccepted && sendSmsAccepted && phoneStateAccepted) isPermissionGranted =
                    true
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


}