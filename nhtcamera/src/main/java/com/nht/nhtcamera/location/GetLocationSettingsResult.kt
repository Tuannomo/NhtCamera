package com.nht.nhtcamera.location

sealed class GetLocationSettingsResult {

    object Success : GetLocationSettingsResult()

    object Cancelled : GetLocationSettingsResult()

    object Failure : GetLocationSettingsResult()
}
