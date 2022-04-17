package com.example.centerinvestcv.trash

import android.annotation.SuppressLint
import android.gesture.Prediction
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.centerinvestcv.model.FaceNetModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt


class AnalysisFaceDetector(
    private val previewWidth: Float,
    private val previewHeight: Float,
    private val isFrontLens: Boolean,
    private var model: FaceNetModel
) : ImageAnalysis.Analyzer {

    var listener: Listener? = null

    var faceList = ArrayList<Pair<String, FloatArray>>()
    private val nameScoreHashmap = HashMap<String, ArrayList<Float>>()
    private val metricToBeUsed = "l2"

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

//        if (mediaImage != null) {
        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        // Real-time contour detection
//        val realTimeOpts = FaceDetectorOptions.Builder()
//            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
//            .build()
        val realTimeOpts = FaceDetectorOptions.Builder()
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

//            val options = FaceDetectorOptions.Builder()
//                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
//                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
//                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
//                .enableTracking()
//                .build()

        val detector: FaceDetector = FaceDetection.getClient(realTimeOpts)
        detector.process(image)
            .addOnSuccessListener { faces ->
                val listener = listener ?: return@addOnSuccessListener
                val reverseDimens = rotation == 90 || rotation == 270
                val width = if (reverseDimens) imageProxy.height else imageProxy.width
                val height = if (reverseDimens) imageProxy.width else imageProxy.height

                val faceBounds = faces.map { it.boundingBox.transform(width, height) }
                listener.onFacesDetected(faceBounds)
                imageProxy.close()
//                processFaceContourDetectionResult(faces)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                e.printStackTrace()
                imageProxy.close()
            }
//        }
    }

    private suspend fun runModel(faces: List<Face>, cameraFrameBitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val predictions = ArrayList<Prediction>()
            for (face in faces) {
                try {
                    // Crop the frame using face.boundingBox.
                    // Convert the cropped Bitmap to a ByteBuffer.
                    // Finally, feed the ByteBuffer to the FaceNet model.
                    val croppedBitmap = cropRectFromBitmap(cameraFrameBitmap, face.boundingBox)
                    val subject = model.getFaceEmbedding(croppedBitmap)


                    // Perform clustering ( grouping )
                    // Store the clusters in a HashMap. Here, the key would represent the 'name'
                    // of that cluster and ArrayList<Float> would represent the collection of all
                    // L2 norms/ cosine distances.
                    for (i in 0 until faceList.size) {
                        // If this cluster ( i.e an ArrayList with a specific key ) does not exist,
                        // initialize a new one.
                        if (nameScoreHashmap[faceList[i].first] == null) {
                            // Compute the L2 norm and then append it to the ArrayList.
                            val p = ArrayList<Float>()
                            if (metricToBeUsed == "cosine") {
                                p.add(cosineSimilarity(subject, faceList[i].second))
                            } else {
                                p.add(L2Norm(subject, faceList[i].second))
                            }
                            nameScoreHashmap[faceList[i].first] = p
                        }
                        // If this cluster exists, append the L2 norm/cosine score to it.
                        else {
                            if (metricToBeUsed == "cosine") {
                                nameScoreHashmap[faceList[i].first]?.add(
                                    cosineSimilarity(
                                        subject,
                                        faceList[i].second
                                    )
                                )
                            } else {
                                nameScoreHashmap[faceList[i].first]?.add(
                                    L2Norm(
                                        subject,
                                        faceList[i].second
                                    )
                                )
                            }
                        }
                    }

                    // Compute the average of all scores norms for each cluster.
                    val avgScores = nameScoreHashmap.values.map { scores ->
                        scores.toFloatArray().average()
                    }
                    Log.d("TAG", "Average score for each user : $nameScoreHashmap")

                    val names = nameScoreHashmap.keys.toTypedArray()
                    nameScoreHashmap.clear()

                    // Calculate the minimum L2 distance from the stored average L2 norms.
                    val bestScoreUserName: String = if (metricToBeUsed == "cosine") {
                        // In case of cosine similarity, choose the highest value.
                        if (avgScores.maxOrNull()!! > model.model.cosineThreshold) {
                            names[avgScores.indexOf(avgScores.maxOrNull()!!)]
                        } else {
                            "Unknown"
                        }
                    } else {
                        // In case of L2 norm, choose the lowest value.
                        if (avgScores.minOrNull()!! > model.model.l2Threshold) {
                            "Unknown"
                        } else {
                            names[avgScores.indexOf(avgScores.minOrNull()!!)]
                        }
                    }
                    Log.d("TAG", "Person identified as $bestScoreUserName")
                    predictions.add(
                        Prediction(
                            face.boundingBox,
                            bestScoreUserName
                        )
                    )
                } catch (e: Exception) {
                    // If any exception occurs with this box and continue with the next boxes.
                    Log.e("Model", "Exception in FrameAnalyser : ${e.message}")
                    continue
                }
            }
            withContext(Dispatchers.Main) {
                // Clear the BoundingBoxOverlay and set the new results ( boxes ) to be displayed.
                boundingBoxOverlay.faceBoundingBoxes = predictions
                boundingBoxOverlay.invalidate()
            }
        }
    }

    fun cropRectFromBitmap(source: Bitmap, rect: Rect ): Bitmap {
        var width = rect.width()
        var height = rect.height()
        if ( (rect.left + width) > source.width ){
            width = source.width - rect.left
        }
        if ( (rect.top + height ) > source.height ){
            height = source.height - rect.top
        }
        val croppedBitmap = Bitmap.createBitmap( source , rect.left , rect.top , width , height )
        // Uncomment the below line if you want to save the input image.
        // BitmapUtils.saveBitmap( context , croppedBitmap , "source" )
        return croppedBitmap
    }

    // Compute the L2 norm of ( x2 - x1 )
    private fun L2Norm(x1: FloatArray, x2: FloatArray): Float {
        return sqrt(x1.mapIndexed { i, xi -> (xi - x2[i]).pow(2) }.sum())
    }


    // Compute the cosine of the angle between x1 and x2.
    private fun cosineSimilarity(x1: FloatArray, x2: FloatArray): Float {
        val mag1 = sqrt(x1.map { it * it }.sum())
        val mag2 = sqrt(x2.map { it * it }.sum())
        val dot = x1.mapIndexed { i, xi -> xi * x2[i] }.sum()
        return dot / (mag1 * mag2)
    }

    private fun Rect.transform(width: Int, height: Int): RectF {
//        val scaleX = previewWidth / width.toFloat()
//        val scaleY = previewHeight / height.toFloat()
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

//    private fun processFaceContourDetectionResult(faces: List<Face>) {
//        // Task completed successfully
//        if (faces.isEmpty()) {
////            Toast.makeText(context, "No face found", Toast.LENGTH_LONG).show()
//            return
//        }
////        mGraphicOverlay.clear()
////        for (i in faces.indices) {
////            val face: Face = faces[i]
////            val faceGraphic = FaceContourGraphic(mGraphicOverlay)
////            mGraphicOverlay.add(faceGraphic)
////            faceGraphic.updateFace(face)
////        }
//    }

    interface Listener {
        fun onFacesDetected(faceBounds: List<RectF>)

//        fun onError(exception: Exception)
    }
}