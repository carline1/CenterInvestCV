package com.example.centerinvestcv.ui.fragments.face_manager_fragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.centerinvestcv.db.RoomFaceRepository
import com.example.centerinvestcv.db.dao.FaceEntity
import com.example.centerinvestcv.di.appComponent
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class FaceManagerViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var roomFaceRepository: RoomFaceRepository

    init {
        application.appComponent.inject(this)
    }

    fun loadAllFaceEntities(): Single<List<FaceEntity>> {
        return roomFaceRepository.loadAllFaceEntities()
    }

    fun deleteFaceEntity(id: Int): Completable {
        return roomFaceRepository.deleteFaceEntity(id)
    }

    fun editFaceEntity(id: Int, newName: String): Completable =
        roomFaceRepository.editFaceEntity(id, newName)

}