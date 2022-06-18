package ru.centerinvest.hidingpersonaldata.utils

import android.graphics.*
import android.media.Image
import java.io.ByteArrayOutputStream


object BitmapUtils {

    fun cropRectFromBitmap(source: Bitmap, rect: Rect): Bitmap {
        val left = if (source.width - rect.right >= 0) source.width - rect.right else 0
        val top = if (rect.top >= 0) rect.top else 0
        val width = if ((left + rect.width()) <= source.width) rect.width() else source.width - left
        val height = if ((rect.top + rect.height()) <= source.height) rect.height() else source.height - rect.top

        return Bitmap.createBitmap(source, left, top, width, height)
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)

        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, false)
    }

    private fun flipBitmap(source: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postScale(-1f, 1f, source.width / 2f, source.height / 2f)

        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun imageToBitmap(image: Image, rotationDegrees: Int): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val yuv = out.toByteArray()
        var output = BitmapFactory.decodeByteArray(yuv, 0, yuv.size)
        output = rotateBitmap(output, rotationDegrees.toFloat())

        return flipBitmap(output)
    }

}