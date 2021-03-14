package com.faysal.smsautomation.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class InternetServ : JobIntentService() {

    lateinit var service: ApiService
    lateinit var smsDao: PhoneSmsDao

    companion object {
        val TAG = "InternetServ";
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, InternetServ::class.java, 123, work)
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
            } catch (e: Exception) {
                e.printStackTrace()
            }


            Log.d(TAG, "serverOperation: " + listSMS.size)

            /*runBlocking  {
                try {
                    val response = async { service.getAllPost() }.await()
                    if (response.isSuccessful){
                        Log.d(TAG, "serverOperation: Success ")
                        *//*for (post  in response.body()!!){
                        Log.d(TAG, "serverOperation: "+post.title)
                    }*//*

                }else{
                    Log.d(TAG, "serverOperation: Failed")
                }
            }catch (e : Throwable){
                Log.d(TAG, "serverOperation: Error "+e.message)
            }

        }*/
        }


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