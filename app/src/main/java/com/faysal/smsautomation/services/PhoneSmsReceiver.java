package com.faysal.smsautomation.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


public class PhoneSmsReceiver extends BroadcastReceiver {


    private static final String TAG = "PhoneSmsReceiver";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        int i;
        Bundle extras = intent.getExtras();
        int i2 = 1;
        try {
            Object obj = intent.getExtras().get("slot_id");
            if (obj == null) {
                i = Integer.parseInt((String) intent.getExtras().get("subscription"));
            } else {
                i = Integer.parseInt((String) obj) + 1;
            }
            i2 = i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Object[] objArr = (Object[]) extras.get("pdus");
        for (Object obj2 : objArr) {
            SmsMessage createFromPdu = SmsMessage.createFromPdu((byte[]) obj2);
            createFromPdu.getDisplayOriginatingAddress();
            createFromPdu.getDisplayOriginatingAddress();
            String messageBody = createFromPdu.getMessageBody();
            if (messageBody != null) {
                try {
                    Log.d("Message Listener", messageBody);
                  //  mListener.messageReceived(createFromPdu, i2);
                } catch (Exception unused) {
                }
            }
        }
    }

//    public static void bindListener(SmsListener smsListener) {
//        mListener = smsListener;
//    }
}
