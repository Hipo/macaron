package com.hipo.biometricutils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE

fun Context.hasDeviceBiometric(): Boolean {
    val canAuthResult = BiometricManager.from(this).canAuthenticate()
    return (canAuthResult == BIOMETRIC_ERROR_NONE_ENROLLED || canAuthResult == BIOMETRIC_ERROR_NO_HARDWARE).not()
}
