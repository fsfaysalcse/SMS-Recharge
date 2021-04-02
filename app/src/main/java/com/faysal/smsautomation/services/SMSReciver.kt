package com.faysal.smsautomation.services

import android.Manifest
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


class SMSReciver : BroadcastReceiver() {

    private val SMS_RECEIVED: String = "android.provider.Telephony.SMS_RECEIVED"
    private val TAG = "SMSBroadcastReceiver"
    lateinit var smsDao: PhoneSmsDao
    lateinit var database :  SmsDatabase

    lateinit var context: Context

    fun saveActivites(sms: Activites) {

        GlobalScope.launch {
            try {
                smsDao.saveDeliveredMessage(sms)
                Log.d(TAG, "insertDelivered: saved succesfully")
            } catch (e: Exception) {
                Log.d(TAG, "Failed  " + e.message)
            }
        }
    }


    override fun onReceive(ct: Context, intent: Intent) {


        context = ct
        database = SmsDatabase.getInstance(context)
        smsDao = database.phoneSmsDao()


        val background_service = SharedPref.getBoolean(context, Constants.BACKGROUND_SERVVICE)
        if (!background_service) {
            return
        }

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val phoneNumber = telephonyManager!!.line1Number
        Log.d(TAG, "onReceive: " + phoneNumber)

        var inSIM = false

        if (intent.getAction() != null) {
            if (intent.getAction().equals(SMS_RECEIVED)) {
                val bundle: Bundle = intent.getExtras()!!
                if (bundle != null) {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    for (message in messages) {

                        val timeStamp: String =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                        insertSms(
                            PhoneSms(
                                smsid = 0,
                                sender_phone = message.originatingAddress,
                                receiver_phone = phoneNumber,
                                body = message.displayMessageBody,
                                thread_id = "34543",
                                timestamp = timeStamp,
                                processRunning = false
                            )
                        )

                      /*  val simSlot = intent.getIntExtra("simSlot", -1)
                        Log.d(TAG, "onReceive: Sim slot "+simSlot)*/
                        prepareForBackgroundService()
                    }

                }
            }
        }


    }



    private fun prepareForBackgroundService() {


        var listSMS: List<PhoneSms> = mutableListOf()



        GlobalScope.async {
            try {

                listSMS = smsDao.getAll()
                Log.d(TAG, "prepareForBackgroundService: " + listSMS.size)

                listSMS.forEach { sms ->
                    Log.d(
                        TAG,
                        "prepareForBackgroundService: " + sms.body + " ---> " + sms.processRunning
                    )
                    sendSmsToBackgroundService(sms)
                    val milisecound = SharedPref.getString(context, Constants.SHARED_INTERVAL).toInt() * 1000
                    SystemClock.sleep(milisecound.toLong())
                }


            } catch (e: Exception) {
                Log.d(TAG, "prepareForBackgroundService: " + e.message)
                e.printStackTrace()
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
