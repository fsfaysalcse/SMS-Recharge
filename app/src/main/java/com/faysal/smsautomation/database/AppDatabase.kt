package com.faysal.smsautomation.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PhoneSms::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun smsDao(): SmsDao

}