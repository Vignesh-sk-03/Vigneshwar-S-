package com.example.data.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.remote.CricketApiService
import com.example.data.remote.SimulatedCricketApi
import com.example.data.repository.CricketRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val cricketRepository: CricketRepository
}

class CricketContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    private val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(context)
    }

    // Swappable API implementation
    // Set to true to use real Retrofit, false to use the rich high-fidelity simulated ticker
    private val useRealApi = false

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.cricapi.com/v1/") // Standard base URL for CricAPI
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val apiService: CricketApiService by lazy {
        if (useRealApi) {
            retrofit.create(CricketApiService::class.java)
        } else {
            SimulatedCricketApi()
        }
    }

    override val cricketRepository: CricketRepository by lazy {
        CricketRepository(
            cricketDao = database.cricketDao(),
            apiService = apiService,
            settingsDataStore = settingsDataStore
        )
    }
}
