package com.example.centerinvestcv.utils

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.centerinvestcv.ml.AnalysisFaceDetector
import com.example.centerinvestcv.ml.model.FaceNetModel
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService

object CameraConnectUtils {

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var listener: Listener? = null

    fun attachListener(listener: Listener) {
        this.listener = listener
    }

    fun detachListener() {
        listener = null
    }

    fun Fragment.getPermissions(
        faceNetModel: FaceNetModel,
        cameraExecutor: ExecutorService?,
        previewView: PreviewView
    ) {
        if (allPermissionsGranted()) {
            startCamera(faceNetModel, cameraExecutor, previewView)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    fun Fragment.requestPermissionsResult(
        faceNetModel: FaceNetModel,
        cameraExecutor: ExecutorService?,
        requestCode: Int,
        previewView: PreviewView
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(faceNetModel, cameraExecutor, previewView)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Разрешение не было предоставлено пользователем",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun Fragment.allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    fun Fragment.startCamera(
        faceNetModel: FaceNetModel,
        cameraExecutor: ExecutorService?,
        previewView: PreviewView
    ) {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture?.addListener({
            val cameraProvider = cameraProviderFuture?.get()
            bindPreview(
                faceNetModel,
                cameraProvider,
                cameraExecutor,
                CameraSelector.LENS_FACING_FRONT,
                previewView
            )
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    fun Fragment.bindPreview(
        faceNetModel: FaceNetModel,
        cameraProvider: ProcessCameraProvider?,
        cameraExecutor: ExecutorService?,
        lensFacing: Int,
        previewView: PreviewView
    ) {
        // Preview
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // Select back camera as a default
//        val cameraSelector = CameraSelector.LENS_FACING_FRONT
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
        setFaceDetector(faceNetModel, cameraExecutor, imageAnalysis, lensFacing, previewView)

        try {
            // Unbind use cases before rebinding
            cameraProvider?.unbindAll()

            // Bind use cases to camera
            cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )

        } catch (e: Exception) {
            Log.e("TAG", "Ошибка соединения cameraProvider с жизненным циклом", e)
        }
    }

    fun setFaceDetector(
        faceNetModel: FaceNetModel,
        cameraExecutor: ExecutorService?,
        imageAnalysis: ImageAnalysis,
        lensFacing: Int,
        previewView: PreviewView
    ) {
//        val previewStreamStateObserver = binding?.previewView?.previewStreamState?.observe(this) { streamState ->
//            if (streamState != PreviewView.StreamState.STREAMING) {
//                return@observe
//            }

        var width = previewView.width * previewView.scaleX
        var height = previewView.height * previewView.scaleY
        val rotation = previewView.display.rotation
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            val temp = width
            width = height
            height = temp
        }

        cameraExecutor?.let {
            imageAnalysis.setAnalyzer(
                it,
                createFaceDetector(faceNetModel, width, height, lensFacing)
            )
        }
//            binding?.previewView?.previewStreamState?.removeObserver(previewStreamStateObserver)
//        }
    }

    fun createFaceDetector(
        faceNetModel: FaceNetModel,
        viewfinderWidth: Float,
        viewfinderHeight: Float,
        lensFacing: Int
    ): ImageAnalysis.Analyzer {
        val isFrontLens = lensFacing == CameraSelector.LENS_FACING_FRONT
        val faceDetector = AnalysisFaceDetector(
            previewWidth = viewfinderWidth,
            previewHeight = viewfinderHeight,
            isFrontLens = isFrontLens,
            model = faceNetModel
        )
        faceDetector.listener = object : AnalysisFaceDetector.Listener {
            override fun onFacesDetected(faceBounds: List<RectF>, faces: List<Bitmap>) {
                listener?.drawOverlay(faceBounds)
                listener?.drawFace(faces)
            }

            override fun onUnidentifiedPersonFinded(isUnidentifiedPersonFind: Boolean) {

            }
        }
        return faceDetector
    }

    interface Listener {
        fun drawOverlay(faceBounds: List<RectF>)

        fun drawFace(faces: List<Bitmap>)
    }

    private const val REQUEST_CODE_PERMISSIONS = 10
    private val REQUIRED_PERMISSIONS =
        mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

}



