package com.faysal.smsautomation.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.faysal.smsautomation.R
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref


class SplashScreen : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGTH = 3000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val isLoggedIn = SharedPref.getBoolean(applicationContext,Constants.IS_LOGED_IN)
        val isPermissionGranted = SharedPref.getBoolean(applicationContext,Constants.PERMISSION_GRANTED)


        Handler().postDelayed(Runnable {
            if (isLoggedIn && isPermissionGranted){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, ActivityPermission::class.java))
                finish()
            }

        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}