package com.faysal.smsautomation.services

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.core.app.JobIntentService
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import me.everything.providers.android.telephony.Sms
import me.everything.providers.android.telephony.TelephonyProvider


class InternetServ : JobIntentService() {

    lateinit var service :  ApiService

    companion object{
        val TAG = "InternetServ";
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, InternetServ::class.java, 123, work)
        }
    }

    override fun onCreate() {
        super.onCreate()
        service = NetworkBuilder.getApiService();
    }


    override fun onHandleWork(intent: Intent) {
        val input = intent.getStringExtra("inputExtra")
        if (input !=null){
           serverOperation();
        }
    }

    private fun serverOperation() {

        val telephonyProvider = TelephonyProvider(applicationContext)
        val smses: List<Sms> = telephonyProvider.getSms(TelephonyProvider.Filter.ALL).getList()
        
        for (sms in smses){
           val result = deleteSms(smsId = sms.id, thread_id = sms.threadId)
           Log.d(TAG, "serverOperation: ${sms.id} is deleted successfully $result")
        }



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

    fun deleteSMS(context: Context, message: String, number: String) {
        try {
            Log.d(TAG, "Deleting SMS from inbox")
            val uriSms = Uri.parse("content://sms/inbox")
            val c: Cursor? = context.contentResolver.query(
                uriSms, arrayOf(
                    "_id", "thread_id", "address",
                    "person", "date", "body"
                ), null, null, null
            )
            if (c != null && c.moveToFirst()) {
                do {
                    val id: Long = c.getLong(0)
                    val threadId: Long = c.getLong(1)
                    val address: String = c.getString(2)
                    val body: String = c.getString(5)
                    if (message == body && address == number) {
                        Log.d(TAG, "Deleting SMS with id: $threadId")
                        context.contentResolver.delete(
                            Uri.parse("content://sms/$id"), null, null
                        )
                    }
                } while (c.moveToNext())
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "Could not delete SMS from inbox: " + e.message)
        }
    }

    fun deleteSms(smsId: Long, thread_id: Int): Boolean {
        var isSmsDeleted = false
        isSmsDeleted = try {
            val thread = Uri.parse("content://sms")
            contentResolver.delete(
                thread, "thread_id=? and _id=?", arrayOf(
                    java.lang.String.valueOf(
                        thread_id
                    ), java.lang.String.valueOf(smsId)
                )
            )
            true
        } catch (ex: Exception) {
            false
        }
        return isSmsDeleted
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