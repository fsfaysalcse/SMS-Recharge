package com.faysal.smsautomation.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.work.*
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.sql.Time
import java.util.concurrent.TimeUnit


class InternetService : JobIntentService() {

    lateinit var service: ApiService
    lateinit var smsDao: PhoneSmsDao

    companion object {
        val TAG = "InternetServ";
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, InternetService::class.java, 123, work)
        }
    }

    override fun onCreate() {
        super.onCreate()
        service = NetworkBuilder.getApiService();
        smsDao = SmsDatabase.getInstance(application).phoneSmsDao()
    }


    override fun onHandleWork(intent: Intent) {
        val input = intent.getStringExtra("inputExtra")
        if (input != null) {
            serverOperation();
        }
    }

    private fun serverOperation() {

        var listSMS: List<PhoneSms> = mutableListOf()
        GlobalScope.async {
            try {
                listSMS = smsDao.getAll()
                for (sms in listSMS){
                    enqueueWork(sms)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }




    }

    fun enqueueWork(sms: PhoneSms){

        try {
            Thread.sleep(5000)
        }catch (e : Exception){

        }

        val workManager = WorkManager.getInstance(this)
        val data: Data = Data.Builder()
            .putString("simNo", sms.receiver_phone)
            .putString("sender", sms.sender_phone)
            .putString("datetime", sms.timestamp)
            .putString("smsBody", sms.body)
            .build()

        val constraints: Constraints = Constraints.Builder()
            .build()

        val workRequest = OneTimeWorkRequest.Builder(
            ServerWorker::class.java)
            .setInputData(data)
            .setConstraints(constraints)
            .build()


      workManager.enqueue(workRequest)


    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onStopCurrentWork(): Boolean {
        Log.d(TAG, "onStopCurrentWork")
        return super.onStopCurrentWork()
    }


}