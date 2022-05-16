package ru.centerinvest.hidingpersonaldata.di

//import android.app.Application
//import androidx.room.Room
//import dagger.Module
//import dagger.Provides
//import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository
//import javax.inject.Singleton
//
//@Module
//class RoomModule(private val application: Application) {
//
//    @Provides
//    fun provideApplication(): Application = application
//
//    @Provides
//    fun provideFaceDatabase(application: Application): RoomFaceDatabase =
//        Room.databaseBuilder(
//            application,
//            RoomFaceDatabase::class.java,
//            "face_database"
//        ).build()
//
//    @Provides
//    @Singleton
//    fun provideRoomFaceRepository(roomFaceDatabase: RoomFaceDatabase): RoomFaceRepository =
//        RoomFaceRepository(roomFaceDatabase.faceDao())
//
//}