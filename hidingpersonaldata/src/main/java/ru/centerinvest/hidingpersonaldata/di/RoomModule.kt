package ru.centerinvest.hidingpersonaldata.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import ru.centerinvest.hidingpersonaldata.BuildConfig
import ru.centerinvest.hidingpersonaldata.db.RoomFaceDatabase
import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository


@Module
class RoomModule(private val application: Application) {

    @Provides
    fun provideApplication(): Application = application

    @Provides
    @FaceFeature
    fun provideRoomFaceRepository(roomFaceDatabase: RoomFaceDatabase): RoomFaceRepository =
        RoomFaceRepository(roomFaceDatabase.faceDao())

    @Provides
    fun provideFaceDatabase(application: Application): RoomFaceDatabase {
        val factory = SupportFactory(SQLiteDatabase.getBytes(BuildConfig.KEY.toCharArray()))
        return Room.databaseBuilder(
            application,
            RoomFaceDatabase::class.java,
            "face_db"
        ).openHelperFactory(factory).build()
    }

}