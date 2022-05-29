package com.example.centerinvestcv.ui.fragments.face_add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.core.Single
import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

class FaceAddViewModel(private val roomFaceRepository: RoomFaceRepository) : ViewModel() {

    fun saveFaceToDatabase(faceEntity: FaceEntity) =
        roomFaceRepository.insertFavourite(faceEntity)

    fun loadAllFaceEntities(): Single<List<FaceEntity>> {
        return roomFaceRepository.loadAllFaceEntities()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val roomFaceRepository: RoomFaceRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FaceAddViewModel(roomFaceRepository) as T
        }
    }

}