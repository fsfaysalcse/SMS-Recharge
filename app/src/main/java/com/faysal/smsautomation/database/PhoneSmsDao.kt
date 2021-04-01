package com.faysal.smsautomation.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhoneSmsDao {
    @Query("SELECT * FROM phone_sms")
    suspend fun getAll(): List<PhoneSms>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: PhoneSms)

    @Delete
    suspend fun delete(sms: PhoneSms)

    @Insert
    suspend fun saveDeliveredMessage(sms: DeliveredSMS)


    @Query("SELECT * FROM delivered_sms")
    fun getAllDelivered(): LiveData<List<DeliveredSMS>>
}