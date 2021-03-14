package com.faysal.smsautomation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import com.faysal.smsautomation.DatabaseBuilder
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.SmsDao
import com.faysal.smsautomation.database.viewmodels.RoomDBViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception


class SMSReciver : BroadcastReceiver() {

    private val SMS_RECEIVED: String = "android.provider.Telephony.SMS_RECEIVED"
    private val TAG = "SMSBroadcastReceiver"
    lateinit var viewModel : RoomDBViewModel
    lateinit var smsDao : SmsDao



    override fun onReceive(context: Context, intent: Intent) {
        val database = DatabaseBuilder.getInstance(context)
        smsDao = database.smsDao()

        var inSIM = false

        if (intent.getAction() != null) {
            if (intent.getAction().equals(SMS_RECEIVED)) {
                val bundle: Bundle = intent.getExtras()!!
                if (bundle != null) {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    for (message in messages) {

                        Log.d(TAG, "Message Content : " + " == " + (message.getMessageBody()))
                        Log.d(
                            TAG,
                            "Message Content Body : " + " == " + message.getDisplayMessageBody()
                        )
                        Log.d(
                            TAG,
                            "Message recieved From" + " == " + messages[0]?.getOriginatingAddress()
                        )


                        insertSms(
                            PhoneSms(
                            sender_phone = message.originatingAddress,
                                receiver_phone = "3453454",
                                body = message.displayMessageBody,
                                thread_id = "34543",
                                timestamp = message.timestampMillis.toString()
                        ))




                        GlobalScope.launch {
                            try {
                                val list = smsDao.getAll()
                                Log.d(TAG, "onReceive: "+list.size)
                            }catch (e : Exception){
                                e.printStackTrace()
                            }
                        }


                        /*  val serviceIntent = Intent(context,InternetServ::class.java)
                          serviceIntent.putExtra("inputExtra", message.getDisplayMessageBody())
                          InternetServ.enqueueWork(context, serviceIntent)*/


                    }
                }
            }
        }
    }

    fun insertSms(sms: PhoneSms){
        GlobalScope.launch {
            try {
                smsDao.insert(sms)
                Log.d(TAG, "SMS added successfully")
            }catch (e : Exception){
                Log.d(TAG, "Failed to insert data into room")
            }
        }
    }
}

/*    if (intent.getAction() != null) {
        if (intent.getAction().equals(SMS_RECEIVED)) {
            val bundle: Bundle = intent.getExtras()!!
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<Any>?
                val messages: Array<SmsMessage?> = arrayOfNulls<SmsMessage>(pdus!!.size)
                for (i in pdus!!.indices) {
                    messages[i] = SmsMessage.createFromPdu(pdus!![i] as ByteArray)
                    var from = ""+messages[i]?.getOriginatingAddress();

                    Log.d(TAG, "Message Content : " + " == " + (messages[i]?.getMessageBody()))
                    Log.d(TAG, "Message Content Body : " + " == " + messages[i]?.getDisplayMessageBody())
                    Log.d(TAG, "Message recieved From" + " == " + messages[0]?.getOriginatingAddress())

                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    Log.d(TAG, "Number "+messages.get(0).)
                }
                *//*if (messages.length > -1) {
                    Log.d(TAG,"Message recieved: "," == "+ messages[0].getMessageBody());
                    Log.d(TAG,"Message recieved From"," == "+ messages[0].getOriginatingAddress());
              }*//*
            }
        }*/

