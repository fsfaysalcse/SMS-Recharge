package com.faysal.smsautomation.services

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.faysal.smsautomation.Models.OutSms
import com.faysal.smsautomation.database.Activites
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import com.faysal.smsautomation.util.SimUtil
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "WorkMessageSender"
class WorkMessageSender(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {

    private var result = Result.success()

    private var apiService: ApiService = NetworkBuilder.getApiService(applicationContext)

    override fun doWork(): Result {

        val bs = SharedPref.getBoolean(applicationContext, Constants.BACKGROUND_SERVICE)
        if (!bs) { return Result.success() }

        val msgUrl = inputData.getString("messageUrl")

        if (msgUrl.isNullOrEmpty()){ return Result.failure() }

        apiService.getOutSms(msgUrl).enqueue(object : Callback<OutSms>{
            override fun onResponse(call: Call<OutSms>, response: retrofit2.Response<OutSms>) {
                if (response.isSuccessful){
                    response.body()?.let { outs ->
                        if (outs.guid.isNullOrEmpty()) outs.guid = ""
                        if (outs.message.isNullOrEmpty()) outs.message = ""
                        if (outs.number.isNullOrEmpty()) outs.number = ""

                        Log.d(TAG, "doWork: "+response.body().toString())

                        val result = SimUtil.sendSMS(outs.number,"${outs.message}",applicationContext)
                        if (result){
                            saveActivities(
                                Activites(
                                    message = "${outs.message} - outgoing message has send",
                                    status = true,
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                                ))
                        }else{
                            saveActivities(
                                Activites(
                                    message = "${outs.message} - outgoing message sending failed",
                                    status = false,
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                                ))
                        }
                    }
                }

                result = Result.success()
            }

            override fun onFailure(call: Call<OutSms>, t: Throwable) {
                Log.d(TAG, "doWork: Failed something wrong")
                result = Result.failure()
            }

        })

        return result
    }

   private fun saveActivities(activities: Activites) {
        val daos = SmsDatabase.getInstance(applicationContext).phoneSmsDao()
        GlobalScope.launch {
            try {
                daos.saveDeliveredMessage(activities)
            } catch (e: Exception) {
                Log.d(TAG, "Failed  " + e.message)
            }
        }
    }
}