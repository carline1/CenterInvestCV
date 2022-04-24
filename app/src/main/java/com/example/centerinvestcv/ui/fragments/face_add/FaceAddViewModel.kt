package com.example.centerinvestcv.ui.fragments.face_add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.centerinvestcv.db.RoomFaceRepository
import com.example.centerinvestcv.db.dao.FaceEntity
import com.example.centerinvestcv.di.appComponent
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class FaceAddViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var roomFaceRepository: RoomFaceRepository

//    private val _overlayRectList = MutableLiveData<List<RectF>>()
//    val overlayRectList: LiveData<List<RectF>> = _overlayRectList

//    private val _currentFace = MutableLiveData<Bitmap?>()
//    val currentFace: LiveData<Bitmap?> = _currentFace

    init {
        application.appComponent.inject(this)
    }

    fun saveFaceToDatabase(faceEntity: FaceEntity) =
        roomFaceRepository.insertFavourite(faceEntity)

    fun loadAllFaceEntities(): Single<List<FaceEntity>> {
        return roomFaceRepository.loadAllFaceEntities()
    }

//    override fun drawOverlay(faceBounds: List<RectF>) {
//        _overlayRectList.value = faceBounds
//    }
//
//    override fun drawFace(faces: List<Bitmap>) {
//        _currentFace.value = if (faces.isNotEmpty()) faces[0] else null
//    }

}