package com.faysal.smsautomation.services

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.faysal.smsautomation.Models.OutSms
import com.faysal.smsautomation.database.Activites
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SendUtil
import com.faysal.smsautomation.util.SharedPref
import com.faysal.smsautomation.util.SimUtil
import com.faysal.smsautomation.util.SimUtil.sendSMS
import com.google.gson.Gson
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

    private lateinit var apiservice : ApiService

    override fun doWork(): Result {

        val background_service = SharedPref.getBoolean(applicationContext, Constants.BACKGROUND_SERVVICE)
        if (!background_service) {
            return Result.success()
        }




        val messageUrl = inputData.getString("messageUrl")

        if (messageUrl.isNullOrEmpty()){
            return Result.failure()
        }

        apiservice = NetworkBuilder.getApiService(applicationContext)


       CoroutineScope(Dispatchers.IO).launch {
            supervisorScope {
                try {
                    val response = async { apiservice.getOutSms(messageUrl)}.await()
                    if (response.isSuccessful){
                        response.body()?.let { outsms ->
                            if (outsms.guid.isNullOrEmpty()) outsms.guid = ""
                            if (outsms.message.isNullOrEmpty()) outsms.message = ""
                            if (outsms.number.isNullOrEmpty()) outsms.number = ""

                            Log.d(TAG, "doWork: "+response.body().toString())

                            val result = SimUtil.sendSMS(outsms.number,"${outsms.message}",applicationContext)
                            if (result){
                                saveActivites(
                                    Activites(
                                        message = "${outsms.message} - outgoing message has send",
                                        status = true,
                                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                                    ))
                            }else{
                                saveActivites(
                                    Activites(
                                        message = "${outsms.message} - outgoing message sending failed",
                                        status = false,
                                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                                    ))
                            }
                        }
                    }

                    result = Result.success()

                }catch (e : Exception){
                    Log.d(TAG, "doWork: Failed something wrong")
                    result = Result.failure()
                }
            }

        }


       /* val queue = Volley.newRequestQueue(applicationContext)
        val stringRequest = StringRequest(
            Request.Method.GET, messageUrl,
            { response ->
                // Display the first 500 characters of the response string.
                Log.d(InternetService.TAG, "sendOutgoingSms: " + response.toString())
                var outsms = Gson().fromJson<OutSms>(response.toString(), OutSms::class.java)
                Log.d(InternetService.TAG, "sendOutgoingSms: " + outsms.number)



                if (outsms.guid.isNullOrEmpty()) outsms.guid = ""
                if (outsms.message.isNullOrEmpty()) outsms.message = ""
                if (outsms.number.isNullOrEmpty()) outsms.number = ""

                try {
                 val result = SimUtil.sendSMS(outsms.number,"${outsms.message}",applicationContext)
                    if (result){
                        saveActivites(
                            Activites(
                                message = "${outsms.message} - outgoing message has send",
                                status = true,
                                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                            ))
                    }else{
                        saveActivites(
                            Activites(
                                message = "${outsms.message} - outgoing message sending failed",
                                status = false,
                                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                            ))
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }
                result = Result.success()

            },
            {
                result = Result.failure()
            })

        queue.add(stringRequest)*/
        return result
    }

    fun saveActivites(activites: Activites) {
        val daos = SmsDatabase.getInstance(applicationContext).phoneSmsDao()
        GlobalScope.launch {
            try {
                daos.saveDeliveredMessage(activites)
            } catch (e: Exception) {
                Log.d(TAG, "Failed  " + e.message)
            }
        }
    }
}