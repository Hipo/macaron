package com.hipo.photocropping

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.core.graphics.drawable.toBitmap
import com.github.chrisbanes.photoview.PhotoView

fun PhotoView.cropImage(): Bitmap? {
    drawable?.toBitmap()?.let { imageBitmap ->
        val matrix = android.graphics.Matrix()
        getDisplayMatrix(matrix)
        val matrixRect = RectF()
        matrixRect.set(
            0f, 0f,
            drawable.intrinsicWidth.toFloat(),
            drawable.intrinsicHeight.toFloat()
        )
        matrix.mapRect(matrixRect)
        val rect = RectF()
        val scale = scale
        rect.top = displayRect.top / scale * -1
        rect.left = displayRect.left / scale * -1
        rect.bottom = if (rect.top < 0) displayRect.bottom / scale else height / scale
        rect.right = if (rect.left < 0) displayRect.right / scale else width / scale
        val smallestEdgeLength = if (imageBitmap.width.toFloat() > imageBitmap.height.toFloat()) {
            imageBitmap.height
        } else {
            imageBitmap.width
        }
        val scaleFactorForOriginal = width.toFloat() / smallestEdgeLength
        return Bitmap.createBitmap(
            imageBitmap,
            (rect.left.toInt() / scaleFactorForOriginal).toInt(),
            (rect.top.toInt() / scaleFactorForOriginal).toInt(),
            (rect.right.toInt() / scaleFactorForOriginal).toInt(),
            (rect.right.toInt() / scaleFactorForOriginal).toInt()
        )
    }
    return null
}
