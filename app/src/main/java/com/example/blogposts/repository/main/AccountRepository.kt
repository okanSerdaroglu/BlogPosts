package com.example.blogposts.repository.main

import android.util.Log
import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.session.SessionManager
import kotlinx.coroutines.Job
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val blogPostsMainService: BlogPostsMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) {
    private val TAG: String = "AppDebug"

    private var repositoryJob: Job? = null

    fun cancelActiveJobs() {
        Log.d(TAG, "AuthRepository: cancelling on-going jobs...")
    }
}