package com.nht.nhtcamera

import android.app.Application

class CameraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        cameraApplication = this

    }

    companion object {
        var cameraApplication: CameraApplication? = null
            private set
        private val TAG = CameraApplication::class.java.simpleName
    }
}