package com.example.blogposts.api.main

import androidx.lifecycle.LiveData
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.utils.GenericApiResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface BlogPostsMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>

}