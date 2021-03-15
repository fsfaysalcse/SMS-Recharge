package com.faysal.smsautomation.services

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ServerWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private val TAG = "ServerWorker"
    val context = context

    lateinit var apiService: ApiService

    init {
        apiService = NetworkBuilder.getApiService()
    }

    override fun doWork(): Result {

        var status = Result.failure()

        val service = Integer.valueOf(SharedPref.getString(context, Constants.SHARED_SERVICE))
        val verifycode = Integer.valueOf(SharedPref.getString(context, Constants.SHARED_VERIFICATION_CODE))
        val sender = inputData.getString("sender")
        val simNo = inputData.getString("simNo")
        val datetime = inputData.getString("datetime")
        val smsBody = inputData.getString("smsBody")


        GlobalScope.launch {
            supervisorScope {
                try {
                    Log.d(TAG, "doWork: inside coroutines")
                    val response = async {
                        apiService.sendSmsToServer(
                            service,
                            verifycode,
                            sender,
                            simNo,
                            datetime,
                            smsBody
                        )
                    }.await()

                    if (response.isSuccessful){
                        if (response.body()?.message == "Success"){
                            Log.d(TAG, "doWork: success ")
                            status = Result.success()
                        }else{
                            status = Result.failure()
                        }
                    }else{
                        status = Result.failure()
                    }

                } catch (e: Throwable) {
                    status = Result.failure()
                }
            }

        }




        return status
    }


}