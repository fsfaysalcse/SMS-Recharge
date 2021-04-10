package com.faysal.smsautomation.util

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi

object SimUtil {
    private const val TAG = "SimUtil"
    fun sendSMS(phoneNo: String, msg: String, context: Context) : Boolean{
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            val sim1Switch = SharedPref.getBoolean(context, Constants.SHARED_SIM_1_ACTIVE)
            val sim2Switch = SharedPref.getBoolean(context, Constants.SHARED_SIM_2_ACTIVE)

            if (sim1Switch && sim2Switch){
                val randoms = (Math.random() * 2).toInt()
                return sendingSms(phoneNo, msg, randoms)
            }else if(sim1Switch){
                return sendingSms(phoneNo, msg, 0)
            }else if (sim2Switch){
                return sendingSms(phoneNo, msg, 1)
            }else{
                return false
            }



        }
        return false
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    fun sendingSms(phoneNumber: String, message: String, slot: Int) : Boolean {
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            SmsManager.getSmsManagerForSubscriptionId(slot)
                .sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            Log.d(TAG, "sendSMS: sms has been send")

            return true
        } catch (ex: Exception) {
            Log.d(TAG, "sendSMS: Failed to send sme")
            ex.printStackTrace()
            return false
        }
    }


}