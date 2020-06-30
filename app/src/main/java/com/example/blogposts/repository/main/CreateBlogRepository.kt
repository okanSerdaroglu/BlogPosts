package com.example.blogposts.repository.main


import com.example.blogposts.di.main.MainScope
import com.example.blogposts.models.AuthToken
import com.example.blogposts.ui.main.create_blog.state.CreateBlogViewState
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

@FlowPreview
@MainScope
interface CreateBlogRepository {

    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ): Flow<DataState<CreateBlogViewState>>
}