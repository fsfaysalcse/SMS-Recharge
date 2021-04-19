package com.faysal.smsautomation.ui

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.faysal.smsautomation.R
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import com.faysal.smsautomation.util.Util

class ActivityPermission : AppCompatActivity() {


    private val PERMISSION_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        if(checkPermission())gotoVerifyActivity()

        val allowBtn = findViewById<Button>(R.id.allowBtn)
        allowBtn.setOnClickListener {
            permissionOperation()
        }



    }

    private fun permissionOperation() {
        if (!checkPermission()) {
            requestPermission();
        } else {
            gotoVerifyActivity()
        }
    }


    private fun checkPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_SMS)
        val result1 =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.SEND_SMS)
        val result2 =
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_PHONE_STATE
            )
        val result3 =
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.RECEIVE_SMS
            )
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                val readSmsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val sendSmsAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val phoneStateAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED
                val recivedSmsAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED
                if (readSmsAccepted && sendSmsAccepted && phoneStateAccepted && recivedSmsAccepted)
                    gotoVerifyActivity()
                else {

                    Util.showAlertMessage(applicationContext,"You need to allow access to both the permissions")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS) &&
                            shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS) &&
                            shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) &&
                            shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)
                        ) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            arrayOf(
                                                Manifest.permission.READ_SMS,
                                                Manifest.permission.SEND_SMS,
                                                Manifest.permission.READ_PHONE_STATE,
                                                Manifest.permission.RECEIVE_SMS
                                            ),
                                            PERMISSION_REQUEST_CODE
                                        )
                                    }
                                })
                            return
                        }
                    }
                }
            }
        }
    }

    private fun gotoVerifyActivity() {
        SharedPref.putBoolean(this,Constants.PERMISSION_GRANTED,true)
        startActivity(Intent(applicationContext,AcitivtyDomainVerifiy::class.java))
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}