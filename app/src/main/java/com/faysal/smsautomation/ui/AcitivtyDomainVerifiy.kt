package com.faysal.smsautomation.ui

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.faysal.smsautomation.Models.Domain
import com.faysal.smsautomation.util.Util
import com.faysal.smsautomation.util.Util.isUrlValid
import com.faysal.smsautomation.databinding.AcitivtyDomainVerificationBinding
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

class AcitivtyDomainVerifiy : AppCompatActivity() {


    lateinit var binding: AcitivtyDomainVerificationBinding
    lateinit var api2Service: ApiService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivtyDomainVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        api2Service = NetworkBuilder.getAnotherApiService()

        binding.btnSave.setOnClickListener {

            var domainName = binding.etDomainName.text.toString().trim()


            if (!Util.isOnline(this)) {
                Util.showAlertMessage(
                    applicationContext,
                    "Internet connection not found !"
                )
                return@setOnClickListener
            }

            if (domainName.isNullOrEmpty()) {
                Util.showAlertMessage(
                    applicationContext,
                    "Please enter domain name."
                )
                return@setOnClickListener
            }



            if (!domainName.contains(".")) {
                domainName = "$domainName.eflexi.xyz"
            }



            var dialog: AlertDialog =
                SpotsDialog.Builder().setContext(this).setMessage("PLEASE WAIT")
                    .setCancelable(false)
                    .build()
            dialog.show()

            api2Service.getDomainInfo(domainName).enqueue(object : Callback<Domain>{
                override fun onResponse(call: Call<Domain>, response: Response<Domain>) {
                    if (response.isSuccessful) {
                        if (response.body()?.status == "1") {
                            SharedPref.putString(
                                applicationContext,
                                Constants.SHARED_DOMAIN_NAME,
                                domainName
                            )
                            startActivity(
                                Intent(
                                    this@AcitivtyDomainVerifiy,
                                    SecretKeyVerification::class.java
                                )
                            )
                        } else {
                            Util.showAlertMessage(
                                applicationContext,
                                "Invalid Domain"
                            )

                        }
                    }
                    dialog.dismiss()
                }

                override fun onFailure(call: Call<Domain>, t: Throwable) {
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