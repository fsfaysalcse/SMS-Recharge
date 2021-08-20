package com.faysal.smsautomation.ui

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.faysal.smsautomation.Models.Verification
import com.faysal.smsautomation.util.Util
import com.faysal.smsautomation.databinding.ActivitySecretKeyVerificationBinding
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class SecretKeyVerification : AppCompatActivity() {

    lateinit var binding: ActivitySecretKeyVerificationBinding
    lateinit var apiService: ApiService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecretKeyVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = NetworkBuilder.getApiService(applicationContext)

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

            apiService.getVerificationCodeInfo(verificationCode).enqueue(object : Callback<Verification>{
                override fun onResponse(
                    call: Call<Verification>,
                    response: Response<Verification>
                ) {
                    if (response.isSuccessful) {

                        if (response.body()?.status == 1) {
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
                            )}
                        }
                    dialog.dismiss()
                }

                override fun onFailure(call: Call<Verification>, t: Throwable) {
                    dialog.dismiss()
                    Util.showAlertMessage(
                        applicationContext,
                        t.message.toString()
                    )
                }

            })


        }
    }
}