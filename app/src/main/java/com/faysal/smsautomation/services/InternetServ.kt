package com.faysal.smsautomation.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.faysal.smsautomation.internet.ApiInterface
import com.faysal.smsautomation.internet.ApiService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class InternetServ : JobIntentService() {

    lateinit var service :  ApiInterface

    companion object{
        val TAG = "InternetServ";
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, InternetServ::class.java, 123, work)
        }
    }

    override fun onCreate() {
        super.onCreate()
        service = ApiService.getApiService();
    }


    override fun onHandleWork(intent: Intent) {
        val input = intent.getStringExtra("inputExtra")
        if (input !=null){
           serverOperation();
        }
    }

    private fun serverOperation() {
        runBlocking  {
            try {
                val response = async { service.getAllPost() }.await()
                if (response.isSuccessful){
                    Log.d(TAG, "serverOperation: Success ")
                    /*for (post  in response.body()!!){
                        Log.d(TAG, "serverOperation: "+post.title)
                    }*/

                }else{
                    Log.d(TAG, "serverOperation: Failed")
                }
            }catch (e : Throwable){
                Log.d(TAG, "serverOperation: Error "+e.message)
            }

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