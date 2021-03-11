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

    @GET("smsReceive")
    suspend fun getVerificationCodeInfo(
        @Query("verificationCode") verificationCode : String
    ) : Response<Verification>



    @GET("api/companyInfo")
    suspend fun getCompanyInfo() : Response<Company>

}