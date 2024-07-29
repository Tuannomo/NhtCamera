package com.nht.nhtcamera

import android.content.Context
import com.nht.nhtcamera.camera.CameraMainScreen

class CameraManager {
    fun initializeCamera(context: Context, isRequireLocation: Boolean = false) {
        // Implementation code
        CameraMainScreen.startInstance(context, isRequireLocation)
    }

    fun startCamera() {
        // Implementation code
    }

}