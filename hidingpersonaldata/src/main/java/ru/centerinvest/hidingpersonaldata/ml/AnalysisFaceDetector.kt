package ru.centerinvest.hidingpersonaldata.ml

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import ru.centerinvest.hidingpersonaldata.utils.BitmapUtils


class AnalysisFaceDetector(
    private val previewWidth: Float,
    private val previewHeight: Float,
    private val isFrontLens: Boolean
) : ImageAnalysis.Analyzer {

    var detectListener: DetectListener? = null

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)
        val imageBitmap = BitmapUtils.imageToBitmap(mediaImage, rotation)

        val realTimeOpts = FaceDetectorOptions.Builder()
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

        val detector = FaceDetection.getClient(realTimeOpts)
        detector.process(image)
            .addOnSuccessListener { faces ->
                val listener = detectListener ?: return@addOnSuccessListener
                val reverseDimens = rotation == 90 || rotation == 270
                val width = if (reverseDimens) imageProxy.height else imageProxy.width
                val height = if (reverseDimens) imageProxy.width else imageProxy.height

                val faceBounds = faces.map { it.boundingBox.transform(width, height) }
                val faceBitmaps =
                    faces.map { BitmapUtils.cropRectFromBitmap(imageBitmap, it.boundingBox) }
                listener.onFacesDetected(faceBounds, faceBitmaps)
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                imageProxy.close()
            }
    }

    private fun Rect.transform(width: Int, height: Int): RectF {
        val scaleX = previewWidth / width
        val scaleY = previewHeight / height

        val flippedLeft = if (isFrontLens) width - right else left
        val flippedRight = if (isFrontLens) width - left else right

        val scaledLeft = scaleX * flippedLeft
        val scaledTop = scaleY * top
        val scaledRight = scaleX * flippedRight
        val scaledBottom = scaleY * bottom
        return RectF(scaledLeft, scaledTop, scaledRight, scaledBottom)
    }

    interface DetectListener {
        fun onFacesDetected(faceBounds: List<RectF>, faces: List<Bitmap>)
    }
}