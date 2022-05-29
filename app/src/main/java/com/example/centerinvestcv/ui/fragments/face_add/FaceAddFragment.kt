package com.example.centerinvestcv.ui.fragments.face_add

import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.centerinvestcv.R
import com.example.centerinvestcv.databinding.FaceAddFragmentBinding
import com.example.centerinvestcv.ui.common.views.CustomAlertDialog
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity
import ru.centerinvest.hidingpersonaldata.di.RoomFaceComponentViewModel
import ru.centerinvest.hidingpersonaldata.ml.model.FaceNetModel
import ru.centerinvest.hidingpersonaldata.ml.model.TensorflowModels
import ru.centerinvest.hidingpersonaldata.utils.Camera


class FaceAddFragment : Fragment(), Camera.CameraListener {

    private var _binding: FaceAddFragmentBinding? = null
    private val binding get() = _binding!!

    private val roomFaceComponentViewModel: RoomFaceComponentViewModel by viewModels()
    private val viewModel: FaceAddViewModel by viewModels() {
        FaceAddViewModel.Factory(roomFaceComponentViewModel.roomFaceComponent.roomFaceRepository)
    }

    private lateinit var faceNetModel: FaceNetModel
    private val lensFacing = CameraSelector.LENS_FACING_FRONT

    private var camera: Camera? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FaceAddFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUI()

        faceNetModel = FaceNetModel(
            context = requireContext(),
            model = TensorflowModels.FACENET,
            useGpu = true,
            useXNNPack = true
        )

        binding.previewView.doOnLayout {
            camera = Camera(
                requireActivity(),
                this,
                faceNetModel,
                lensFacing,
                it,
                binding.previewView.surfaceProvider
            )
            if (allPermissionsGranted()) {
                camera?.startCamera(Camera.FaceAnalizerType.Detect)
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
                    val faceList = ArrayList(list.map { (it.id.toString() to it.imageData) })
                    camera?.faceRecognizer?.faceList = faceList
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
        if (requestCode == Camera.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                camera?.startCamera(Camera.FaceAnalizerType.Detect)
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.permission_was_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun allPermissionsGranted() = Camera.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpUI() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.actionButton.setOnClickListener {
            if (camera?.currentFaces?.isNotEmpty() == true) {
                val face = camera?.currentFaces?.get(0)
                val alert = LayoutInflater.from(requireContext())
                    .inflate(R.layout.custom_alert_dialog, null, false) as CustomAlertDialog

                alert.showDialog(
                    getString(R.string.saving_face),
                    getString(R.string.cancel),
                    getString(R.string.save),
                    { cancel() },
                ) {
                    if (alert.editText?.text.toString().isNotBlank()) {
                        face?.let {
                            viewModel.saveFaceToDatabase(
                                FaceEntity(
                                    name = alert.editText?.text.toString(),
                                    imageData = faceNetModel.getFaceEmbedding(it)
                                )
                            ).subscribeOn(Schedulers.io()).subscribe()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.face_saved_successfully),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        cancel()
                        binding.root.findNavController().popBackStack()
                    } else {
                        alert.editText?.error = getString(R.string.field_cannot_be_empty)
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.face_not_recognized),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun drawOverlay(faceBounds: List<RectF>) {
        binding.faceContourOverlay.drawFaceContour(faceBounds)
    }

    override fun drawFace(faces: List<Bitmap>) {
        binding.faceImage.setImageBitmap(if (faces.isNotEmpty()) faces[0] else null)
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.clearCamera()
        _binding = null
    }

}