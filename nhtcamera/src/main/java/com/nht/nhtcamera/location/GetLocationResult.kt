package com.nht.nhtcamera.location

import android.location.Location

sealed class GetLocationResult {
    data class Success(val location: Location) : GetLocationResult()

    object TimeOut : GetLocationResult()
    object Failure : GetLocationResult()
}
