package com.example.centerinvestcv.ui.fragments.face_manager_fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

class FaceManagerViewModel(private val roomFaceRepository: RoomFaceRepository) : ViewModel() {

    private val _loadAllFaceEntitiesMLD = MutableLiveData<Result<List<FaceEntity>>>()
    val loadAllFaceEntitiesMLD: LiveData<Result<List<FaceEntity>>> = _loadAllFaceEntitiesMLD

    private val _deleteFaceEntityMLD = MutableLiveData<Result<Unit>>()
    val deleteFaceEntityMLD: LiveData<Result<Unit>> = _deleteFaceEntityMLD

    private val _editFaceEntityMLD = MutableLiveData<Result<Unit>>()
    val editFaceEntityMLD: LiveData<Result<Unit>> = _editFaceEntityMLD

    fun loadAllFaceEntities() {
        roomFaceRepository.loadAllFaceEntities()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                _loadAllFaceEntitiesMLD.value = Result.success(list)
            }, {
                _loadAllFaceEntitiesMLD.value = Result.failure(it)
            })
    }

    fun deleteFaceEntity(id: Int) {
        roomFaceRepository.deleteFaceEntity(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _deleteFaceEntityMLD.value = Result.success(Unit)
            }) {
                _deleteFaceEntityMLD.value = Result.failure(it)
            }
    }

    fun editFaceEntity(id: Int, newName: String) {
        roomFaceRepository.editFaceEntity(id, newName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _editFaceEntityMLD.value = Result.success(Unit)
            }) {
                _editFaceEntityMLD.value = Result.failure(it)
            }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val roomFaceRepository: RoomFaceRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FaceManagerViewModel(roomFaceRepository) as T
        }
    }

}