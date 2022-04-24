package com.example.centerinvestcv.di

import android.app.Application
import androidx.room.Room
import com.example.centerinvestcv.db.RoomFaceDatabase
import com.example.centerinvestcv.db.RoomFaceRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule(private val application: Application) {

    @Provides
    fun provideApplication(): Application = application

    @Provides
    fun provideFaceDatabase(application: Application): RoomFaceDatabase =
        Room.databaseBuilder(
            application,
            RoomFaceDatabase::class.java,
            "face_database"
        ).build()

    @Provides
    @Singleton
    fun provideRoomFaceRepository(roomFaceDatabase: RoomFaceDatabase): RoomFaceRepository =
        RoomFaceRepository(roomFaceDatabase.faceDao())

}