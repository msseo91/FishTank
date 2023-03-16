package com.marine.fishtank.api

import com.marine.fishtank.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitBuilder {
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .also {
                if(BuildConfig.DEBUG) {
                    it.client(
                        OkHttpClient().newBuilder().addInterceptor(HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.HEADERS
                        }).build()
                    )
                }
            }
            .build()
    }

    val fishService: FishService = getRetrofit().create(FishService::class.java)
}