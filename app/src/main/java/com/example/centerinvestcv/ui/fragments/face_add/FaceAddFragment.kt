package com.example.centerinvestcv.ui.fragments.face_add

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.example.centerinvestcv.db.dao.FaceEntity
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.centerinvest.hidingpersonaldata.ml.model.FaceNetModel
import ru.centerinvest.hidingpersonaldata.ml.model.TensorflowModels
import ru.centerinvest.hidingpersonaldata.utils.Camera


class FaceAddFragment : Fragment(), Camera.CameraListener {

    private var _binding: FaceAddFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FaceAddViewModel by viewModels()

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
//                it.forEach { face ->
//                    Log.d("TAG", face.faceImage.asList().toString())
//                }

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
                    "Разрешение не было предоставлено пользователем",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
//        requestPermissionsResult(
//            faceNetModel,
//            cameraExecutor,
//            requestCode,
//            binding.previewView
//        )
    }

    private fun allPermissionsGranted() = Camera.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpUI() {
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.actionButton.setOnClickListener {
//            viewModel.currentFace.value?.let {
//                viewModel.saveFaceToDatabase(
//                    FaceEntity(
//                        faceImage = faceNetModel.getFaceEmbedding(it)
//                    )
//                ).subscribeOn(Schedulers.io()).subscribe()
//                Toast.makeText(requireContext(), "Лицо успешно сохранено", Toast.LENGTH_LONG).show()
//            } ?: Toast.makeText(requireContext(), "Лицо не распознано", Toast.LENGTH_LONG).show()
            if (camera?.currentFaces?.isNotEmpty() == true) {
                val face = camera?.currentFaces?.get(0)

//                val editText = EditText(requireContext())
//                val saveFaceAlertBuilder: AlertDialog.Builder = AlertDialog.Builder(
//                    ContextThemeWrapper(
//                        requireContext(),
//                        R.style.AlertDialogCustom
//                    )
//                )
//                    .setTitle("Сохранение лица")
//                    .setView(editText)
//                    .setPositiveButton("Сохранить", null)
//                    .setNegativeButton("Отмена") { dialog, _ ->
//                        dialog.cancel()
//                    }
//                val saveFaceAlertDialog = saveFaceAlertBuilder.create()
//                saveFaceAlertDialog.show()
//
//                saveFaceAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
//                    if (editText.text.toString().isNotBlank()) {
//                        face?.let {
//                            viewModel.saveFaceToDatabase(
//                                FaceEntity(
//                                    name = editText.toString(),
//                                    imageData = faceNetModel.getFaceEmbedding(it)
//                                )
//                            ).subscribeOn(Schedulers.io()).subscribe()
//                            Toast.makeText(requireContext(), "Лицо успешно сохранено", Toast.LENGTH_LONG)
//                                .show()
//                        }
//                        saveFaceAlertDialog.cancel()
//                        findNavController().popBackStack()
//                    } else {
//                        editText.error = "Поле не может быть пустым"
//                    }
//                }
                val saveFaceAlertDialog = AlertDialog.Builder(requireContext()).create()
                val saveFaceAlertDialogLayout = LayoutInflater.from(requireContext())
                    .inflate(R.layout.custom_alert_dialog, null)
                saveFaceAlertDialog.apply {
                    setView(saveFaceAlertDialogLayout)
                    setCancelable(false)
                }
                saveFaceAlertDialogLayout.let { layout ->
                    val editText = layout.findViewById<EditText>(R.id.editText)
                    layout.findViewById<TextView>(R.id.title).text = "Сохранение лица"
                    layout.findViewById<TextView>(R.id.negativeButton).apply {
                        text = "Отмена"
                        setOnClickListener { saveFaceAlertDialog.cancel() }
                    }
                    layout.findViewById<TextView>(R.id.positiveButton).apply {
                        text = "Сохранить"
                        setOnClickListener {
                            if (editText.text.toString().isNotBlank()) {
                                face?.let {
                                    viewModel.saveFaceToDatabase(
                                        FaceEntity(
                                            name = editText.text.toString(),
                                            imageData = faceNetModel.getFaceEmbedding(it)
                                        )
                                    ).subscribeOn(Schedulers.io()).subscribe()
                                    Toast.makeText(
                                        requireContext(),
                                        "Лицо успешно сохранено",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                saveFaceAlertDialog.cancel()
                                binding.root.findNavController().popBackStack()
                            } else {
                                editText.error = "Поле не может быть пустым"
                            }
                        }
                    }
                }
                saveFaceAlertDialog.show()

            } else {
                Toast.makeText(requireContext(), "Лицо не распознано", Toast.LENGTH_LONG).show()
            }
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