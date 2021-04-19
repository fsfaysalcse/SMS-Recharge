package com.faysal.smsautomation.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;


public class SendUtil {
    private static final String TAG = "SendUtil";

    public static boolean sendSMS(Context context, final String phoneNumber, String message) {

        try {
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(message);
            int mMessageSentTotalParts = parts.size();

            Log.d(TAG, "Message Count: " + mMessageSentTotalParts);

            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

            for (int j = 0; j < mMessageSentTotalParts; j++) {
                sentIntents.add(sentPI);
                deliveryIntents.add(deliveredPI);
            }

            mMessageSentTotalParts = 0;
            sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

}
