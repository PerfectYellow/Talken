package com.example.cyloop.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.cyloop.MainActivity

object PermissionUtils {
    fun hasCameraPermission(): Boolean {
        val activity = MainActivity.currentActivity ?: return false
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission() {
        val activity = MainActivity.currentActivity ?: return
        activity.requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            1001
        )
    }
}
