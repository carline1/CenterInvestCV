package com.example.centerinvestcv.di

import com.example.centerinvestcv.ui.fragments.face_add.FaceAddViewModel
import com.example.centerinvestcv.ui.fragments.main.MainFragmentViewModel
import dagger.Component
import javax.inject.Singleton

@Component(modules = [RoomModule::class])
@Singleton
interface AppComponent {
    fun inject(faceAddViewModel: FaceAddViewModel)

    fun inject(mainFragmentViewModel: MainFragmentViewModel)
}