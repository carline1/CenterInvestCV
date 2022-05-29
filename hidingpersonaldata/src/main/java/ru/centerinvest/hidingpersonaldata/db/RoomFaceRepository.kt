package ru.centerinvest.hidingpersonaldata.db

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import ru.centerinvest.hidingpersonaldata.db.dao.FaceDao
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

class RoomFaceRepository(private val faceDao: FaceDao) {

    fun insertFavourite(faceEntity: FaceEntity): Completable =
        faceDao.insertFace(faceEntity)

    fun loadAllFaceEntities(): Single<List<FaceEntity>> =
        faceDao.loadAllFaceEntities()

    fun deleteFaceEntity(id: Int): Completable = faceDao.deleteFaceEntity(id)

    fun editFaceEntity(id: Int, newName: String): Completable =
        faceDao.editFaceEntity(id, newName)

}