package com.faysal.smsautomation.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "delivered_sms")
data class Activites(
    @PrimaryKey(autoGenerate = true) var deliverid : Int = 0,
    @ColumnInfo(name = "number") var sender_phone : String?,
    @ColumnInfo(name = "message") var message : String?,
    @ColumnInfo(name = "timestamp") var timestamp : String?,
    @ColumnInfo(name = "status") var status: Boolean = false,
    @ColumnInfo(name = "fromSim") var fromSim: String?

)