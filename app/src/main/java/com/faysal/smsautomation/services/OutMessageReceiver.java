package com.faysal.smsautomation.services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class OutMessageReceiver extends BroadcastReceiver {

    private static final String TAG = "OutMessageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (getResultCode()) {

            case Activity.RESULT_OK:
                Log.d(TAG, "onReceive: SMS Delivered");
                break;
            case Activity.RESULT_CANCELED:
                Log.d(TAG, "onReceive: SMS not delivered");
                break;
        }
    }
}
