package com.hipo.macaron

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hipo.macaron.imagecropdemo.ImageCropMainActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUi()
    }

    private fun initUi() {
        mainPhotoCropDemoButton.setOnClickListener { onImageCropButtonClick() }
    }

    private fun onImageCropButtonClick() {
        startActivity(ImageCropMainActivity.newIntent(this))
    }
}
