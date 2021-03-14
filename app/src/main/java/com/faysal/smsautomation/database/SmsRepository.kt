package com.faysal.smsautomation.database

import android.app.Application
import androidx.lifecycle.LiveData
import com.faysal.smsautomation.DatabaseBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SmsRepository (application: Application){

    lateinit var smsDao : SmsDao
    lateinit var getAllSms : LiveData<List<PhoneSms>>

    init {
        val database = DatabaseBuilder.getInstance(application)
        smsDao = database.smsDao()

    }


}