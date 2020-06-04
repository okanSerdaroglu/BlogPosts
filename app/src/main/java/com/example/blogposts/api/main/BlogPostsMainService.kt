package com.example.blogposts.api.main

import androidx.lifecycle.LiveData
import com.example.blogposts.api.GenericResponse
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.utils.GenericApiResponse
import retrofit2.http.*

interface BlogPostsMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>

    @PUT("account/properties/update")
    @FormUrlEncoded
    fun saveAccountProperties(
        @Header("Authorization") authorization: String,
        @Field("email") email: String,
        @Field("username") username: String
    ): LiveData<GenericApiResponse<GenericResponse>>

}