package com.example.centerinvestcv.di

import com.example.centerinvestcv.ui.fragments.FaceAddViewModel
import dagger.Component
import javax.inject.Singleton

@Component(modules = [RoomModule::class])
@Singleton
interface AppComponent {
    fun inject(faceAddViewModel: FaceAddViewModel)
}