package com.hipo.photocropping

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val DEFAULT_IMAGE_QUALITY = 80

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

/**
 * Returns an Intent Chooser
 *
 * @param chooserTitle This res id will be used to show title on intent chooser.
 * @param fileAuth This param should be same as `authorities` param in AndroidManifest.xml
 * To make them same, it can be created as;
 * manifestPlaceholders = [FILES_AUTHORITY:"${applicationId}.provider"]
 * buildConfigField "String", "FILES_AUTHORITY", "\"${applicationId}.provider\""
 * and called BuildConfig.FILES_AUTHORITY
 * @param fileName image file name to be created for camera intent.
 *
 * **/
fun getImageChooserIntent(
    context: Context,
    @StringRes chooserTitle: Int,
    fileAuth: String,
    fileName: String?
): Intent {
    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    return try {
        val imageFile = createImageFile(context, fileName ?: BuildConfig.LIBRARY_PACKAGE_NAME)
        val imageUri = FileProvider.getUriForFile(context, fileAuth, imageFile)
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            resolveActivity(context.packageManager)
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        val chooserIntent =
            Intent.createChooser(galleryIntent, context.getString(chooserTitle))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(captureIntent))

        chooserIntent
    } catch (e: Exception) {
        galleryIntent
    }
}

/**
 * Returns image that saved in app's external files
 *
 * @param context Needed to create new File.
 * @param imageName Name of the image file that saved in app's external files.
 */
fun getImageBitmapFromExternalFiles(
    context: Context,
    imageName: String = BuildConfig.LIBRARY_PACKAGE_NAME
): Bitmap? {
    return BitmapFactory.decodeFile(getImageFilePath(context, imageName))
}

/**
 * Returns image bitmap from local storage
 *
 * @param contentResolver Needed to get image from local storage
 * @param imageUri Image uri path
 */
fun getImageBitmapFromLocal(contentResolver: ContentResolver, imageUri: String?): Bitmap? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        ImageDecoder.createSource(contentResolver, Uri.parse(imageUri)).let { bitmapSrc ->
            ImageDecoder.decodeBitmap(bitmapSrc)
        }
    } else {
        MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(imageUri))
    }
}

/**
 * Creates new image file with given bitmap,
 * saves into app's external files and returns Boolean based on success state
 *
 * @param imageName Name of the image file to be saved.
 * @param imageBitmap Bitmap to be saved.
 * @param context Will be used to create new image file.
 * @param imageQuality Image quality of image to be saved. Default is 80.
 */

fun saveBitmapToExternalFiles(
    imageName: String?,
    imageBitmap: Bitmap?,
    context: Context?,
    imageQuality: Int = DEFAULT_IMAGE_QUALITY
): Boolean {
    if (context == null || imageName == null || imageBitmap == null) {
        return false
    }
    return try {
        val imageFile = createImageFile(context, imageName)
        val fileOutputStream = FileOutputStream(imageFile)

        imageBitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, fileOutputStream)
        fileOutputStream.apply {
            flush()
            close()
        }
        true
    } catch (exception: Exception) {
        false
    }
}
