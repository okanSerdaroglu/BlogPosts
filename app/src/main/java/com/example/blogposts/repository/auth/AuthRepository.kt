package com.example.blogposts.repository.auth

import com.example.blogposts.api.auth.BlogPostsAuthService
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.persistesnce.AuthTokenDao
import com.example.blogposts.session.SessionManager
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostsAuthService: BlogPostsAuthService,
    val sessionManager: SessionManager
) {
}