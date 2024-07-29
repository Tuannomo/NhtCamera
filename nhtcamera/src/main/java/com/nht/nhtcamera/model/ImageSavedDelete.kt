package com.nht.nhtcamera.model

import com.nht.nhtcamera.model.ImageSaved

data class ImageSavedDelete(
    var posDelete: Int,
    var imageSaved: List<ImageSaved>
)