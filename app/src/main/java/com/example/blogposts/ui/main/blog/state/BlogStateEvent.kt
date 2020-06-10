package com.example.blogposts.ui.main.blog.state

import okhttp3.MultipartBody

sealed class BlogStateEvent {

    class BlogSearchEvent : BlogStateEvent()

    class CheckAuthorBlogPostsEvent : BlogStateEvent()

    class DeleteBlogPostEvent : BlogStateEvent()

    data class UpdatedBlogPostEvent(
        var title: String,
        var body: String,
        val image: MultipartBody.Part?
    ) : BlogStateEvent()

    class None : BlogStateEvent()


}