package com.baize.common_lib

import android.app.Application

class App : Application() {
    companion object {
        var context: Application? = null
        fun getApplication(): Application {
            return context ?: App()
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

}