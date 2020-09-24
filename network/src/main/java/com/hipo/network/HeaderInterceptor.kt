package com.hipo.network

import android.os.Build
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
    private val packageName: String, // = BuildConfig.APPLICATION_ID
    private val appVersion: String, // = BuildConfig.VERSION_NAME
    private vararg val otherHeaders: Pair<String, String>,
    private val appName: String? = null,
    private val clientType: String = DEFAULT_CLIENT_TYPE,
    private val osVersion: String = Build.VERSION.SDK_INT.toString(),
    private val deviceModel: String = Build.MODEL
) : Interceptor {

    private val defaultAppName = packageName.split('.')
        .run { elementAtOrNull(THIRD_ITEM_INDEX) ?: elementAtOrNull(SECOND_ITEM_INDEX).orEmpty() }.capitalize()

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.run {
            proceed(
                request()
                    .newBuilder()
                    .addHeader(KEY_APP_NAME, appName ?: defaultAppName)
                    .addHeader(KEY_CLIENT_TYPE, clientType)
                    .addHeader(KEY_DEVICE_OS_VERSION, osVersion)
                    .addHeader(KEY_APP_PACKAGE_NAME, packageName)
                    .addHeader(KEY_APP_VERSION, appVersion)
                    .addHeader(KEY_DEVICE_MODEL, deviceModel)
                    .apply {
                        otherHeaders.forEach { header ->
                            addHeader(header.first, header.second)
                        }
                    }.build()
            )
        }
    }

    companion object {
        private const val KEY_APP_NAME = "App-Name"
        private const val KEY_CLIENT_TYPE = "Client-Type"
        private const val KEY_DEVICE_OS_VERSION = "Device-OS-Version"
        private const val KEY_APP_PACKAGE_NAME = "App-Package-Name"
        private const val KEY_APP_VERSION = "App-Version"
        private const val KEY_DEVICE_MODEL = "Device-Model"

        private const val DEFAULT_CLIENT_TYPE = "android"
        private const val SECOND_ITEM_INDEX = 1
        private const val THIRD_ITEM_INDEX = 2
    }
}
