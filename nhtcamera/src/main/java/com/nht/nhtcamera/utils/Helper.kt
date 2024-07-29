package com.nht.nhtcamera.utils

import java.text.SimpleDateFormat
import java.util.Locale


/**
 * Created by majidabdul on 05/02/2018.
 */
class Helper {
    companion object {

        fun getImgCapturedNameByDate(): String {
            return SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
        }

        fun getImgCapturedDate(): String {
            return SimpleDateFormat(
                "dd-MM-yyyy HH:mm",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
        }
    }

}