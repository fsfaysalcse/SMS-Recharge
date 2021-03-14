package com.faysal.smsautomation.database.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.faysal.smsautomation.DatabaseBuilder
import com.faysal.smsautomation.database.PhoneSms
import com.faysal.smsautomation.database.SmsDao
import kotlinx.coroutines.launch
import java.lang.Exception

class RoomDBViewModel(application: Application) : ViewModel(){
    private  val TAG = "RoomDBViewModel"
    lateinit var smsDao : SmsDao
    lateinit var getAllSms : LiveData<List<PhoneSms>>

    init {
        val database = DatabaseBuilder.getInstance(application)
        smsDao = database.smsDao()
    }

/*     fun fetchAllSms() : LiveData<List<PhoneSms>> {
        viewModelScope.launch {
            try {
              getAllSms = smsDao.getAll()
            }catch (e : Exception){
                Log.d(TAG, "Failed to fetch data from room database")
            }
        }
        return getAllSms
    }

    fun insertSms(sms: PhoneSms){
        viewModelScope.launch {
            try {
                smsDao.insert(sms)
            }catch (e : Exception){
                Log.d(TAG, "Failed to insert data into room")
            }
        }
    }*/

}