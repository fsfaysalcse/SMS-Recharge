package com.faysal.smsautomation.internet

import com.faysal.smsautomation.Models.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {



    @GET("api-getoperatorservice")
    suspend fun getServices(
        @Query("list") list : String = "all"
    ) : Response<Service>


    @GET("api/interval")
    suspend fun getInterval(
        @Query("type") type : String = "sms"
    ) : Response<Interval>


    @GET("api-getdomain")
    fun getDomainInfo(
        @Query("domain") domain : String
    ) : Call<Domain>

    @GET("api-getnotice")
    suspend fun getNotice(
        @Query("show") show : String = "recharge"
    ) : Response<Notice>

    @GET("smsReceive")
    fun getVerificationCodeInfo(
        @Query("verificationCode") verificationCode : String
    ) : Call<Verification>

    @GET
    fun getOutSms(@Url url: String) : Call<OutSms>



    @GET("api/companyInfo")
    suspend fun getCompanyInfo() : Response<Company>



    @GET("/smsApi/pendingSms")
    fun getOutgoingMessages(
        @Query("verifycode") verifycode : Int
    ) : Call<OutgoingMessages>



    // Send Sms to server

    @GET("smsApi/smsReceive")
    fun sendSmsToServer(
        @Query("service") service : Int,
        @Query("verifycode") verifycode : Int,
        @Query("sender") sender : String?,
        @Query("simNo") simNo : String?,
        @Query("datetime") datetime : String?,
        @Query("smsBody") smsBody : String?
    ) : Call<SaveSms>




}