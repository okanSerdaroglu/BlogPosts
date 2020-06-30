package com.example.blogposts.repository.main

import com.example.blogposts.di.main.MainScope
import com.example.blogposts.models.AuthToken
import com.example.blogposts.models.BlogPost
import com.example.blogposts.ui.main.blog.state.BlogViewState
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

@FlowPreview
@MainScope
interface BlogRepository {

    fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

}