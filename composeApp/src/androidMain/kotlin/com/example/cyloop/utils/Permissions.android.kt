package com.example.cyloop.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.cyloop.MainActivity

actual fun hasCameraPermission(): Boolean {
    val activity = MainActivity.currentActivity ?: return false
    return ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

actual fun requestCameraPermission() {
    val activity = MainActivity.currentActivity ?: return
    activity.requestPermissions(
        arrayOf(Manifest.permission.CAMERA),
        1001
    )
}

actual fun openAppSettings() {
    val activity = MainActivity.currentActivity ?: return
    val intent = android.content.Intent(
        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        android.net.Uri.fromParts("package", activity.packageName, null)
    )
    activity.startActivity(intent)
}
