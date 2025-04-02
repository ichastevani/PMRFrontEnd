package com.example.myapplication.network

import com.example.myapplication.data.AppContainer
import com.example.myapplication.data.NetworkPMRRepository
import com.example.myapplication.data.PMRRepository
import com.example.myapplication.utils.ConstUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DefaultAppContainer : AppContainer {
  val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
  }

  val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .build()

  private val retrofit = Retrofit.Builder()
    .baseUrl(ConstUtil.apiBackend)
    .addConverterFactory(GsonConverterFactory.create())
    .client(okHttpClient)
    .build()

  private val retrofitService: PMRApiService by lazy {
    retrofit.create(PMRApiService::class.java)
  }

  override val pmrRepository: PMRRepository by lazy {
    NetworkPMRRepository(retrofitService)
  }

}