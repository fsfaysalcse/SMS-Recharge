package com.faysal.smsautomation.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SmsDao {
    @Query("SELECT * FROM phone_sms")
    suspend fun getAll() : List<PhoneSms>

    @Insert
    suspend fun insert(sms : PhoneSms)

    @Delete
    suspend fun delete(sms : PhoneSms)
}