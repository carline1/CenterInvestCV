package com.example.centerinvestcv.ui.fragments.main

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.centerinvestcv.databinding.MainFragmentBinding
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.centerinvest.hidingpersonaldata.di.RoomFaceComponentViewModel
import ru.centerinvest.hidingpersonaldata.ml.model.FaceNetModel
import ru.centerinvest.hidingpersonaldata.ml.model.TensorflowModels
import ru.centerinvest.hidingpersonaldata.utils.Camera

class MainFragment : Fragment(), Camera.CameraListener {

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    //    private val viewModel: MainFragmentViewModel by viewModels()
    private val roomFaceViewModel: RoomFaceComponentViewModel by viewModels()
    private val viewModel: MainFragmentViewModel by viewModels() {
        MainFragmentViewModel.Factory(roomFaceViewModel.roomFaceComponent.roomFaceRepository)
    }

    private lateinit var faceNetModel: FaceNetModel
    private val lensFacing = CameraSelector.LENS_FACING_FRONT

    private var camera: Camera? = null

    private val backupHiddenViewValues: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                camera?.startCamera(Camera.FaceAnalizerType.Recognize)
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

    private fun allPermissionsGranted() = Camera.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun hideViewValues(hiddenViews: List<TextView>) {
        val hiddenText = "*** ₽"
        if (backupHiddenViewValues.isEmpty() || (backupHiddenViewValues.isNotEmpty() && hiddenViews[0].text != hiddenText)) {
            backupHiddenViewValues.clear()
            hiddenViews.forEach {
                backupHiddenViewValues.add(it.text.toString())
                it.text = hiddenText
            }
        }
    }

    private fun restoreViewValues(hiddenViews: List<TextView>) {
        for (i in backupHiddenViewValues.indices) {
            hiddenViews[i].text = backupHiddenViewValues[i]
        }
    }

    override fun hideData(hide: Boolean) {
        val hiddenViews = listOf(
            binding.account1Amount,
            binding.account2Amount,
            binding.transaction1Amount,
            binding.transaction2Amount,
            binding.transaction3Amount
        )
        if (hide) {
            hideViewValues(hiddenViews)
        } else {
            restoreViewValues(hiddenViews)
        }
//        binding.account1Amount.isVisible = !hide
//        binding.account2Amount.isVisible = !hide
//        binding.transaction1Amount.isVisible = !hide
//        binding.transaction2Amount.isVisible = !hide
//        binding.transaction3Amount.isVisible = !hide

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}