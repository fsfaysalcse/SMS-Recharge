package com.faysal.smsautomation.internet

import com.faysal.smsautomation.Models.Posts
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {
    @GET("posts")
    suspend fun getAllPost() : Response<Posts>
}