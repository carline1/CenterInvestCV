package com.example.centerinvestcv.di

import android.app.Application
import androidx.room.Room
import com.example.centerinvestcv.db.RoomFaceDatabase
import com.example.centerinvestcv.db.RoomFaceRepository
import dagger.Module
import dagger.Provides
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
class RoomModule(private val application: Application) {

    @Provides
    fun provideApplication(): Application = application

    @Provides
    fun provideFaceDatabase(application: Application): RoomFaceDatabase {
        val factory = SupportFactory(SQLiteDatabase.getBytes("PassPhrase".toCharArray()))
        return Room.databaseBuilder(application, RoomFaceDatabase::class.java, "face_database")
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    @Singleton
    fun provideRoomFaceRepository(roomFaceDatabase: RoomFaceDatabase): RoomFaceRepository =
        RoomFaceRepository(roomFaceDatabase.faceDao())

}