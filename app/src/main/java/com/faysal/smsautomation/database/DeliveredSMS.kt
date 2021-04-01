package com.faysal.smsautomation.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "delivered_sms")
data class DeliveredSMS(
    @PrimaryKey(autoGenerate = true) var deliverid : Int = 0,
    @ColumnInfo(name = "number") var sender_phone : String?,
    @ColumnInfo(name = "message") var body : String?,
    @ColumnInfo(name = "guid") var guid : String?,
    @ColumnInfo(name = "timestamp") var delivered_time: String?,
    @ColumnInfo(name = "isSend") var isSend: Boolean = false,
    @ColumnInfo(name = "fromSim") var fromSim: String?

)