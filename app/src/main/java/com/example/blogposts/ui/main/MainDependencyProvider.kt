package com.example.blogposts.ui.main

import com.bumptech.glide.RequestManager
import com.example.blogposts.viewmodels.ViewModelProviderFactory

interface MainDependencyProvider {

    fun getVMProviderFactory(): ViewModelProviderFactory

    fun getGlideRequestManager(): RequestManager

}