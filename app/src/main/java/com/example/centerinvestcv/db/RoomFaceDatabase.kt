package com.example.centerinvestcv.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.centerinvestcv.db.dao.Converters
import com.example.centerinvestcv.db.dao.FaceDao
import com.example.centerinvestcv.db.dao.FaceEntity

@Database(
    entities = [FaceEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RoomFaceDatabase : RoomDatabase() {

    abstract fun faceDao(): FaceDao
}