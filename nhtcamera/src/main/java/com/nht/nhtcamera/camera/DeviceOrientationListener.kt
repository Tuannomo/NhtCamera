package com.nht.nhtcamera.camera

import android.content.Context
import android.view.OrientationEventListener
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_180
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_270
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_90
import com.nht.nhtcamera.camera.CameraRotationType.Companion.ROTATION_O

class DeviceOrientationListener(
    context: Context
) : OrientationEventListener(context) {

    var rotation = 0
    private var onOrientationChangeListener: OnOrientationChangeListener? = null

    interface OnOrientationChangeListener {
        fun onOrientationChange(rotation: Int)
    }

    override fun onOrientationChanged(orientation: Int) {
        if ((orientation < 35 || orientation > 325) && rotation != ROTATION_O) {
            rotation = ROTATION_O
            onOrientationChangeListener?.onOrientationChange(rotation)
        } else if (orientation in 146..214 && rotation != ROTATION_180) {
            rotation = ROTATION_180
            onOrientationChangeListener?.onOrientationChange(rotation)
        } else if (orientation in 56..124 && rotation != ROTATION_270) {
            rotation = ROTATION_270
            onOrientationChangeListener?.onOrientationChange(rotation)
        } else if (orientation in 236..304 && rotation != ROTATION_90) {
            rotation = ROTATION_90
            onOrientationChangeListener?.onOrientationChange(rotation)
        }
    }

    fun setOnOrientationChangeListener(onOrientationChangeListener: OnOrientationChangeListener) {
        this.onOrientationChangeListener = onOrientationChangeListener
    }
}