package com.example

import android.app.Application
import com.example.di.AppContainer

class MediaHubApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
