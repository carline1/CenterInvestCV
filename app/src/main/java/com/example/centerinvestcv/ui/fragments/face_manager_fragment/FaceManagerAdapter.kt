package com.example.centerinvestcv.ui.fragments.face_manager_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.centerinvestcv.R
import com.example.centerinvestcv.databinding.FaceAddViewHolderBinding
import com.example.centerinvestcv.databinding.FaceItemViewHolderBinding
import com.example.centerinvestcv.utils.SharedPreferenceUtil.isHideDataEnabled
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

class FaceManagerAdapter(
    private val actions: Actions
) : ListAdapter<FaceManagerAdapter.FaceViewHolderModel, FaceManagerAdapter.BaseViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        return when (ViewHolderType.values()[viewType]) {
            ViewHolderType.ITEM -> BaseViewHolder.FaceItemViewHolder(
                FaceItemViewHolderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                actions
            )
            ViewHolderType.ADD -> BaseViewHolder.FaceAddViewHolder(
                FaceAddViewHolderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is FaceViewHolderModel.FaceItem -> ViewHolderType.ITEM.ordinal
            is FaceViewHolderModel.AddItem -> ViewHolderType.ADD.ordinal
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) =
        holder.bind(currentList[position])


    override fun getItemCount() = currentList.size

    sealed class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(model: FaceViewHolderModel)

        class FaceItemViewHolder(
            private val binding: FaceItemViewHolderBinding,
            private val actions: Actions
        ) : BaseViewHolder(binding.root) {
            override fun bind(model: FaceViewHolderModel) {
                model as FaceViewHolderModel.FaceItem
                if (binding.root.context.isHideDataEnabled) {
                    binding.apply {
                        root.isVisible = true
                        faceName.text = model.face.name
                        edit.setOnClickListener {
                            actions.editFaceName(
                                model.face.id,
                                model.face.name
                            )
                        }
                        delete.setOnClickListener { actions.deleteFace(model.face.id) }
                    }
                } else {
                    binding.root.isVisible = false
                }
            }
        }

        class FaceAddViewHolder(private val binding: FaceAddViewHolderBinding) :
            BaseViewHolder(binding.root) {
            override fun bind(model: FaceViewHolderModel) {
                if (binding.root.context.isHideDataEnabled) {
                    binding.root.apply {
                        isVisible = true
                        binding.root.setOnClickListener {
                            it.findNavController()
                                .navigate(R.id.action_faceManagerFragment_to_faceAddFragment)
                        }
                    }
                } else {
                    binding.root.isVisible = false
                }
            }
        }

    }

    sealed class FaceViewHolderModel {
        data class FaceItem(val face: FaceEntity) : FaceViewHolderModel()

        object AddItem : FaceViewHolderModel()
    }

    enum class ViewHolderType {
        ITEM,
        ADD
    }

    private class DiffCallback : DiffUtil.ItemCallback<FaceViewHolderModel>() {
        override fun areItemsTheSame(
            oldItem: FaceViewHolderModel,
            newItem: FaceViewHolderModel
        ) =
            if (oldItem is FaceViewHolderModel.FaceItem && newItem is FaceViewHolderModel.FaceItem) {
                oldItem.face.id == newItem.face.id
            } else oldItem is FaceViewHolderModel.AddItem && newItem is FaceViewHolderModel.AddItem

        override fun areContentsTheSame(
            oldItem: FaceViewHolderModel,
            newItem: FaceViewHolderModel
        ) =
            if (oldItem is FaceViewHolderModel.FaceItem && newItem is FaceViewHolderModel.FaceItem) {
                oldItem.face.name == newItem.face.name
                        && oldItem.face.imageData.contentEquals(newItem.face.imageData)
            } else oldItem is FaceViewHolderModel.AddItem && newItem is FaceViewHolderModel.AddItem

    }

    interface Actions {
        fun editFaceName(id: Int, text: String)

        fun deleteFace(id: Int)
    }

}