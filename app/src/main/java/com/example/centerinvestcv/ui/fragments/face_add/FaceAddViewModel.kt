package com.example.centerinvestcv.ui.fragments.face_add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.core.Single
import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

class FaceAddViewModel(private val roomFaceRepository: RoomFaceRepository) : ViewModel() {

//    @Inject
//    lateinit var roomFaceRepository: RoomFaceRepository

//    private val _overlayRectList = MutableLiveData<List<RectF>>()
//    val overlayRectList: LiveData<List<RectF>> = _overlayRectList

//    private val _currentFace = MutableLiveData<Bitmap?>()
//    val currentFace: LiveData<Bitmap?> = _currentFace

    init {
//        application.appComponent.inject(this)
//        ViewModelProvider(this)
//            .get<RoomFaceComponentViewModel>()
//            .roomFaceComponent
//            .inject(this)
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

    @Suppress("UNCHECKED_CAST")
    class Factory(private val roomFaceRepository: RoomFaceRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FaceAddViewModel(roomFaceRepository) as T
        }
    }

}