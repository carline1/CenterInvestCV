package com.example.centerinvestcv.ui.fragments.face_manager_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.centerinvestcv.R
import com.example.centerinvestcv.databinding.FaceManagerFragmentBinding
import com.example.centerinvestcv.ui.common.views.CustomAlertDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.centerinvest.hidingpersonaldata.di.RoomFaceComponentViewModel

class FaceManagerFragment : Fragment() {

    private var _binding: FaceManagerFragmentBinding? = null
    private val binding get() = _binding!!

    //    private val viewModel: FaceManagerViewModel by viewModels()
    private val roomFaceViewModel: RoomFaceComponentViewModel by viewModels()
    private val viewModel: FaceManagerViewModel by viewModels() {
        FaceManagerViewModel.Factory(roomFaceViewModel.roomFaceComponent.roomFaceRepository)
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
    }

    private fun setUpUi() {
        adapter = FaceManagerAdapter(object : FaceManagerAdapter.Actions {
            override fun editFaceName(id: Int) {
//                val saveFaceAlertDialog = AlertDialog.Builder(requireContext()).create()
//                val saveFaceAlertDialogLayout = LayoutInflater.from(requireContext())
//                    .inflate(R.layout.custom_alert_dialog, null)
//                saveFaceAlertDialog.apply {
//                    setView(saveFaceAlertDialogLayout)
//                    setCancelable(false)
//                }
//                saveFaceAlertDialogLayout.let { layout ->
//                    val editText = layout.findViewById<EditText>(R.id.editText)
//                    layout.findViewById<TextView>(R.id.title).text = "Изменение названия лица"
//                    layout.findViewById<TextView>(R.id.negativeButton).apply {
//                        text = "Отмена"
//                        setOnClickListener { saveFaceAlertDialog.cancel() }
//                    }
//                    layout.findViewById<TextView>(R.id.positiveButton).apply {
//                        text = "Сохранить"
//                        setOnClickListener {
//                            if (editText.text.toString().isNotBlank()) {
//
//                                viewModel.editFaceEntity(id, editText.text.toString())
//                                    .subscribeOn(Schedulers.io())
//                                    .observeOn(AndroidSchedulers.mainThread())
//                                    .subscribe({
//                                        loadFaces()
//                                        Toast.makeText(
//                                            requireContext(),
//                                            "Название лица изменено",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//                                    }) {
//                                        Toast.makeText(
//                                            requireContext(),
//                                            "Что-то пошло не так",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                saveFaceAlertDialog.cancel()
//                            } else {
//                                editText.error = "Поле не может быть пустым"
//                            }
//                        }
//                    }
//                }
//                saveFaceAlertDialog.show()
                val alert = LayoutInflater.from(requireContext())
                    .inflate(R.layout.custom_alert_dialog, null, false) as CustomAlertDialog
                alert.showDialog(
                    getString(R.string.change_face_name),
                    getString(R.string.cancel),
                    getString(R.string.save),
                    { cancel() }
                ) {
                    if (alert.editText?.text.toString().isNotBlank()) {

                        viewModel.editFaceEntity(id, alert.editText?.text.toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                loadFaces()
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.face_name_has_been_changed),
                                    Toast.LENGTH_LONG
                                ).show()
                            }) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.something_went_wrong),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        cancel()
                    } else {
                        alert.editText?.error = getString(R.string.field_cannot_be_empty)
                    }
                }
            }

            override fun deleteFace(id: Int) {
                viewModel.deleteFaceEntity(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        loadFaces()
                    }) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

        })
        binding.apply {
            faceRecycler.adapter = adapter
            faceRecycler.layoutManager = LinearLayoutManager(requireContext())
            back.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        loadFaces()
    }

    private fun loadFaces() {
        viewModel.loadAllFaceEntities()
            .subscribeOn(Schedulers.io())
            .subscribe({ list ->
                val faceList: MutableList<FaceManagerAdapter.FaceViewHolderModel> =
                    (list.map {
                        FaceManagerAdapter.FaceViewHolderModel.FaceItem(it)
                    } + FaceManagerAdapter.FaceViewHolderModel.AddItem)
                            as MutableList<FaceManagerAdapter.FaceViewHolderModel>

                adapter?.submitList(faceList)
            }, {
                Log.d("TAG", "GG")
            })
    }

}