package com.faysal.smsautomation.services

import android.R
import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.SystemClock
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.faysal.smsautomation.App.Companion.CHANNEL_ID


class ServerService : IntentService("ExampleIntentService") {
    private var wakeLock: WakeLock? = null
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ExampleApp:Wakelock")
        wakeLock!!.acquire()

        Log.d(TAG, "Wakelock acquired")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Example IntentService")
                .setContentText("Running...")
                .build()
            startForeground(1, notification)
        }
    }

    override fun onHandleIntent(@Nullable intent: Intent?) {
        Log.d(TAG, "onHandleIntent")
        val input = intent!!.getStringExtra("inputExtra")
        Log.d(TAG, "onHandleIntent: "+input)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        wakeLock!!.release()
        Log.d(TAG, "Wakelock released")
    }

    companion object {
        private const val TAG = "ExampleIntentService"
    }

    init {
        setIntentRedelivery(true)
    }
}