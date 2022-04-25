package com.example.centerinvestcv.ui.fragments.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.centerinvestcv.databinding.FragmentMainBinding
import com.example.centerinvestcv.ml.model.FaceNetModel
import com.example.centerinvestcv.ml.model.TensorflowModels
import com.example.centerinvestcv.utils.Camera
import io.reactivex.rxjava3.schedulers.Schedulers

class MainFragment : Fragment(), Camera.CameraListener {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainFragmentViewModel by viewModels()

    private lateinit var faceNetModel: FaceNetModel
    private val lensFacing = CameraSelector.LENS_FACING_FRONT

    private var camera: Camera? = null

    private val backupHiddenViewValues: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return _binding?.root
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

}