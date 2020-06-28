package com.example.blogposts.di.auth

import com.example.blogposts.ui.auth.AuthActivity
import dagger.Subcomponent

@Subcomponent(
    modules = [
        AuthModule::class,
        AuthViewModelModule::class,
        AuthFragmentsModule::class
    ]
)
interface AuthComponent {

    @Subcomponent.Factory
    interface Factory {

        fun create(): AuthComponent

    }

    fun inject(authActivity: AuthActivity)

}