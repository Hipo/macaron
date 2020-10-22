package com.hipo.biometricutils

import androidx.biometric.BiometricConstants.ERROR_CANCELED
import androidx.biometric.BiometricConstants.ERROR_HW_NOT_PRESENT
import androidx.biometric.BiometricConstants.ERROR_HW_UNAVAILABLE
import androidx.biometric.BiometricConstants.ERROR_LOCKOUT_PERMANENT
import androidx.biometric.BiometricConstants.ERROR_NO_BIOMETRICS
import androidx.biometric.BiometricConstants.ERROR_UNABLE_TO_PROCESS
import androidx.biometric.BiometricConstants.ERROR_USER_CANCELED
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.showBiometricAuthentication(
    titleText: String,
    descriptionText: String? = null,
    negativeButtonText: String,
    successCallback: (() -> Unit)? = null,
    failCallBack: (() -> Unit)? = null,
    hardwareErrorCallback: (() -> Unit)? = null
) {
    if (hasDeviceBiometric().not()) {
        hardwareErrorCallback?.invoke()
        return
    }

    val biometricExecutor = { command: Runnable ->
        try {
            command.run()
        } catch (exception: Exception) {
            // TODO log to crashlytics
            exception.printStackTrace()
        }
    }

    val biometricAuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        @Suppress("ComplexCondition")
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            if (errorCode == ERROR_HW_NOT_PRESENT ||
                errorCode == ERROR_HW_UNAVAILABLE ||
                errorCode == ERROR_LOCKOUT_PERMANENT ||
                errorCode == ERROR_CANCELED ||
                errorCode == ERROR_UNABLE_TO_PROCESS ||
                errorCode == ERROR_NO_BIOMETRICS
            ) {
                hardwareErrorCallback?.invoke()
            } else if (errorCode == ERROR_USER_CANCELED) {
                failCallBack?.invoke()
            }
            // TODO Handle ERROR_NEGATIVE_BUTTON
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            successCallback?.invoke()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            failCallBack?.invoke()
        }
    }

    val biometricPromptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(titleText)
            .setNegativeButtonText(negativeButtonText)
            .also {
                if (descriptionText != null) it.setDescription(descriptionText)
            }
            .build()
    }

    try {
        BiometricPrompt(this, biometricExecutor, biometricAuthenticationCallback)
            .authenticate(biometricPromptInfo)
    } catch (exception: Exception) {
        exception.printStackTrace()
    }
}
