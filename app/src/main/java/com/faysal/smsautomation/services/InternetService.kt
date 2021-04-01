package com.faysal.smsautomation.services

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.faysal.smsautomation.Models.OutSms
import com.faysal.smsautomation.database.Activites
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class InternetService : JobIntentService() {

    lateinit var service: ApiService
    lateinit var smsDao: PhoneSmsDao

    lateinit var apiService: ApiService


    companion object {
        val TAG = "InternetServ";
        private var lastJobId: Int = 123

        fun enqueueWork(context: Context, work: Intent) {
            lastJobId = Random(999999).nextInt()
            enqueueWork(context, InternetService::class.java, lastJobId, work)
        }
    }

    override fun onCreate() {
        super.onCreate()
        service = NetworkBuilder.getApiService();
        smsDao = SmsDatabase.getInstance(application).phoneSmsDao()
        apiService = NetworkBuilder.getApiService()
    }


    override fun onHandleWork(intent: Intent) {

        Log.d(TAG, "onHandleWork: ")

        val smsid = intent.getIntExtra("smsid", -1)
        val sender = intent.getStringExtra("sender")
        val simNo = intent.getStringExtra("simNo")
        val datetime = intent.getStringExtra("datetime")
        val smsBody = intent.getStringExtra("smsBody")

        enqueueWork(
            PhoneSms(
                smsid,
                sender,
                simNo,
                smsBody,
                "123",
                datetime
            )
        )
    }


    fun enqueueWork(sms: PhoneSms) {

        val service =
            Integer.valueOf(SharedPref.getString(applicationContext, Constants.SHARED_SERVICE))
        val verifycode = Integer.valueOf(
            SharedPref.getString(
                applicationContext,
                Constants.SHARED_VERIFICATION_CODE
            )
        )


        GlobalScope.launch {
            supervisorScope {
                try {
                    val response = async {
                        apiService.sendSmsToServer(
                            service,
                            verifycode,
                            sms.sender_phone,
                            sms.receiver_phone,
                            sms.timestamp,
                            sms.body
                        )
                    }.await()

                    val outgoingResponse =
                        async { apiService.getOutgoingMessages(verifycode) }.await()

                    if (response.isSuccessful) {
                        if (response.body()?.message == "Success") {
                            deleteSms(sms)


                            saveActivites(
                                Activites(
                                    sender_phone = sms.sender_phone,
                                    message ="[IN] - > "+ sms.body + ".... this sms has been saved to server",
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                                    status = true,
                                    fromSim = "Sim 1"

                                ))

                            val milisecound = SharedPref.getString(applicationContext,Constants.SHARED_INTERVAL).toInt() * 1000
                            SystemClock.sleep(milisecound.toLong())


                            if (outgoingResponse.isSuccessful) {
                                val responseBody = outgoingResponse.body()

                                if (responseBody?.size!! > 0) {
                                    val reciverInfo = responseBody?.get(0)?.url
                                    if (!reciverInfo.isNullOrEmpty()) {
                                        sendOutgoingSms(reciverInfo)
                                    }
                                }
                            }

                            Log.d(TAG, "Job Successfully done")


                        }
                    }

                } catch (e: Throwable) {
                    Toast.makeText(this@InternetService, ""+e.printStackTrace(), Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "enqueueWork: failed " + e.message)
                }
            }
        }

    }

    private fun sendOutgoingSms(reciverInfo: String) {
        val queue = Volley.newRequestQueue(this)
        val url = reciverInfo

        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Display the first 500 characters of the response string.
                Log.d(TAG, "sendOutgoingSms: " + response.toString())
                val outsms = Gson().fromJson<OutSms>(response.toString(), OutSms::class.java)
                Log.d(TAG, "sendOutgoingSms: "+outsms.number)
                sendSMS(outsms.number,outsms.message,outsms.guid)

            },
            Response.ErrorListener {


            })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }


    fun sendSMS(phoneNo: String, msg: String,guid : String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNo, null, msg+"\n\n GUID : "+guid, null, null)

            saveActivites(
                Activites(
                    sender_phone = phoneNo ,
                    message ="[OUT] - > "+ msg + "  GUID $guid .... this message has been send to "+phoneNo,
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                    status = true,
                    fromSim = "Sim 1"

                ))
        } catch (ex: Exception) {
            saveActivites(
                Activites(
                    sender_phone = phoneNo ,
                    message ="[OUT] - > "+ msg + "  GUID $guid .... faild sending  sms to "+phoneNo,
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                    status = false,
                    fromSim = "Sim 1"

                ))

            ex.printStackTrace()
        }
    }

    fun saveActivites(sms: Activites) {
        GlobalScope.launch {
            try {
                smsDao.saveDeliveredMessage(sms)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSms(sms: PhoneSms) {
        GlobalScope.launch {
            try {
                smsDao.delete(sms)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onStopCurrentWork(): Boolean {
        Log.d(TAG, "onStopCurrentWork")
        return super.onStopCurrentWork()
    }


}


