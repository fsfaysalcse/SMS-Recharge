package com.faysal.smsautomation.services

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.work.*
import com.faysal.smsautomation.database.Activites
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class HandlerSMSWork(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {


    private val JOB_GROUP_NAME: String = "outgoing_message_operation"
    lateinit var smsDao: PhoneSmsDao
    lateinit var apiService: ApiService

    private var result = Result.success()

    init {
        smsDao = SmsDatabase.getInstance(context).phoneSmsDao()
        apiService = NetworkBuilder.getApiService()
    }


    override fun doWork(): Result {
        val smsId = inputData.getInt("smsid", -1)
        val sender = inputData.getString("sender")
        val simNo = inputData.getString("simNo")
        val datetime = inputData.getString("datetime")
        val smsBody = inputData.getString("smsBody")
        val isProcessing = inputData.getString("isProcessing")

        val phoneSms = PhoneSms(smsId, sender, simNo, smsBody, datetime, true)

        val service =
            Integer.valueOf(SharedPref.getString(applicationContext, Constants.SHARED_SERVICE))
        val verifyCode = Integer.valueOf(
            SharedPref.getString(
                applicationContext,
                Constants.SHARED_VERIFICATION_CODE
            )
        )


        GlobalScope.launch {
            supervisorScope {
                try {
                    val response = async {
                        apiService.sendSmsToServer(
                            service,
                            verifyCode,
                            sender,
                            simNo,
                            datetime,
                            smsBody
                        )
                    }.await()

                    val outgoingResponse =
                        async { apiService.getOutgoingMessages(verifyCode) }.await()

                    if (response.isSuccessful && outgoingResponse.isSuccessful) {

                        if (response.body()?.message == "Success") {
                            deleteSms(phoneSms)

                            saveActivites(
                                Activites(
                                    message = smsBody + ".... Saved to server",
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                                    status = true,

                                    )
                            )


                            val responseBody = outgoingResponse.body()
                            if (responseBody?.size!! > 0) {
                                val reciverInfo = responseBody?.get(0)?.url
                                if (!reciverInfo.isNullOrEmpty()) {
                                    sendOutgoingSms(reciverInfo)
                                }
                            }
                        }

                        result = Result.success()


                    } else {
                        saveActivites(
                            Activites(
                                message = smsBody + ".... Failed to Server",
                                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                                status = true,

                                )
                        )

                        result = Result.failure()
                    }


                } catch (e: Throwable) {


                    val smsNew = phoneSms.apply {
                        processRunning = false
                    }

                    updateSms(smsNew)

                    saveActivites(
                        Activites(
                            message = "Something wrong..." + e.message,
                            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
                            status = true,

                            )
                    )

                    result = Result.failure()
                }
            }
        }

        return result
    }

    private fun sendOutgoingSms(reciverInfo: String) {

        val interval = SharedPref.getString(applicationContext, Constants.SHARED_INTERVAL).toLong()
        val datas = Data.Builder().apply {
            putString("messageUrl", reciverInfo)
        }.build()


        val workRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(WorkMessageSender::class.java)
            .setInitialDelay(interval, TimeUnit.SECONDS)
            .setInputData(datas)
            .build()

        val workManager: WorkManager = WorkManager.getInstance(applicationContext);
        var work: WorkContinuation = workManager.beginUniqueWork(
            JOB_GROUP_NAME,
            ExistingWorkPolicy.APPEND,
            workRequest
        );
        work.enqueue();
    }


    fun saveActivites(sms: Activites) {
        GlobalScope.launch {
            try {
                smsDao.saveDeliveredMessage(sms)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSms(sms: PhoneSms) {
        GlobalScope.launch {
            try {
                smsDao.delete(sms)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateSms(sms: PhoneSms) {
        GlobalScope.launch {
            try {
                smsDao.update(sms)
                Log.d(InternetService.TAG, "SMS update successfully")
            } catch (e: Exception) {
                Log.d(InternetService.TAG, "Failed to update data into room")
            }
        }
    }


}