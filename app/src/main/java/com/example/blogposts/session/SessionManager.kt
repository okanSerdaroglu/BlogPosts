package com.example.blogposts.session

import android.app.Application
import com.example.blogposts.persistesnce.AuthTokenDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {

}