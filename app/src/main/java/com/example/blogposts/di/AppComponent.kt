package com.example.blogposts.di

import android.app.Application
import com.example.blogposts.di.auth.AuthComponent
import com.example.blogposts.di.main.MainComponent
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.BaseActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        AppModule::class,
        SubComponentModule::class
    ]
)
interface AppComponent {

    val sessionManager: SessionManager

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent

    }

    fun inject(baseActivity: BaseActivity)

    fun authComponent(): AuthComponent.Factory

    fun mainComponent(): MainComponent.Factory


}