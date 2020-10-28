package com.hipo.macaron.imagecropdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hipo.macaron.R
import com.hipo.photocropping.cropImage
import com.hipo.photocropping.getImageBitmapFromExternalFiles
import com.hipo.photocropping.getImageBitmapFromLocal
import com.hipo.photocropping.saveBitmapToExternalFiles
import kotlinx.android.synthetic.main.activity_image_crop.*


class ImageCropActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)
        initUi()
    }

    private fun initUi() {
        imageCropCropButton.setOnClickListener { cropAndSaveImage() }
        loadImage()
    }

    private fun loadImage() {
        val imageUri = intent.getStringExtra(IMAGE_URI_KEY).orEmpty()
        val imageName = intent.getStringExtra(IMAGE_NAME_KEY).orEmpty()

        val bitmap = when {
            imageUri.isBlank().not() -> getImageBitmapFromLocal(contentResolver, imageUri)
            imageName.isBlank().not() -> getImageBitmapFromExternalFiles(this, imageName)
            else -> null
        }
        imageCropPhotoView.setImageBitmap(bitmap)
    }

    private fun cropAndSaveImage() {
        val croppedImage = imageCropPhotoView.cropImage()
        val imageName = "Image${System.currentTimeMillis()}"
        if (croppedImage == null) {
            return
        }
        val isImageSaved = saveBitmapToExternalFiles(
            imageName = imageName,
            imageBitmap = croppedImage,
            context = this
        )

        if (isImageSaved) {
            setResult(
                RESULT_OK,
                Intent().apply { putExtra(SAVED_IMAGE_NAME_KEY, imageName) }
            )
            finish()
        } else {
            Toast.makeText(this, "Image Saving Failed", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        const val IMAGE_NAME_KEY = "image_name"
        const val SAVED_IMAGE_NAME_KEY = "saved_image_name"
        private const val IMAGE_URI_KEY = "image_uri"

        fun newIntent(
            context: Context,
            imageUri: String?,
            imageName: String,
        ): Intent {
            return Intent(context, ImageCropActivity::class.java).apply {
                putExtra(IMAGE_URI_KEY, imageUri)
                putExtra(IMAGE_NAME_KEY, imageName)
            }
        }
    }
}
