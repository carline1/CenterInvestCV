package ru.centerinvest.hidingpersonaldata.di

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.Component
import ru.centerinvest.hidingpersonaldata.db.RoomFaceRepository
import javax.inject.Scope

@[FaceFeature Component(modules = [RoomModule::class])]
interface RoomFaceComponent {

    val roomFaceRepository: RoomFaceRepository

}

class RoomFaceComponentViewModel(application: Application) : AndroidViewModel(application) {
    val roomFaceComponent: RoomFaceComponent = DaggerRoomFaceComponent
        .builder()
        .roomModule(RoomModule(application))
        .build()
}

@Scope
@Retention
annotation class FaceFeature