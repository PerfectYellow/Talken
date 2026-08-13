package com.example.cyloop.storage

import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class)
actual fun authenticateBiometrics(
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val context = LAContext()
    
    if (context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)) {
        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            "Log in using your biometric credential",
            { success, _ ->
                if (success) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
        )
    } else {
        onFailure()
    }
}

actual suspend fun getLegacyPassword(context: Any?): String? {
    return null
}
