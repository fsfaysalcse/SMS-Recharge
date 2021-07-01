package com.faysal.smsautomation.util

import android.R
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*


object Util {
    fun showAlertMessage(context: Context, message: String) {
        Toasty.info(context, message, Toast.LENGTH_SHORT, true).show();
    }

    fun showSuccessMessage(context: Context, message: String) {
        Toasty.success(context, message, Toast.LENGTH_SHORT, true).show();
    }

    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this)
            .matches()
    }

    fun String.isUrlValid(): Boolean {
        return URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this).matches()
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                //It will check for both wifi and cellular network
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    fun currentTimeStamp() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
}