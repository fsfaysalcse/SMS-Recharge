package com.faysal.smsautomation.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_sms")
data class PhoneSms(
    @PrimaryKey(autoGenerate = true) val smsid: Int,
    @ColumnInfo(name = "sender_phone") val sender_phone: String?,
    @ColumnInfo(name = "receiver_phone") val receiver_phone: String?,
    @ColumnInfo(name = "body") val body: String?,
    @ColumnInfo(name = "timestamp") val timestamp: String?,
    @ColumnInfo(name = "processRunning") var processRunning: Boolean?

)