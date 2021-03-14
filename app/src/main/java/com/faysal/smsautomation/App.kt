package com.faysal.smsautomation

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.faysal.smsautomation.database.AppDatabase


class App : Application() {
    companion object{
        val CHANNEL_ID = "SMS_RECHARGE"
    }

    override fun onCreate() {
        super.onCreate()
        DatabaseBuilder.getInstance(this);
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}