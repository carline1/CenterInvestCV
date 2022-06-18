package com.example.centerinvestcv.ui.fragments.face_add

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.centerinvestcv.R
import com.example.centerinvestcv.databinding.FaceAddFragmentBinding
import com.example.centerinvestcv.ui.common.views.CustomAlertDialog
import com.example.centerinvestcv.utils.FullScreenStateChanger
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity
import ru.centerinvest.hidingpersonaldata.di.RoomFaceComponentViewModel
import ru.centerinvest.hidingpersonaldata.ml.model.FaceNetModel
import ru.centerinvest.hidingpersonaldata.ml.model.TensorflowModels
import ru.centerinvest.hidingpersonaldata.utils.Camera


class FaceAddFragment : Fragment(), Camera.CameraListener {

    private var _binding: FaceAddFragmentBinding? = null
    private val binding get() = _binding!!

    private val roomFaceComponentViewModel: RoomFaceComponentViewModel by viewModels()
    private val viewModel: FaceAddViewModel by viewModels {
        FaceAddViewModel.Factory(roomFaceComponentViewModel.roomFaceComponent.roomFaceRepository)
    }

    private lateinit var faceNetModel: FaceNetModel
    private val lensFacing = CameraSelector.LENS_FACING_FRONT

    private var camera: Camera? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        faceNetModel = FaceNetModel(
            context = requireContext(),
            model = TensorflowModels.FACENET,
            useGpu = false,
            useXNNPack = true
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FaceAddFragmentBinding.inflate(inflater, container, false)
        setUpUI()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        setObservers()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            Camera.REQUIRED_PERMISSIONS,
            Camera.REQUEST_CODE_PERMISSIONS
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == Camera.REQUEST_CODE_PERMISSIONS) {
            if (camera?.allPermissionsGranted(requireContext()) == true) {
                camera?.startCamera(Camera.FaceAnalyzerType.Detect)
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.permission_was_not_granted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setUpUI() {
        binding.previewView.doOnLayout {
            camera = Camera(
                requireContext(),
                this,
                faceNetModel,
                lensFacing,
                it,
                binding.previewView.surfaceProvider
            )
            if (camera?.allPermissionsGranted(requireContext()) == true) {
                camera?.startCamera(Camera.FaceAnalyzerType.Detect)
            } else {
                requestPermissions()
            }
            camera?.attachListener(this)

            viewModel.loadAllFaceEntities()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.actionButton.setOnClickListener {
            if (camera?.currentFace != null) {
                val face = camera?.currentFace
                val alert = LayoutInflater.from(requireContext())
                    .inflate(R.layout.custom_alert_dialog, binding.root, false) as CustomAlertDialog

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
                            )
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

    private fun setObservers() {
        viewModel.saveFaceToDatabaseMLD.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.face_saved_successfully),
                    Toast.LENGTH_LONG
                ).show()
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.loadAllFaceEntitiesMLD.observe(viewLifecycleOwner) { result ->
            result.onSuccess { list ->
                val faceList = ArrayList(list.map { (it.id.toString() to it.imageData) })
                camera?.addFaceRecognizerList(faceList)
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.faces_not_loading),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun drawOverlay(faceBounds: List<RectF>) {
        binding.faceContourOverlay.drawFaceContour(faceBounds)
    }

    override fun drawFace(face: Bitmap?) {
        binding.faceImage.setImageBitmap(face)
    }

    override fun onResume() {
        super.onResume()
        FullScreenStateChanger.fullScreen(activity as AppCompatActivity, true)
    }

    override fun onStop() {
        super.onStop()
        FullScreenStateChanger.fullScreen(activity as AppCompatActivity, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.clearCamera()
        _binding = null
    }

}