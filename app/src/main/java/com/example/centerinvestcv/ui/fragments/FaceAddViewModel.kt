package com.example.centerinvestcv.ui.fragments

import android.app.Application
import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.centerinvestcv.db.RoomFaceRepository
import com.example.centerinvestcv.db.dao.FaceEntity
import com.example.centerinvestcv.di.appComponent
import com.example.centerinvestcv.utils.CameraConnectUtils
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class FaceAddViewModel(application: Application) : AndroidViewModel(application), CameraConnectUtils.Listener {

    @Inject
    lateinit var roomFaceRepository: RoomFaceRepository

    private val _overlayRectList = MutableLiveData<List<RectF>>()
    val overlayRectList: LiveData<List<RectF>> = _overlayRectList

    private val _currentFace = MutableLiveData<Bitmap?>()
    val currentFace: LiveData<Bitmap?> = _currentFace

    init {
        CameraConnectUtils.attachListener(this)
        application.appComponent.inject(this)
    }

    fun saveFaceToDatabase(faceEntity: FaceEntity) =
        roomFaceRepository.insertFavourite(faceEntity)

    fun loadAllFaceEntities(): Single<List<FaceEntity>> {
        return roomFaceRepository.loadAllFaceEntities()
    }

    override fun onCleared() {
        super.onCleared()

        CameraConnectUtils.detachListener()
    }

    override fun drawOverlay(faceBounds: List<RectF>) {
        _overlayRectList.value = faceBounds
    }

    override fun drawFace(faces: List<Bitmap>) {
        _currentFace.value = if (faces.isNotEmpty()) faces[0] else null
    }

}