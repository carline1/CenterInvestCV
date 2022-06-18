package ru.centerinvest.hidingpersonaldata.ml

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.centerinvest.hidingpersonaldata.ml.model.FaceNetModel
import ru.centerinvest.hidingpersonaldata.utils.BitmapUtils
import kotlin.math.pow
import kotlin.math.sqrt


class AnalysisFaceRecognizer(
    private var model: FaceNetModel
) : ImageAnalysis.Analyzer {

    var recognizeListener: RecognizeListener? = null

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
                CoroutineScope(Dispatchers.Unconfined).launch {
                    runModel(faces, imageBitmap)
                }

                imageProxy.close()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                imageProxy.close()
            }
    }

    private suspend fun runModel(faces: List<Face>, cameraFrameBitmap: Bitmap) {
        withContext(Dispatchers.Unconfined) {
            var unidentifiedPersons = 0
            for (face in faces) {
                try {
                    // Crop the frame using face.boundingBox.
                    // Convert the cropped Bitmap to a ByteBuffer.
                    // Finally, feed the ByteBuffer to the FaceNet model.
                    val croppedBitmap =
                        BitmapUtils.cropRectFromBitmap(cameraFrameBitmap, face.boundingBox)
                    val currentFaceEmbedding = model.getFaceEmbedding(croppedBitmap)

                    // Perform clustering ( grouping )
                    // Store the clusters in a HashMap. Here, the key would represent the 'name'
                    // of that cluster and ArrayList<Float> would represent the collection of all
                    // L2 norms/ cosine distances.
                    if (faceList.isEmpty()) {
                        unidentifiedPersons = 1
                    } else {
                        for (i in 0 until faceList.size) {
                            // If this cluster ( i.e an ArrayList with a specific key ) does not exist,
                            // initialize a new one.
                            if (nameScoreHashmap[faceList[i].first] == null) {
                                // Compute the L2 norm and then append it to the ArrayList.
                                val currentFaceScore = ArrayList<Float>()
                                if (metricToBeUsed == "cosine") {
                                    currentFaceScore.add(cosineSimilarity(currentFaceEmbedding, faceList[i].second))
                                } else {
                                    currentFaceScore.add(L2Norm(currentFaceEmbedding, faceList[i].second))
                                }
                                nameScoreHashmap[faceList[i].first] = currentFaceScore
                            }
                            // If this cluster exists, append the L2 norm/cosine score to it.
                            else {
                                if (metricToBeUsed == "cosine") {
                                    nameScoreHashmap[faceList[i].first]?.add(
                                        cosineSimilarity(
                                            currentFaceEmbedding,
                                            faceList[i].second
                                        )
                                    )
                                } else {
                                    nameScoreHashmap[faceList[i].first]?.add(
                                        L2Norm(
                                            currentFaceEmbedding,
                                            faceList[i].second
                                        )
                                    )
                                }
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
                            unidentifiedPersons += 1
                            recognizeListener?.onUnidentifiedPersonFinded(true)
                            "Unknown"
                        }
                    } else {
                        // In case of L2 norm, choose the lowest value.
                        if (avgScores.minOrNull()!! > model.model.l2Threshold) {
                            recognizeListener?.onUnidentifiedPersonFinded(true)
                            unidentifiedPersons += 1
                            "Unknown"
                        } else {
                            names[avgScores.indexOf(avgScores.minOrNull()!!)]
                        }
                    }
                    Log.d("TAG", "Person identified as $bestScoreUserName")
                } catch (e: Exception) {
                    // If any exception occurs with this box and continue with the next boxes.
                    Log.e("TAG", "Exception in FrameAnalyser : ${e.localizedMessage}")
                    continue
                }
            }
            if (faces.isNotEmpty() && unidentifiedPersons == 0) {
                recognizeListener?.onUnidentifiedPersonFinded(false)
            } else {
                recognizeListener?.onUnidentifiedPersonFinded(true)
            }
        }
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

    interface RecognizeListener {
        fun onUnidentifiedPersonFinded(isUnidentifiedPersonFind: Boolean)
    }

}