package com.example

import android.app.Application
import com.example.data.di.CricketContainer

class CricTaughtApplication : Application() {
    lateinit var container: CricketContainer

    override fun onCreate() {
        super.onCreate()
        container = CricketContainer(this)
    }
}
