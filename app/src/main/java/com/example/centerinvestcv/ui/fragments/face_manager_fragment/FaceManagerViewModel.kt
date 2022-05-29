package com.example.centerinvestcv.ui.fragments.face_manager_fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

class FaceManagerViewModel(private val roomFaceRepository: RoomFaceRepository) : ViewModel() {

    fun loadAllFaceEntities(): Single<List<FaceEntity>> {
        return roomFaceRepository.loadAllFaceEntities()
    }

    fun deleteFaceEntity(id: Int): Completable {
        return roomFaceRepository.deleteFaceEntity(id)
    }

    fun editFaceEntity(id: Int, newName: String): Completable =
        roomFaceRepository.editFaceEntity(id, newName)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val roomFaceRepository: RoomFaceRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FaceManagerViewModel(roomFaceRepository) as T
        }
    }

}