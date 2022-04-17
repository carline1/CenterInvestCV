package com.example.centerinvestcv.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.centerinvestcv.databinding.FragmentMainBinding
import com.example.centerinvestcv.trash.AnalysisFaceDetector
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainFragment : Fragment() {

    private var binding: FragmentMainBinding? = null
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraExecutor: ExecutorService? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        this.binding = binding

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding?.faceContourOverlay?.drawFaceContour(listOf(RectF(3F, 100F, 600F, 600F)))

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture?.addListener({
            val cameraProvider = cameraProviderFuture?.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        // Preview
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding?.previewView?.surfaceProvider)
            }

        // Select back camera as a default
//        val cameraSelector = CameraSelector.LENS_FACING_FRONT
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
        setFaceDetector(imageAnalysis, CameraSelector.LENS_FACING_FRONT)

        try {
            // Unbind use cases before rebinding
            cameraProvider?.unbindAll()

            // Bind use cases to camera
            cameraProvider?.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )

        } catch (exc: Exception) {
            Log.e("TAG", "Use case binding failed", exc)
        }
    }

    private fun setFaceDetector(imageAnalysis: ImageAnalysis, lensFacing: Int) {
//        val previewStreamStateObserver = binding?.previewView?.previewStreamState?.observe(this) { streamState ->
//            if (streamState != PreviewView.StreamState.STREAMING) {
//                return@observe
//            }

        val preview = binding?.previewView!!
        var width = preview.width * preview.scaleX
        var height = preview.height * preview.scaleY
        val rotation = preview.display.rotation
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            val temp = width
            width = height
            height = temp
        }

        cameraExecutor?.let {
            imageAnalysis.setAnalyzer(
                it,
                createFaceDetector(width, height, lensFacing)
            )
        }
//            binding?.previewView?.previewStreamState?.removeObserver(previewStreamStateObserver)
//        }
    }

    private fun createFaceDetector(
        viewfinderWidth: Float,
        viewfinderHeight: Float,
        lensFacing: Int
    ): ImageAnalysis.Analyzer {
        val isFrontLens = lensFacing == CameraSelector.LENS_FACING_FRONT
        val faceDetector = AnalysisFaceDetector(viewfinderWidth, viewfinderHeight, isFrontLens)
        faceDetector.listener = object : AnalysisFaceDetector.Listener {
            override fun onFacesDetected(faceBounds: List<RectF>) {
//                faceBoundsOverlay.post { faceBoundsOverlay.drawFaceBounds(faceBounds) }
                binding?.faceContourOverlay?.drawFaceContour(faceBounds)
            }

//            override fun onError(exception: Exception) {
//                Log.d(TAG, "Face detection error", exception)
//            }
        }
        return faceDetector
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
        binding = null
    }

    companion object {
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

}