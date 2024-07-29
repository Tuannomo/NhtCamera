package com.nht.nhtcamera.camera

import androidx.annotation.IntDef
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_180
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_270
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_90
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_O

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    ROTATION_O,
    ROTATION_90,
    ROTATION_180,
    ROTATION_270
)
annotation class CameraRotationType {
    companion object {
        const val ROTATION_O = 0
        const val ROTATION_90 = 1
        const val ROTATION_180 = 2
        const val ROTATION_270 = 3
    }
}
