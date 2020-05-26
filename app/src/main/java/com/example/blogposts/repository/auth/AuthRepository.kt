package com.example.blogposts.repository.auth

import androidx.lifecycle.LiveData
import com.example.blogposts.api.auth.BlogPostsAuthService
import com.example.blogposts.api.auth.network_responses.LoginResponse
import com.example.blogposts.api.auth.network_responses.RegistrationResponse
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.persistesnce.AuthTokenDao
import com.example.blogposts.session.SessionManager
import com.example.blogposts.utils.GenericApiResponse
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostsAuthService: BlogPostsAuthService,
    val sessionManager: SessionManager
) {

    fun testLoginRequest(
        email: String,
        password: String
    ): LiveData<GenericApiResponse<LoginResponse>> {
        return blogPostsAuthService.login(email, password)
    }

    fun testRegistrationRequest(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<GenericApiResponse<RegistrationResponse>> {
        return blogPostsAuthService.register(email, username, password, confirmPassword)
    }


}