package com.example.centerinvestcv.ui.fragments.face_add

import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.centerinvestcv.databinding.FragmentFaceAddBinding
import com.example.centerinvestcv.db.dao.FaceEntity
import com.example.centerinvestcv.ml.model.FaceNetModel
import com.example.centerinvestcv.ml.model.TensorflowModels
import com.example.centerinvestcv.utils.Camera
import io.reactivex.rxjava3.schedulers.Schedulers

class FaceAddFragment : Fragment(), Camera.CameraListener {

    private var _binding: FragmentFaceAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FaceAddViewModel by viewModels()

    private lateinit var faceNetModel: FaceNetModel
    private val lensFacing = CameraSelector.LENS_FACING_FRONT

    private var camera: Camera? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFaceAddBinding.inflate(inflater, container, false)

        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setUpUI()

        faceNetModel = FaceNetModel(
            context = requireContext(),
            model = TensorflowModels.FACENET,
            useGpu = true,
            useXNNPack = true
        )
//        getPermissions(faceNetModel, cameraExecutor, binding.previewView)
        binding.previewView.doOnLayout {
            camera = Camera(
                requireActivity(),
                this,
                faceNetModel,
                lensFacing,
                it,
                binding.previewView.surfaceProvider
            )
            if (camera?.allPermissionsGranted() == true) {
                camera?.startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    Camera.REQUIRED_PERMISSIONS,
                    Camera.REQUEST_CODE_PERMISSIONS
                )
            }
            camera?.attachListener(this)

            viewModel.loadAllFaceEntities()
                .subscribeOn(Schedulers.io())
                .subscribe({ list ->
//                it.forEach { face ->
//                    Log.d("TAG", face.faceImage.asList().toString())
//                }

                    val faceList = ArrayList(list.map { (it.id.toString() to it.faceImage) })
                    camera?.faceDetector?.faceList = faceList
                }, {
                    Log.d("TAG", "GG")
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        camera?.requestPermissionsResult(requestCode)
//        requestPermissionsResult(
//            faceNetModel,
//            cameraExecutor,
//            requestCode,
//            binding.previewView
//        )
    }

    private fun setUpUI() {
        binding.actionButton.setOnClickListener {
//            viewModel.currentFace.value?.let {
//                viewModel.saveFaceToDatabase(
//                    FaceEntity(
//                        faceImage = faceNetModel.getFaceEmbedding(it)
//                    )
//                ).subscribeOn(Schedulers.io()).subscribe()
//                Toast.makeText(requireContext(), "Лицо успешно сохранено", Toast.LENGTH_LONG).show()
//            } ?: Toast.makeText(requireContext(), "Лицо не распознано", Toast.LENGTH_LONG).show()
            camera?.currentFaces?.get(0)?.let {
                viewModel.saveFaceToDatabase(
                    FaceEntity(
                        faceImage = faceNetModel.getFaceEmbedding(it)
                    )
                ).subscribeOn(Schedulers.io()).subscribe()
                Toast.makeText(requireContext(), "Лицо успешно сохранено", Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(requireContext(), "Лицо не распознано", Toast.LENGTH_LONG).show()
        }
    }

    private fun setObservers() {
//        viewModel.overlayRectList.observe(viewLifecycleOwner) {
//            binding.faceContourOverlay.drawFaceContour(it)
//        }
//        viewModel.currentFace.observe(viewLifecycleOwner) {
//            binding.faceImage.setImageBitmap(it)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.clearCamera()
    }

    override fun drawOverlay(faceBounds: List<RectF>) {
        binding.faceContourOverlay.drawFaceContour(faceBounds)
    }

    override fun drawFace(faces: List<Bitmap>) {
        binding.faceImage.setImageBitmap(if (faces.isNotEmpty()) faces[0] else null)
    }

}