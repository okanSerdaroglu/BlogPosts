package com.example.blogposts.ui.main

import com.bumptech.glide.RequestManager
import com.example.blogposts.viewmodels.AuthViewModelFactory

interface MainDependencyProvider {

    fun getVMProviderFactory(): AuthViewModelFactory

    fun getGlideRequestManager(): RequestManager

}