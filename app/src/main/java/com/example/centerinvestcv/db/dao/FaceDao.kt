package com.example.centerinvestcv.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface FaceDao {

    @Insert(entity = FaceEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertFace(faceEntity: FaceEntity): Completable

    @Query("SELECT * FROM ${FaceEntity.TABLE_NAME}")
    fun loadAllFaceEntities(): Single<List<FaceEntity>>

//    @Query("SELECT * FROM ${FaceEntity.TABLE_NAME}")
//    fun loadAllFavouriteEntities(): Single<List<FaceEntity>>
//
//    @Insert(entity = FaceEntity::class, onConflict = OnConflictStrategy.REPLACE)
//    fun insertFavourite(faceEntity: FaceEntity): Completable
//
//    @Insert(entity = FaceEntity::class, onConflict = OnConflictStrategy.REPLACE)
//    fun insertAll(faceEntityList: List<FaceEntity>): Completable
//
//    @Query("DELETE FROM ${FaceEntity.TABLE_NAME} WHERE favouriteId like :favouriteId")
//    fun deleteFavourite(favouriteId: String): Completable

}