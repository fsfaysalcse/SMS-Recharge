package com.faysal.smsautomation.ui

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.faysal.smsautomation.util.Util
import com.faysal.smsautomation.databinding.ActivitySecretKeyVerificationBinding
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.lang.Exception

class SecretKeyVerification : AppCompatActivity() {

    lateinit var binding: ActivitySecretKeyVerificationBinding
    lateinit var apiService: ApiService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretKeyVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = NetworkBuilder.getApiService()

        binding.btnSave.setOnClickListener {


            val verificationCode = binding.etVerificationCode.text.toString().trim()


            if (Util.isOnline(this) == false) {
                Util.showAlertMessage(
                    applicationContext,
                    "Internet connection not found !"
                )
                return@setOnClickListener
            }

            if (verificationCode.isNullOrEmpty()) {
                Util.showAlertMessage(
                    applicationContext,
                    "Please enter verification code."
                )
                return@setOnClickListener
            }

            var dialog: AlertDialog =
                SpotsDialog.Builder().setContext(this).setMessage("PLEASE WAIT")
                    .setCancelable(false)
                    .build()
            dialog.show()


            GlobalScope.launch {
                supervisorScope {
                    try {
                        val responseVerificationCode =
                            async { apiService.getVerificationCodeInfo(verificationCode) }.await()
                        if (responseVerificationCode.isSuccessful) {

                            if (responseVerificationCode.body()?.status == 1) {
                                SharedPref.putString(
                                    applicationContext,
                                    Constants.SHARED_VERIFICATION_CODE,
                                    verificationCode
                                )
                                SharedPref.putBoolean(
                                    applicationContext,
                                    Constants.IS_LOGED_IN,
                                    true
                                )
                                startActivity(
                                    Intent(
                                        this@SecretKeyVerification,
                                        MainActivity::class.java
                                    )
                                )
                                finish()
                            }else {
                                Util.showAlertMessage(
                                    applicationContext,
                                    "Invalid Verification Code"
                                )
                            }
                        }

                        dialog.dismiss()

                    } catch (e: Exception) {
                        dialog.dismiss()
                        Util.showAlertMessage(
                            applicationContext,
                            e.message.toString()
                        )
                    }
                }

            }

        }
    }
}