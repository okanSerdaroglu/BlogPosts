package com.example.blogposts.ui

import com.example.blogposts.session.SessionManager
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity() {
    val TAG: String = "BaseActivity"

    @Inject
    lateinit var sessionManager:SessionManager
}