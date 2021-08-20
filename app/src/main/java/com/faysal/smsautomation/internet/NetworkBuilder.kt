package com.faysal.smsautomation.internet

import android.content.Context
import android.os.SystemClock
import com.faysal.smsautomation.util.Constants
import com.faysal.smsautomation.util.SharedPref
import com.google.gson.GsonBuilder
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit


object NetworkBuilder {
    val BASE_URL2 = "http://device.dsbpos.com/"

    fun getApiService (context: Context) : ApiService {
        var domain = SharedPref.getString(context,Constants.SHARED_DOMAIN_NAME)
//        val interval = SharedPref.getString(context, Constants.SHARED_INTERVAL).toLong()

        domain = "http://"+domain+"/"


        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.HEADERS

        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 1


        val interceptor: Interceptor = object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                SystemClock.sleep(1000)
                return chain.proceed(chain.request())
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.MINUTES)
            .connectTimeout(30, TimeUnit.MINUTES)
            .addInterceptor(logging)
            .addNetworkInterceptor(interceptor)
            .dispatcher(dispatcher)
            .build()


        return Retrofit.Builder()
            .baseUrl(domain)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build().create(ApiService::class.java)
    }

    fun getAnotherApiService () : ApiService {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.HEADERS

        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.MINUTES)
            .connectTimeout(30, TimeUnit.MINUTES)
            .addInterceptor(logging)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()


        return Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build().create(ApiService::class.java)
    }
}