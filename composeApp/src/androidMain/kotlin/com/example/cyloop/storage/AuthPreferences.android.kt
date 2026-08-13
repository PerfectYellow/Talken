package com.example.cyloop.storage

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.cyloop.MainActivity

actual fun authenticateBiometrics(
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val activity = MainActivity.currentActivity ?: return
    val executor = ContextCompat.getMainExecutor(activity)
    
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFailure()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailure()
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric login for CyLoop")
        .setSubtitle("Log in using your biometric credential")
        .setNegativeButtonText("Use account password")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

actual suspend fun getLegacyPassword(context: Any?): String? {
    return null
}
