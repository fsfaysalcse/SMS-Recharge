package com.faysal.smsautomation.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log
import androidx.work.*
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

    private val JOB_GROUP_NAME = "handel_sms_work"
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
        val daos = SmsDatabase.getInstance(context).phoneSmsDao()
        GlobalScope.launch {
            try {
                daos.saveDeliveredMessage(activites)
            } catch (e: Exception) {
                Log.d(TAG, "Failed  " + e.message)
            }
        }
    }


    private fun prepareForBackgroundService() {

       val daos = SmsDatabase.getInstance(context).phoneSmsDao()

        var listedDataJob = GlobalScope.async { daos.getAll() }
        listedDataJob.invokeOnCompletion { cause ->
            if (cause != null) {
                //something happened and the job didn't finish successfully.  Handle that here
                Unit
            } else {
                val myData = listedDataJob.getCompleted()
                myData.forEach { sms ->

                    sendSmsToBackgroundService(sms)

                }
            }
        }


    }


    private fun sendSmsToBackgroundService(sms: PhoneSms) {

        val interval = SharedPref.getString(context,Constants.SHARED_INTERVAL).toLong()


        val datas =  Data.Builder().apply {
            putInt("smsid", sms.smsid)
            putString("simNo", sms.receiver_phone)
            putString("sender", sms.sender_phone)
            putString("datetime", sms.timestamp)
            putString("smsBody", sms.body)
        }.build()



        val  workRequest : OneTimeWorkRequest = OneTimeWorkRequest.Builder(HandlerSMSWork::class.java)
                .setInitialDelay(interval,TimeUnit.SECONDS)
                .setInputData(datas)
                .build()

        val workManager : WorkManager = WorkManager.getInstance(context);
        var work : WorkContinuation = workManager.beginUniqueWork(
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


    fun insertSms(sms: PhoneSms) {
        val daos = SmsDatabase.getInstance(context).phoneSmsDao()
        GlobalScope.launch {
            try {
                daos.insert(sms)
                Log.d(TAG, "SMS added successfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to insert data into room" + e.message)
            }
        }
    }

    fun updateSms(sms: PhoneSms) {
        val daos = SmsDatabase.getInstance(context).phoneSmsDao()
        GlobalScope.launch {
            try {
                daos.update(sms)
                Log.d(TAG, "SMS update successfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to update data into room")
            }
        }
    }
}
