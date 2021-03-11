package com.faysal.smsautomation

import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import com.google.android.material.snackbar.Snackbar


object Util {
    fun showAlertMessage(view: View, message: String) {
        Snackbar.make(
            view,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this)
            .matches()
    }

    fun String.isUrlValid(): Boolean {
        return URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this).matches()
    }
}