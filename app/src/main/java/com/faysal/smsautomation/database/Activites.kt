package com.faysal.smsautomation.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "delivered_sms")
data class Activites(
    @PrimaryKey(autoGenerate = true) val activitesId : Int = 0,
    @ColumnInfo(name = "message") val message : String?,
    @ColumnInfo(name = "timestamp") val timestamp : String?,
    @ColumnInfo(name = "status") val status: Boolean?,

)