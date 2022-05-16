package com.example.centerinvestcv.db

import com.example.centerinvestcv.db.dao.FaceDao
import com.example.centerinvestcv.db.dao.FaceEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class RoomFaceRepository(private val faceDao: FaceDao) {

    fun insertFavourite(faceEntity: FaceEntity): Completable =
        faceDao.insertFace(faceEntity)

    fun loadAllFaceEntities(): Single<List<FaceEntity>> =
        faceDao.loadAllFaceEntities()

    fun deleteFaceEntity(id: Int): Completable = faceDao.deleteFaceEntity(id)

    fun editFaceEntity(id: Int, newName: String): Completable =
        faceDao.editFaceEntity(id, newName)

//    fun loadAllFavouriteEntities(): Single<List<FavouriteIdsEntity>> =
//        faceDao.loadAllFavouriteEntities()
//
//    fun insertAll(favouriteIdsEntityList: List<FavouriteIdsEntity>) =
//        faceDao.insertAll(favouriteIdsEntityList)
//
//    fun insertFavourite(favouriteIdsEntity: FavouriteIdsEntity): Completable =
//        faceDao.insertFavourite(favouriteIdsEntity)
//
//    fun deleteFavourite(favouriteId: String): Completable = faceDao.deleteFavourite(favouriteId)

}