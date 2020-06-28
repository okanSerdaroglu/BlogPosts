package com.example.blogposts

import android.app.Application
import com.example.blogposts.di.AppComponent
import com.example.blogposts.di.DaggerAppComponent
import com.example.blogposts.di.auth.AuthComponent

class BaseApplication : Application() {

    lateinit var appComponent: AppComponent

    private var authComponent: AuthComponent? = null

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    fun authComponent(): AuthComponent {
        if (authComponent == null) {
            authComponent = appComponent.authComponent().create()
        }
        return authComponent as AuthComponent
    }

    fun releaseAuthComponent() {
        authComponent = null
    }

    fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

}