package com.hipo.photocropping

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.IOException

/**
 * When user clicks on Intent Chooser, creates new file for camera to write on.
 * If there is already created file in the Pictures folder for profile picture,
 * overwrites it to avoid stacking unreachable image files.
 */
@Throws(IOException::class)
fun createImageFile(context: Context, postfix: String = BuildConfig.LIBRARY_PACKAGE_NAME): File {
    return File(getImageFilePath(context, postfix))
}

fun getImageFilePath(context: Context, postfix: String = BuildConfig.LIBRARY_PACKAGE_NAME): String {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return "$storageDir/${File.separator}/$postfix"
}
