package com.example.centerinvestcv.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.centerinvestcv.databinding.FragmentFaceAddBinding
import com.example.centerinvestcv.db.dao.FaceEntity
import com.example.centerinvestcv.model.FaceNetModel
import com.example.centerinvestcv.model.Models
import com.example.centerinvestcv.utils.CameraConnectUtils.getPermissions
import com.example.centerinvestcv.utils.CameraConnectUtils.requestPermissionsResult
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceAddFragment : Fragment() {

    private var _binding: FragmentFaceAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FaceAddViewModel by viewModels()

    private var cameraExecutor: ExecutorService? = null
    private lateinit var faceNetModel: FaceNetModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFaceAddBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()
        faceNetModel = FaceNetModel(
            context = requireContext(),
            model = Models.FACENET,
            useGpu = true,
            useXNNPack = true
        )
        getPermissions(faceNetModel, cameraExecutor, binding.previewView)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        setUpUI()


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        requestPermissionsResult(
            faceNetModel,
            cameraExecutor,
            requestCode,
            binding.previewView
        )
    }

    private fun setUpUI() {
        binding.actionButton.setOnClickListener {
            viewModel.currentFace.value?.let {
                viewModel.saveFaceToDatabase(
                    FaceEntity(
                        faceImage = faceNetModel.getFaceEmbedding(it)
                    )
                ).subscribeOn(Schedulers.io()).subscribe()
                Toast.makeText(requireContext(), "Лицо успешно сохранено", Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(requireContext(), "Лицо не распознано", Toast.LENGTH_LONG).show()

            Log.d("TAG", "TEST")
            viewModel.loadAllFaceEntities()
                .subscribeOn(Schedulers.io())
                .subscribe({
                    it.forEach {
                        Log.d("TAG", it.faceImage.toString())
                    }
                }, {
                    Log.d("TAG", "GG")
                })
        }
    }

    private fun setObservers() {
        viewModel.overlayRectList.observe(viewLifecycleOwner) {
            binding.faceContourOverlay.drawFaceContour(it)
        }
        viewModel.currentFace.observe(viewLifecycleOwner) {
            binding.faceImage.setImageBitmap(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }

}