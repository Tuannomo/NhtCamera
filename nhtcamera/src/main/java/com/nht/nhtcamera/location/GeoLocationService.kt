package com.nht.nhtcamera.location

import io.reactivex.Observable

interface GeoLocationService {
    fun isLocationPermissionGranted(): Boolean

    fun fetchLocationSettings(): Observable<GetLocationSettingsResult>

    fun fetchLastLocation(): Observable<GetLocationResult>

    fun fetchCurrentLocation(): Observable<GetLocationResult>
}