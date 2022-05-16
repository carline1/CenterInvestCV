package com.example.centerinvestcv.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.centerinvestcv.db.dao.FaceEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface FaceDao {

    @Insert(entity = FaceEntity::class, onConflict = OnConflictStrategy.REPLACE)
    fun insertFace(faceEntity: FaceEntity): Completable

    @Query("SELECT * FROM ${FaceEntity.TABLE_NAME}")
    fun loadAllFaceEntities(): Single<List<FaceEntity>>

    @Query("DELETE FROM ${FaceEntity.TABLE_NAME} WHERE id like :id")
    fun deleteFaceEntity(id: Int): Completable

    @Query("UPDATE ${FaceEntity.TABLE_NAME} SET name = :newName WHERE id = :id")
    fun editFaceEntity(id: Int, newName: String): Completable

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