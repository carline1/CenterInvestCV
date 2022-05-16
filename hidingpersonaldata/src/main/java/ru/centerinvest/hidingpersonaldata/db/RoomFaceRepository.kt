//package ru.centerinvest.hidingpersonaldata.db
//
//import io.reactivex.rxjava3.core.Completable
//import io.reactivex.rxjava3.core.Single
//import ru.centerinvest.hidingpersonaldata.db.dao.FaceDao
//import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity
//
//class RoomFaceRepository(private val faceDao: FaceDao) {
//
//    fun insertFavourite(faceEntity: FaceEntity): Completable =
//        faceDao.insertFace(faceEntity)
//
//    fun loadAllFaceEntities(): Single<List<FaceEntity>> =
//        faceDao.loadAllFaceEntities()
//
//
////    fun loadAllFavouriteEntities(): Single<List<FavouriteIdsEntity>> =
////        faceDao.loadAllFavouriteEntities()
////
////    fun insertAll(favouriteIdsEntityList: List<FavouriteIdsEntity>) =
////        faceDao.insertAll(favouriteIdsEntityList)
////
////    fun insertFavourite(favouriteIdsEntity: FavouriteIdsEntity): Completable =
////        faceDao.insertFavourite(favouriteIdsEntity)
////
////    fun deleteFavourite(favouriteId: String): Completable = faceDao.deleteFavourite(favouriteId)
//
//}