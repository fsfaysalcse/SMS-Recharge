package com.faysal.smsautomation.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.faysal.smsautomation.database.Activites
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class SMSReciver : BroadcastReceiver() {

    private val SMS_RECEIVED: String = "android.provider.Telephony.SMS_RECEIVED"
    private val TAG = "SMSBroadcastReceiver"
    lateinit var smsDao: PhoneSmsDao
    lateinit var database: SmsDatabase

    lateinit var context: Context


    @SuppressLint("MissingPermission")
    override fun onReceive(ct: Context, intent: Intent) {

        Log.d(TAG, "onReceive: receive a sms")
        context = ct
        database = SmsDatabase.getInstance(context)
        smsDao = database.phoneSmsDao()


        val background_service = SharedPref.getBoolean(context, Constants.BACKGROUND_SERVVICE)
        if (!background_service) {
            return
        }

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

        val phoneNumber = telephonyManager!!.line1Number
        Log.d(TAG, "onReceive: " + phoneNumber)

        var inSIM = false

        if (intent.getAction() != null) {
            if (intent.getAction().equals(SMS_RECEIVED)) {
                val bundle: Bundle = intent.getExtras()!!
                if (bundle != null) {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    for (message in messages) {

                        insertSms(
                            PhoneSms(
                                smsid = 0,
                                sender_phone = message.originatingAddress,
                                receiver_phone = phoneNumber,
                                body = message.displayMessageBody,
                                thread_id = "34543",
                                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                                processRunning = false
                            )
                        )

                        saveActivites(
                            Activites(
                                message = "Message Recived : " + message.displayMessageBody,
                                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                                status = true
                            )
                        )


                        prepareForBackgroundService()
                    }

                }
            }
        }

    }


    fun saveActivites(activites: Activites) {

        GlobalScope.launch {
            try {
                smsDao.saveDeliveredMessage(activites)
                Log.d(TAG, "insertDelivered: saved succesfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed  " + e.message)
            }
        }
    }


    private fun prepareForBackgroundService() {

        smsDao = database.phoneSmsDao()

        var listedDataJob = GlobalScope.async { smsDao.getAll() }
        listedDataJob.invokeOnCompletion {cause ->
            if (cause != null) {
                //something happened and the job didn't finish successfully.  Handle that here
                Unit
            } else {
                val myData = listedDataJob.getCompleted()
                Log.d(TAG, "prepareForBackgroundService:test "+myData.size)
                myData.forEach { sms ->
                    Log.d(
                        TAG,
                        "prepareForBackgroundService: " + sms.body + " ---> " + sms.processRunning
                    )
                    sendSmsToBackgroundService(sms)

                    Log.d(TAG, "prepareForBackgroundService: Before")

                    val interval = SharedPref.getString(context, Constants.SHARED_INTERVAL).toInt()
                    val milliseconds: Long = TimeUnit.SECONDS.toMillis(interval.toLong())
                    SystemClock.sleep(milliseconds)

                    Log.d(TAG, "prepareForBackgroundService: After")
                }
            }
        }


    }


    private fun sendSmsToBackgroundService(sms: PhoneSms) {
        val serviceIntent = Intent(context, InternetService::class.java).apply {
            putExtra("smsid", sms.smsid)
            putExtra("simNo", sms.receiver_phone)
            putExtra("sender", sms.sender_phone)
            putExtra("datetime", sms.timestamp)
            putExtra("smsBody", sms.body)
            putExtra("isProcessing", true)
        }

        val smsNew = sms.apply {
            processRunning = true
        }

        updateSms(smsNew)
        InternetService.enqueueWork(context, serviceIntent)

    }


    fun insertSms(sms: PhoneSms) {
        GlobalScope.launch {
            try {
                smsDao.insert(sms)
                Log.d(TAG, "SMS added successfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to insert data into room" + e.message)
            }
        }
    }

    fun updateSms(sms: PhoneSms) {
        GlobalScope.launch {
            try {
                smsDao.update(sms)
                Log.d(TAG, "SMS update successfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to update data into room")
            }
        }
    }
}
