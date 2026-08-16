package com.example.cyloop.utils

import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.requestAccessForMediaType

actual fun hasCameraPermission(): Boolean {
    return AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized
}

actual fun requestCameraPermission() {
    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { _ -> }
}

actual fun openAppSettings() {
    val settingsUrl = platform.Foundation.NSURL.URLWithString(platform.UIKit.UIApplicationOpenSettingsURLString)
    if (settingsUrl != null) {
        platform.UIKit.UIApplication.sharedApplication.openURL(settingsUrl)
    }
}
