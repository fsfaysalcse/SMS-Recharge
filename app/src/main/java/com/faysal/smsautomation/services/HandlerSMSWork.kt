package com.faysal.smsautomation.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.faysal.smsautomation.Models.SaveSms
import com.faysal.smsautomation.database.Activites
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "HandlerSMSWork"
class HandlerSMSWork(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private var smsDao: PhoneSmsDao = SmsDatabase.getInstance(context).phoneSmsDao()
    private var apiService: ApiService = NetworkBuilder.getApiService(applicationContext)

    private var result = Result.success()


    override fun doWork(): Result {
        val bs = SharedPref.getBoolean(applicationContext, Constants.BACKGROUND_SERVICE)
        if (!bs) { return Result.success() }

        val smsId = inputData.getInt("smsid", -1)
        val sender = inputData.getString("sender")
        val simNo = inputData.getString("simNo")
        val datetime = inputData.getString("datetime")
        val smsBody = inputData.getString("smsBody")
        val isProcessing = inputData.getString("isProcessing")

        val phoneSms = PhoneSms(smsId, sender, simNo, smsBody, datetime, true)

        val service =
            Integer.valueOf(SharedPref.getString(applicationContext, Constants.SHARED_SERVICE))
        val verifyCode = Integer.valueOf(
            SharedPref.getString(
                applicationContext,
                Constants.SHARED_VERIFICATION_CODE
            )
        )

        apiService.sendSmsToServer(
            service,
            verifyCode,
            sender,
            simNo,
            datetime,
            smsBody
        ).enqueue(object : Callback<SaveSms>{
            override fun onResponse(call: Call<SaveSms>, response: Response<SaveSms>) {
                if (response.isSuccessful){
                    if (response.body()?.message == "Success") {
                        deleteSms(phoneSms)

                        saveActivities(
                            Activites(
                                message = smsBody + ".... Saved to server",
                                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                                status = true)
                        )
                    }
                }
                result = Result.success()
            }

            override fun onFailure(call: Call<SaveSms>, t: Throwable) {
                saveActivities(
                    Activites(
                        message = smsBody + ".... Failed to Server",
                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                        status = true,

                        )
                )
                result = Result.failure()
            }

        })

        return result
    }


   private fun saveActivities(sms: Activites) {
        GlobalScope.launch {
            try {
                smsDao.saveDeliveredMessage(sms)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

   private fun deleteSms(sms: PhoneSms) {
        GlobalScope.launch {
            try {
                smsDao.delete(sms)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

   private fun updateSms(sms: PhoneSms) {
        GlobalScope.launch {
            try {
                smsDao.update(sms)
                Log.d(TAG, "SMS update successfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to update data into room")
            }
        }
    }

    override fun onStopped() {
        super.onStopped()
    }

}