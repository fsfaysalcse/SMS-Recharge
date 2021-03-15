package com.faysal.smsautomation.internet

import com.faysal.smsautomation.Models.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Query

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
    suspend fun getDomainInfo(
        @Query("domain") domain : String
    ) : Response<Domain>

    @GET("api-getnotice")
    suspend fun getNotice(
        @Query("show") show : String = "recharge"
    ) : Response<Notice>

    @GET("smsReceive")
    suspend fun getVerificationCodeInfo(
        @Query("verificationCode") verificationCode : String
    ) : Response<Verification>


    @GET("api/companyInfo")
    suspend fun getCompanyInfo() : Response<Company>


    // Send Sms to server

    @GET("smsReceive")
    suspend fun sendSmsToServer(
        @Query("service") service : Int,
        @Query("verifycode") verifycode : Int,
        @Query("sender") sender : String?,
        @Query("simNo") simNo : String?,
        @Query("datetime") datetime : String?,
        @Query("smsBody") smsBody : String?,
    ) : Response<SaveSms>

}