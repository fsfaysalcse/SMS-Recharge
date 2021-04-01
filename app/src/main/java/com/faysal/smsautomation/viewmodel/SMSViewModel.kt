package com.faysal.smsautomation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.faysal.smsautomation.database.DeliveredSMS
import com.faysal.smsautomation.database.PhoneSmsDao
import com.faysal.smsautomation.database.SmsDatabase

class SMSViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var alldeliverdSms : LiveData<List<DeliveredSMS>>
    lateinit var smsDao: PhoneSmsDao

    init {
        val database = SmsDatabase.getInstance(application.applicationContext)
        smsDao = database.phoneSmsDao()
        alldeliverdSms = smsDao.getAllDelivered()
    }

    fun getAllDeliveredMessage(): LiveData<List<DeliveredSMS>> {
        return alldeliverdSms
    }

}