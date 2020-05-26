package com.example.blogposts.di.auth

import com.example.blogposts.api.auth.BlogPostsAuthService
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.persistesnce.AuthTokenDao
import com.example.blogposts.repository.auth.AuthRepository
import com.example.blogposts.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule {

    @AuthScope
    @Provides
    fun provideFakeApiService(retrofitBuilder: Retrofit.Builder): BlogPostsAuthService {
        return retrofitBuilder
            .build().create(BlogPostsAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        blogPostsAuthService: BlogPostsAuthService
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            blogPostsAuthService,
            sessionManager
        )
    }

}