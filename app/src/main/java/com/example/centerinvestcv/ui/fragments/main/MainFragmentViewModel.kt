package com.example.centerinvestcv.ui.fragments.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.core.Single
import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

class MainFragmentViewModel(private val roomFaceRepository: RoomFaceRepository) : ViewModel() {

//    @Inject
//    lateinit var roomFaceRepository: RoomFaceRepository

    init {
//        application.appComponent.inject(this)
    }

    fun loadAllFaceEntities(): Single<List<FaceEntity>> {
        return roomFaceRepository.loadAllFaceEntities()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val roomFaceRepository: RoomFaceRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainFragmentViewModel(roomFaceRepository) as T
        }
    }

}