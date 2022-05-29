package ru.centerinvest.hidingpersonaldata.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.centerinvest.hidingpersonaldata.db.dao.Converters
import ru.centerinvest.hidingpersonaldata.db.dao.FaceDao
import ru.centerinvest.hidingpersonaldata.db.dao.FaceEntity

@Database(
    entities = [FaceEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RoomFaceDatabase : RoomDatabase() {

    abstract fun faceDao(): FaceDao
}