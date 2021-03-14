package com.faysal.smsautomation.database

import androidx.room.*

@Dao
interface PhoneSmsDao {
    @Query("SELECT * FROM phone_sms")
    suspend fun getAll(): List<PhoneSms>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: PhoneSms)

    @Delete
    suspend fun delete(sms: PhoneSms)
}