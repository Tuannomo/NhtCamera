package com.nht.nhtcamera.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.nht.nhtcamera.CameraApplication
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class GeoLocationServiceImpl : GeoLocationService {

    private var googlePlayServicesUtils = GooglePlayServicesUtils()

    override fun isLocationPermissionGranted(): Boolean {
        val context = CameraApplication.cameraApplication
        return if (context != null) {
            (googlePlayServicesUtils.isGooglePlayServicesAvailable()
                    && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            false
        }
    }

    override fun fetchLocationSettings(): Observable<GetLocationSettingsResult> {
        return Observable.create { source ->
            val context = CameraApplication.cameraApplication
            if (context != null) {
                val request = LocationRequest.create()
                    .setNumUpdates(1)
                    .setExpirationDuration(10000)

                val builder = LocationSettingsRequest
                    .Builder()
                    .addLocationRequest(request)

                LocationServices.getSettingsClient(context)
                    .checkLocationSettings(builder.build())
                    .addOnSuccessListener {
                        source.onNext(GetLocationSettingsResult.Success)
                        source.onComplete()
                    }
                    .addOnCanceledListener {
                        source.onNext(GetLocationSettingsResult.Cancelled)
                        source.onComplete()
                    }
                    .addOnFailureListener {
                        source.onNext(GetLocationSettingsResult.Failure)
                        source.onComplete()
                    }
            } else {
                source.onNext(GetLocationSettingsResult.Failure)
                source.onComplete()
            }
        }
    }

    @SuppressLint("MissingPermission", "VisibleForTests")
    override fun fetchLastLocation(): Observable<GetLocationResult> {
        return Observable.create { source ->
            val context = CameraApplication.cameraApplication
            if (context != null) {
                val fusedLocationProviderClient = FusedLocationProviderClient(context)
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (null != location) {
                            source.onNext(GetLocationResult.Success(location))
                            source.onComplete()
                        } else {
                            source.onNext(GetLocationResult.Failure)
                            source.onComplete()
                        }
                    }
                    .addOnFailureListener {
                        source.onNext(GetLocationResult.Failure)
                        source.onComplete()
                    }
            } else {
                source.onNext(GetLocationResult.Failure)
                source.onComplete()
            }
        }
    }

    @SuppressLint("MissingPermission", "VisibleForTests")
    override fun fetchCurrentLocation(): Observable<GetLocationResult> {
        return Observable.create { source ->
            val context = CameraApplication.cameraApplication
            if (context != null) {
                val request = LocationRequest.create()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(1000)
                    .setInterval(1000)
                    .setExpirationDuration(10000)

                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        if (result.locations.isEmpty()) {
                            source.onNext(GetLocationResult.Failure)
                            source.onComplete()
                        } else {
                            val lastLocation = result.locations.last()
                            source.onNext(GetLocationResult.Success(lastLocation))
                            source.onComplete()
                        }
                    }
                }

                val fusedLocationProviderClient = FusedLocationProviderClient(context)
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                )
            } else {
                source.onNext(GetLocationResult.Failure)
                source.onComplete()
            }
        }.timeout(600, TimeUnit.MILLISECONDS).onErrorReturnItem(GetLocationResult.TimeOut)
    }
}