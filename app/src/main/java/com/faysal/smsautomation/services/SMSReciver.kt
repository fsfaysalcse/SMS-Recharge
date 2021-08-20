package com.faysal.smsautomation.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.Log
import com.faysal.smsautomation.database.Activites
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class SMSReciver : BroadcastReceiver() {


    private val SMS_RECEIVED: String = "android.provider.Telephony.SMS_RECEIVED"
    private val TAG = "SMSBroadcastReceiver"
    lateinit var smsDao: PhoneSmsDao
    lateinit var database: SmsDatabase
    lateinit var context: Context


    @SuppressLint("MissingPermission")
    override fun onReceive(ct: Context, intent: Intent) {
        
        context = ct
        database = SmsDatabase.getInstance(context)
        smsDao = database.phoneSmsDao()


        val background_service = SharedPref.getBoolean(context, Constants.BACKGROUND_SERVICE)
        if (!background_service) {
            return
        }

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

        val phoneNumber = telephonyManager!!.line1Number
        var inSIM = false

        if (intent.action != null) {
            if (intent.action.equals(SMS_RECEIVED)) {
                val bundle: Bundle = intent.extras!!
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






    private fun insertSms(sms: PhoneSms) {
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




}
