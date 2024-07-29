package com.nht.nhtcamera.model

import android.graphics.Bitmap

data class ImageSaved(
    var imgBitmap: Bitmap,
    var imgPath: String = "",
    var isSelected: Boolean = false,
    var rotation: Float = 0.0f,
    var mLongitude: Double = 0.0,
    var mLatitude: Double = 0.0,
    var dateCaptured: String = "",
    var imgNameByDate: String = "",
    var mLongitudeGallery: String = "",
    var mLongitudeRefGallery: String = "",
    var mLatitudeGallery: String = "",
    var mLatitudeRefGallery: String = "",
    var deviceName: String = ""
)