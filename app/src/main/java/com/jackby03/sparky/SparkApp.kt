package com.jackby03.sparky

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SparkApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Installed as early as possible so even startup (Hilt/Room) crashes are reported.
        if (!isCrashProcess()) CrashHandler.install(base)
    }

    override fun onCreate() {
        // The crash screen lives in the ":crash" process and must stay free of Hilt/Room startup.
        if (isCrashProcess()) return
        super.onCreate()
    }

    private fun isCrashProcess(): Boolean = getProcessName().endsWith(":crash")
}
