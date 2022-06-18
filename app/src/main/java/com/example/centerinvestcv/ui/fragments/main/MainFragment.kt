package com.example.centerinvestcv.ui.fragments.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.centerinvestcv.R
import com.example.centerinvestcv.databinding.MainFragmentBinding
import com.example.centerinvestcv.utils.SharedPreferenceUtil.isHideDataEnabled
import ru.centerinvest.hidingpersonaldata.di.RoomFaceComponentViewModel
import ru.centerinvest.hidingpersonaldata.ml.model.FaceNetModel
import ru.centerinvest.hidingpersonaldata.ml.model.TensorflowModels
import ru.centerinvest.hidingpersonaldata.utils.Camera

class MainFragment : Fragment(), Camera.CameraListener {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private val roomFaceViewModel: RoomFaceComponentViewModel by viewModels()
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(roomFaceViewModel.roomFaceComponent.roomFaceRepository)
    }

    private lateinit var faceNetModel: FaceNetModel
    private val lensFacing = CameraSelector.LENS_FACING_FRONT

    private var camera: Camera? = null

    private var hiddenViews: List<TextView>? = null
    private val backupHiddenViewValues: MutableList<String> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        faceNetModel = FaceNetModel(
            context = requireContext(),
            model = TensorflowModels.FACENET,
            useGpu = true,
            useXNNPack = true
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
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
        hiddenViews = listOf(
            binding.accountAmount1,
            binding.accountAmount2,
            binding.paymentAmount1,
            binding.paymentAmount2,
            binding.paymentAmount3
        )
        if (requireContext().isHideDataEnabled) {
            hideViewValues(hiddenViews)
            binding.previewView.doOnLayout {
                camera = Camera(
                    requireActivity(),
                    this,
                    faceNetModel,
                    lensFacing,
                    it,
                    binding.previewView.surfaceProvider
                )

                if (camera?.allPermissionsGranted(requireContext()) == true) {
                    camera?.startCamera(Camera.FaceAnalyzerType.Recognize)
                } else {
                    requestPermissions()
                }
                camera?.attachListener(this)

                viewModel.loadAllFaceEntities()
            }
        }
    }

    private fun setObservers() {
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

    private fun hideViewValues(hiddenViews: List<TextView>?) {
        if (hiddenViews.isNullOrEmpty()) return

        val hiddenText = "*** ₽"
        if (backupHiddenViewValues.isEmpty() || (backupHiddenViewValues.isNotEmpty() && hiddenViews[0].text != hiddenText)) {
            backupHiddenViewValues.clear()
            hiddenViews.forEach {
                backupHiddenViewValues.add(it.text.toString())
                it.text = hiddenText
            }
        }
    }

    private fun restoreViewValues(hiddenViews: List<TextView>?) {
        if (hiddenViews.isNullOrEmpty()) return

        for (i in backupHiddenViewValues.indices) {
            hiddenViews[i].text = backupHiddenViewValues[i]
        }
    }

    override fun hideData(hide: Boolean) {
        if (hide) {
            hideViewValues(hiddenViews)
        } else {
            restoreViewValues(hiddenViews)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}