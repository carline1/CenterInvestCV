package com.example.centerinvestcv.ui.fragments.face_manager_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.centerinvestcv.R
import com.example.centerinvestcv.databinding.FaceManagerFragmentBinding
import com.example.centerinvestcv.ui.common.views.CustomAlertDialog
import com.example.centerinvestcv.utils.FullScreenStateChanger
import com.example.centerinvestcv.utils.SharedPreferenceUtil.isHideDataEnabled
import ru.centerinvest.hidingpersonaldata.di.RoomFaceComponentViewModel

class FaceManagerFragment : Fragment() {

    private var _binding: FaceManagerFragmentBinding? = null
    private val binding get() = _binding!!

    private val roomFaceComponentViewModel: RoomFaceComponentViewModel by viewModels()
    private val viewModel: FaceManagerViewModel by viewModels {
        FaceManagerViewModel.Factory(roomFaceComponentViewModel.roomFaceComponent.roomFaceRepository)
    }

    private var adapter: FaceManagerAdapter? = null

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
        setObservers()
    }

    private fun setUpUi() {
        adapter = FaceManagerAdapter(object : FaceManagerAdapter.Actions {
            override fun editFaceName(id: Int, text: String) {
                val alert = LayoutInflater.from(requireContext())
                    .inflate(R.layout.custom_alert_dialog, binding.root, false) as CustomAlertDialog
                alert.editText?.setText(text)
                alert.showDialog(
                    getString(R.string.change_face_name),
                    getString(R.string.cancel),
                    getString(R.string.save),
                    { cancel() }
                ) {
                    if (alert.editText?.text.toString().isNotBlank()) {
                        viewModel.editFaceEntity(id, alert.editText?.text.toString())
                        cancel()
                    } else {
                        alert.editText?.error = getString(R.string.field_cannot_be_empty)
                    }
                }
            }

            override fun deleteFace(id: Int) {
                viewModel.deleteFaceEntity(id)
            }

        })
        binding.apply {
            faceRecycler.adapter = adapter
            faceRecycler.layoutManager = LinearLayoutManager(requireContext())
            backButton.setOnClickListener {
                findNavController().popBackStack()
            }
            hidingDataSwitcher.apply {
                isChecked = requireContext().isHideDataEnabled
                setOnCheckedChangeListener { _, isChecked ->
                    requireContext().isHideDataEnabled = isChecked
                    adapter?.notifyDataSetChanged()
                }
            }
        }

        viewModel.loadAllFaceEntities()
    }

    private fun setObservers() {
        viewModel.loadAllFaceEntitiesMLD.observe(viewLifecycleOwner) { result ->
            val faceList: MutableList<FaceManagerAdapter.FaceViewHolderModel> =
                mutableListOf(FaceManagerAdapter.FaceViewHolderModel.AddItem)
            result.onSuccess { list ->
                faceList.addAll(0, list.map {
                    FaceManagerAdapter.FaceViewHolderModel.FaceItem(it)
                })

                adapter?.submitList(faceList)
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.faces_not_loading),
                    Toast.LENGTH_SHORT
                ).show()
            }
            adapter?.submitList(faceList)
        }
        viewModel.deleteFaceEntityMLD.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                viewModel.loadAllFaceEntities()
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewModel.editFaceEntityMLD.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                viewModel.loadAllFaceEntities()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.face_name_has_been_changed),
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
    }

    override fun onResume() {
        super.onResume()
        FullScreenStateChanger.fullScreen(activity as AppCompatActivity, true)
    }

    override fun onStop() {
        super.onStop()
        FullScreenStateChanger.fullScreen(activity as AppCompatActivity, false)
    }

}