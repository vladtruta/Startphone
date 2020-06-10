package com.vladtruta.startphone.util

import android.app.Application

class StartphoneApp: Application(){
    companion object {
        lateinit var instance: StartphoneApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}