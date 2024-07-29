package com.nht.nhtcamera.utils

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.CheckResult
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

class AlertDialogFactory {

    @CheckResult
    fun getAlertDialog(
        context: Context,
        @StringRes messageStringRes: Int,
        @StringRes positiveButtonStringRes: Int,
        @StringRes negativeButtonStringRes: Int,
        positiveListener: DialogInterface.OnClickListener?
    ): AlertDialog {
        return AlertDialog.Builder(context)
            .setMessage(messageStringRes)
            .setPositiveButton(positiveButtonStringRes, positiveListener)
            .setNegativeButton(negativeButtonStringRes, null)
            .create()
    }
}