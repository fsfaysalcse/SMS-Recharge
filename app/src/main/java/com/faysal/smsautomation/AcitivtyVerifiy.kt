package com.faysal.smsautomation

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.faysal.smsautomation.databinding.AcitivtyDomainVerificationBinding
import com.faysal.smsautomation.internet.ApiService
import com.faysal.smsautomation.internet.NetworkBuilder
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import dmax.dialog.SpotsDialog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import retrofit2.Retrofit
import java.lang.Exception

class AcitivtyVerifiy : AppCompatActivity() {


    lateinit var binding: AcitivtyDomainVerificationBinding
    lateinit var api2Service: ApiService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivtyDomainVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        api2Service = NetworkBuilder.getAnotherApiService()

        binding.btnSave.setOnClickListener {

            val domainName = binding.etDomainName.text.toString().trim()
            if (domainName.isNullOrEmpty()) {
                Util.showAlertMessage(
                    binding.root,
                    "Please enter domain name."
                )
                return@setOnClickListener
            }

            var dialog: AlertDialog =
                SpotsDialog.Builder().setContext(this).setMessage("PLEASE WAIT").setCancelable(false)
                    .build()
            dialog.show()
            GlobalScope.launch{
                supervisorScope {
                    try {
                        val responseDomain = async { api2Service.getDomainInfo(domainName) }.await()
                        if (responseDomain.isSuccessful) {
                            if (responseDomain.body()?.status == "1") {
                                SharedPref.putString(applicationContext, Constants.SHARED_DOMAIN_NAME, domainName)
                                startActivity(
                                    Intent(
                                        this@AcitivtyVerifiy,
                                        SecretKeyVerification::class.java
                                    )
                                )
                            } else {
                                Util.showAlertMessage(
                                    binding.root,
                                    "Invalid Domain"
                                )

                            }
                        }
                        dialog.dismiss()

                    } catch (e: Exception) {
                        dialog.dismiss()
                        Util.showAlertMessage(
                            binding.root,
                            e.message.toString()
                        )
                    }
                }

            }

        }
    }
}