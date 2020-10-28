package com.hipo.macaron.imagecropdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hipo.macaron.BuildConfig
import com.hipo.macaron.R
import com.hipo.photocropping.getImageBitmapFromExternalFiles
import com.hipo.photocropping.getImageChooserIntent
import kotlinx.android.synthetic.main.activity_image_crop_main.*

class ImageCropMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop_main)
        initUi()
    }

    private fun initUi() {
        imageCropMainEditButton.setOnClickListener { onEditClick() }
    }

    private fun onEditClick() {
        val chooserIntent = getImageChooserIntent(
            context = this,
            chooserTitle = R.string.select_an_image,
            fileAuth = BuildConfig.FILES_AUTHORITY,
            fileName = IMAGE_NAME
        )
        startActivityForResult(chooserIntent, IMAGE_PICK_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            IMAGE_PICK_RESULT_CODE -> onImagePickSuccess(data?.dataString)
            IMAGE_SAVE_RESULT_CODE -> {
                onImageCropSuccess(data?.getStringExtra(ImageCropActivity.SAVED_IMAGE_NAME_KEY))
            }
        }
    }

    private fun onImagePickSuccess(imageUri: String?) {
        val imageCropIntent = ImageCropActivity.newIntent(
            context = this,
            imageUri = imageUri,
            imageName = IMAGE_NAME,
        )
        startActivityForResult(imageCropIntent, IMAGE_SAVE_RESULT_CODE)
    }

    private fun onImageCropSuccess(imageName: String?) {
        val imageBitmap = getImageBitmapFromExternalFiles(this, imageName.orEmpty())
        imageCropMainImageView.setImageBitmap(imageBitmap)
    }

    companion object {
        private const val IMAGE_PICK_RESULT_CODE = 1002
        private const val IMAGE_SAVE_RESULT_CODE = 1001
        private const val IMAGE_NAME = "test-image-name"

        fun newIntent(context: Context): Intent {
            return Intent(context, ImageCropMainActivity::class.java)
        }
    }
}
