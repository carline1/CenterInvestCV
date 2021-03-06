package com.example.centerinvestcv.ui.fragments.face_add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

class FaceAddViewModel(private val roomFaceRepository: RoomFaceRepository) : ViewModel() {

    private val _saveFaceToDatabaseMLD = MutableLiveData<Result<Unit>>()
    val saveFaceToDatabaseMLD: LiveData<Result<Unit>> = _saveFaceToDatabaseMLD

    private val _loadAllFaceEntitiesMLD = MutableLiveData<Result<List<FaceEntity>>>()
    val loadAllFaceEntitiesMLD: LiveData<Result<List<FaceEntity>>> = _loadAllFaceEntitiesMLD

    fun saveFaceToDatabase(faceEntity: FaceEntity) {
        roomFaceRepository.addFaceEntity(faceEntity)
            .subscribeOn(Schedulers.io())
            .subscribe({
                _saveFaceToDatabaseMLD.postValue(Result.success(Unit))
            }, {
                _saveFaceToDatabaseMLD.postValue(Result.failure(it))
            })
    }

    fun loadAllFaceEntities() {
        roomFaceRepository.loadAllFaceEntities()
            .subscribeOn(Schedulers.io())
            .subscribe({ list ->
                _loadAllFaceEntitiesMLD.postValue(Result.success(list))
            }, {
                _loadAllFaceEntitiesMLD.postValue(Result.failure(it))
            })
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val roomFaceRepository: RoomFaceRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FaceAddViewModel(roomFaceRepository) as T
        }
    }

}