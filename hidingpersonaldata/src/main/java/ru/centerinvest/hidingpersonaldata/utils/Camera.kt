package ru.centerinvest.hidingpersonaldata.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import ru.centerinvest.hidingpersonaldata.ml.AnalysisFaceDetector
import ru.centerinvest.hidingpersonaldata.ml.AnalysisFaceRecognizer
import ru.centerinvest.hidingpersonaldata.ml.model.FaceNetModel
import java.util.concurrent.Executors

class Camera(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    faceNetModel: FaceNetModel,
    private val lensFacing: Int,
    previewView: View,
    private val surfaceProvider: Preview.SurfaceProvider
) {

    private var listener: CameraListener? = null

    var currentFace: Bitmap? = null
        private set

    private var width = previewView.width * previewView.scaleX
    private var height = previewView.height * previewView.scaleY

    private val faceRecognizer = AnalysisFaceRecognizer(
        model = faceNetModel
    ).apply {
        recognizeListener = object : AnalysisFaceRecognizer.RecognizeListener {
            override fun onUnidentifiedPersonFinded(isUnidentifiedPersonFind: Boolean) {
                this@Camera.listener?.hideData(isUnidentifiedPersonFind)
            }
        }
    }

    private val faceDetector = AnalysisFaceDetector(
        previewWidth = width,
        previewHeight = height,
        isFrontLens = lensFacing == CameraSelector.LENS_FACING_FRONT
    ).apply {
        detectListener = object : AnalysisFaceDetector.DetectListener {
            override fun onFacesDetected(faceBounds: List<RectF>, faces: List<Bitmap>) {
                this@Camera.listener?.drawOverlay(faceBounds)
                this@Camera.listener?.drawFace(if (faces.isNotEmpty()) faces[0] else null)
                currentFace = if (faces.isNotEmpty()) faces[0] else null
            }
        }
    }

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    fun attachListener(listener: CameraListener) {
        this.listener = listener
    }

    fun clearCamera() {
        listener = null
        cameraExecutor?.shutdown()
    }

    fun addFaceRecognizerList(faceList: ArrayList<Pair<String, FloatArray>>) {
        faceRecognizer.faceList = faceList
    }

    private fun bindPreview(
        cameraProvider: ProcessCameraProvider,
        faceAnalyzerType: FaceAnalyzerType
    ) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(surfaceProvider)
            }
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()

        val faceAnalyzer = when (faceAnalyzerType) {
            FaceAnalyzerType.Detect -> faceDetector
            FaceAnalyzerType.Recognize -> faceRecognizer
        }

        cameraExecutor?.let {
            imageAnalysis.setAnalyzer(
                it,
                faceAnalyzer
            )
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (e: Exception) {
            Log.e("TAG", "Ошибка соединения cameraProvider с жизненным циклом", e)
        }
    }

    fun startCamera(faceAnalyzerType: FaceAnalyzerType) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider, faceAnalyzerType)
        }, ContextCompat.getMainExecutor(context))
    }

    fun allPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    interface CameraListener {
        fun drawOverlay(faceBounds: List<RectF>): Unit? = null
        fun drawFace(face: Bitmap?): Unit? = null
        fun hideData(hide: Boolean): Unit? = null
    }

    enum class FaceAnalyzerType {
        Detect,
        Recognize
    }

}