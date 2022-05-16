package com.example.centerinvestcv.ui.fragments.face_manager_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.centerinvestcv.databinding.FaceManagerFragmentBinding
import io.reactivex.rxjava3.schedulers.Schedulers

class FaceManagerFragment : Fragment() {

    private var _binding: FaceManagerFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FaceManagerViewModel by viewModels()

    private val adapter = FaceManagerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FaceManagerFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
    }

    private fun setUpUi() {
        binding.apply {
            faceRecycler.adapter = adapter
            faceRecycler.layoutManager = LinearLayoutManager(requireContext())
            back.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        viewModel.loadAllFaceEntities()
            .subscribeOn(Schedulers.io())
            .subscribe({ list ->
                val faceList: MutableList<FaceManagerAdapter.FaceViewHolderModel> =
                    (list.map {
                        FaceManagerAdapter.FaceViewHolderModel.FaceItem(
                            it,
                            { },
                            { })
                    } + FaceManagerAdapter.FaceViewHolderModel.AddItem)
                            as MutableList<FaceManagerAdapter.FaceViewHolderModel>

                adapter.submitList(faceList)
            }, {
                Log.d("TAG", "GG")
            })
    }

}