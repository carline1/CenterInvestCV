package com.example.centerinvestcv.ui.fragments.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.centerinvestcv.db.RoomFaceRepository
import com.example.centerinvestcv.db.dao.FaceEntity
import com.example.centerinvestcv.di.appComponent
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class MainFragmentViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var roomFaceRepository: RoomFaceRepository

    init {
        application.appComponent.inject(this)
    }

    fun loadAllFaceEntities(): Single<List<FaceEntity>> {
        return roomFaceRepository.loadAllFaceEntities()
    }

}