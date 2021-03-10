package com.faysal.smsautomation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.gsm.SmsManager
import android.util.Log
import android.widget.Toast





class SMSReciver : BroadcastReceiver() {

    private val SMS_RECEIVED: String = "android.provider.Telephony.SMS_RECEIVED"
    private val TAG = "SMSBroadcastReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        var inSIM = false

        if (intent.getAction() != null) {
            if (intent.getAction().equals(SMS_RECEIVED)) {
                val bundle: Bundle = intent.getExtras()!!
                if (bundle != null) {
                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    for (message in messages) {

                        Log.d(TAG, "Message Content : " + " == " + (message.getMessageBody()))
                        Log.d(TAG, "Message Content Body : " + " == " + message.getDisplayMessageBody())
                        Log.d(TAG, "Message recieved From" + " == " + messages[0]?.getOriginatingAddress())

                        /*val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                        Log.d(TAG, "Number "+messages.get(0).messageBody)*/
                    }
                        /*if (messages.length > -1) {
                            Log.d(TAG,"Message recieved: "," == "+ messages[0].getMessageBody());
                            Log.d(TAG,"Message recieved From"," == "+ messages[0].getOriginatingAddress());
                      }*/
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

}