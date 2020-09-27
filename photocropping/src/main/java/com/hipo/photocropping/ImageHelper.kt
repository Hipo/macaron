package com.hipo.photocropping

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.core.content.FileProvider

class ImageHelper {

    /**
    Returns an Intent Chooser

    @param chooserTitle This res id will be used to show title on intent chooser
    @param fileAuth This param should be same as `authorities` param in AndroidManifest.xml
    To make them same, it can be created as;
    manifestPlaceholders = [FILES_AUTHORITY:"${applicationId}.provider"]
    buildConfigField "String", "FILES_AUTHORITY", "\"${applicationId}.provider\""
    and called BuildConfig.FILES_AUTHORITY
     **/
    fun getImageChooserIntent(
        context: Context,
        @StringRes chooserTitle: Int,
        fileAuth: String
    ): Intent {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        return try {
            val imageFile = createImageFile(context)
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
}
