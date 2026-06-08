package com.example.chatapp

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        lateinit var context: Application
            private set
    }
}
