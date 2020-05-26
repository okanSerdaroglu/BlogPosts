package com.example.blogposts.ui

import dagger.android.support.DaggerAppCompatActivity

abstract class BaseActivity : DaggerAppCompatActivity() {
    val TAG: String = "BaseActivity"
}