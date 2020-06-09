package com.example.blogposts.ui.main.blog.state

sealed class BlogStateEvent {

    class BlogSearchEvent : BlogStateEvent()

    class CheckAuthorBlogPostsEvent : BlogStateEvent()

    class DeleteBlogPostEvent: BlogStateEvent()

    class None : BlogStateEvent()

}