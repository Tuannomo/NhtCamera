package com.nht.nhtcamera.location

import io.reactivex.Observable

class LocationProvider {

    private var geoLocationService: GeoLocationService = GeoLocationServiceImpl()

    fun fetchDeviceLocation(): Observable<GetLocationResult> {
        return if (!geoLocationService.isLocationPermissionGranted()) {
            Observable.just(GetLocationResult.Failure)
        } else {
            handleLocationSettingsResult(geoLocationService.fetchLocationSettings())
        }
    }

    private fun handleLocationSettingsResult(locationSettingsResult: Observable<GetLocationSettingsResult>): Observable<GetLocationResult> {
        return locationSettingsResult.flatMap {
            return@flatMap when (it) {
                is GetLocationSettingsResult.Success -> {
                    val result = geoLocationService.fetchLastLocation()
                    handleLastLocationResult(result)
                }
                else -> {
                    Observable.just(GetLocationResult.Failure)
                }
            }
        }
    }

    private fun handleLastLocationResult(locationResult: Observable<GetLocationResult>): Observable<GetLocationResult> {
        return locationResult.flatMap {
            return@flatMap when (it) {
                is GetLocationResult.Success -> {
                    Observable.just(GetLocationResult.Success(it.location))
                }
                else -> {
                    val result = geoLocationService.fetchCurrentLocation()
                    handleCurrentLocationResult(result)
                }
            }
        }
    }

    private fun handleCurrentLocationResult(locationResult: Observable<GetLocationResult>): Observable<GetLocationResult> {
        return locationResult.flatMap {
            return@flatMap when (it) {
                is GetLocationResult.Success -> {
                    Observable.just(GetLocationResult.Success(it.location))
                }
                is GetLocationResult.TimeOut -> {
                    Observable.just(GetLocationResult.TimeOut)
                }
                else -> {
                    Observable.just(GetLocationResult.Failure)
                }
            }
        }
    }
}