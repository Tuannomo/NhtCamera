package com.nht.nhtcamera.location

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.nht.nhtcamera.CameraApplication

class GooglePlayServicesUtils {
    fun isGooglePlayServicesAvailable(): Boolean {
        val context = CameraApplication.cameraApplication
        return if (context != null) {
            GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        } else {
            false
        }
    }
}