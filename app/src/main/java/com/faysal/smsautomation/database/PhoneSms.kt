package com.faysal.smsautomation.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "phone_sms")
data class PhoneSms(
    @PrimaryKey(autoGenerate = true) val smsid : Int = 0,
    @ColumnInfo(name = "sender_phone") val sender_phone : String?,
    @ColumnInfo(name = "receiver_phone") val receiver_phone: String?,
    @ColumnInfo(name = "body") val body : String?,
    @ColumnInfo(name = "thread_id") val thread_id : String?,
    @ColumnInfo(name = "timestamp") val timestamp: String?

)