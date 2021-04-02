package com.faysal.smsautomation.msg_app.receivers;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;


import androidx.core.app.NotificationCompat;

import com.faysal.smsautomation.App;
import com.faysal.smsautomation.MainActivity;
import com.faysal.smsautomation.R;
import com.faysal.smsautomation.util.ContactUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Shantanu on 23-08-2016.
 */
public class SMSReceiver extends BroadcastReceiver {
    public SMSReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle myBundle = intent.getExtras();
        SmsMessage[] messages;

        if (myBundle != null) {
            Object[] pdus = (Object[]) myBundle.get("pdus");

            messages = new SmsMessage[pdus.length];

           // SMSSQLiteHelper smsDB = new SMSSQLiteHelper(context);

            for (int i = 0; i < messages.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = myBundle.getString("format");
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                } else {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);//deprecated method is handled in if...else
                }

             //   smsDB.addSMS(new SMS(messages[i].getOriginatingAddress(), messages[i].getMessageBody(), smsDB.getConversatationID(messages[i].getOriginatingAddress()), getCurrentTimeStamp()));
                showNotification(context, messages[i].getOriginatingAddress(), messages[i].getMessageBody());
                System.out.println(messages[i].getIndexOnSim());

            }

        }
    }


    void showNotification(Context context, String address, String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, "SMS_RECHARGE")
                        .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                        .setContentTitle("Hello World")
                        .setContentText(message);
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.string.notificationID, mBuilder.build());
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return formatter.format(System.currentTimeMillis());
    }

}//end class
