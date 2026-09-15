package com.codeforcesvisualizer

import android.content.Context
import com.codeforcesvisualizer.inject.initKoin

class Application : android.app.Application() {
    companion object {
        @JvmStatic
        lateinit var context: Context
    }
    override fun onCreate() {
        super.onCreate()
        context = this
        initKoin()
    }
}
