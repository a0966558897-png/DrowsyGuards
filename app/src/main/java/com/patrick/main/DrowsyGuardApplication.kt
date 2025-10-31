package com.patrick.main

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DrowsyGuardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化應用程序
    }
}
