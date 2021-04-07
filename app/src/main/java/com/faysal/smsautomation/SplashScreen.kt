package com.faysal.smsautomation

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref


class SplashScreen : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGTH = 3000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val isLogedIn = SharedPref.getBoolean(applicationContext,Constants.IS_LOGED_IN)


        Handler().postDelayed(Runnable {
            if (isLogedIn){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, AcitivtyVerifiy::class.java))
                finish()
            }

        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}