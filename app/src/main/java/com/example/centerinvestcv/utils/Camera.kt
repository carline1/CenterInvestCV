package com.example.centerinvestcv.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.centerinvestcv.ml.AnalysisFaceDetector
import com.example.centerinvestcv.ml.model.FaceNetModel
import java.util.concurrent.Executors

class Camera(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val faceNetModel: FaceNetModel,
    private val lensFacing: Int,
    private val previewView: View,
    private val surfaceProvider: Preview.SurfaceProvider
) {
    private var listener: CameraListener? = null

    var currentFaces: List<Bitmap>? = null
        private set

    private var width = previewView.width * previewView.scaleX
    private var height = previewView.height * previewView.scaleY
//    private val rotation = previewView.display.rotation

    val faceDetector = AnalysisFaceDetector(
        previewWidth = width,
        previewHeight = height,
        isFrontLens = lensFacing == CameraSelector.LENS_FACING_FRONT,
        model = faceNetModel
    ).apply {
        listener = object : AnalysisFaceDetector.Listener {
            override fun onFacesDetected(faceBounds: List<RectF>, faces: List<Bitmap>) {
                this@Camera.listener?.drawOverlay(faceBounds)
                this@Camera.listener?.drawFace(faces)
                currentFaces = faces
            }
        }
    }

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    init {
//        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
//            val temp = width
//            width = height
//            height = temp
//        }

//        faceDetector = AnalysisFaceDetector(
//            previewWidth = width,
//            previewHeight = height,
//            isFrontLens = lensFacing == CameraSelector.LENS_FACING_FRONT,
//            model = faceNetModel
//        ).apply {
//            listener = object : AnalysisFaceDetector.Listener {
//                override fun onFacesDetected(faceBounds: List<RectF>, faces: List<Bitmap>) {
//                    this@Camera.listener?.drawOverlay(faceBounds)
//                    this@Camera.listener?.drawFace(faces)
//                }
//            }
//        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(surfaceProvider)
            }
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()

        cameraExecutor?.let {
            imageAnalysis.setAnalyzer(
                it,
                faceDetector
            )
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (e: Exception) {
            Log.e("TAG", "Ошибка соединения cameraProvider с жизненным циклом", e)
        }
    }

    fun startCamera() {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(context))
    }

    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    //---------------------------------------------------------------------
    fun attachListener(listener: CameraListener) {
        this.listener = listener
    }

    fun clearCamera() {
        listener = null
        cameraExecutor?.shutdown()
    }

//    fun getPermissions() {
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                context,
//                REQUIRED_PERMISSIONS,
//                REQUEST_CODE_PERMISSIONS
//            )
//        }
//    }

    fun requestPermissionsResult(requestCode: Int) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    context,
                    "Разрешение не было предоставлено пользователем",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    //---------------------------------------------------------------------

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
        fun drawOverlay(faceBounds: List<RectF>)

        fun drawFace(faces: List<Bitmap>)
    }

}