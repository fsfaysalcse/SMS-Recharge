package com.faysal.smsautomation.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhoneSmsDao {
    @Query("SELECT * FROM phone_sms WHERE processRunning = 0 ORDER BY timestamp DESC")
    suspend fun getAll(): List<PhoneSms>


    @Query("SELECT * FROM phone_sms WHERE processRunning = 0")
    fun getAllSmsByLiveData(): LiveData<List<PhoneSms>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: PhoneSms)

    @Delete
    suspend fun delete(sms: PhoneSms)

    @Update
    suspend fun update(sms: PhoneSms)

    @Insert
    suspend fun saveDeliveredMessage(sms: Activites)


    @Query("SELECT * FROM delivered_sms ORDER BY timestamp DESC LIMIT 20")
    fun getAllDelivered(): LiveData<List<Activites>>
}