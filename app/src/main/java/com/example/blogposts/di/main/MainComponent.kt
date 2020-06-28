package com.example.blogposts.di.main

import com.example.blogposts.ui.main.MainActivity
import dagger.Subcomponent

@Subcomponent(
    modules = [
        MainModule::class,
        MainViewModelModule::class
    ]
)
interface MainComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): MainComponent

    }

    fun inject(mainActivity: MainActivity)

}