package com.example.blogposts.repository.main

import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.persistesnce.BlogPostDao
import com.example.blogposts.repository.JobManager
import com.example.blogposts.session.SessionManager
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val blogPostsMainService: BlogPostsMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
):JobManager("BlogRepository")
{
    private val TAG:String = "AppDebug"


}