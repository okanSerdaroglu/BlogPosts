package com.example.blogposts.session

import android.app.Application
import com.example.blogposts.persistesnce.AuthTokenDao

class SessionManager
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {

}