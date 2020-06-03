package com.example.blogposts.di.main

import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.repository.main.AccountRepository
import com.example.blogposts.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideBlogPostMainService(
        retrofitBuilder: Retrofit.Builder
    ): BlogPostsMainService {
        return retrofitBuilder
            .build()
            .create(BlogPostsMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideMainRepository(
        blogPostsMainService: BlogPostsMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(
            blogPostsMainService = blogPostsMainService,
            accountPropertiesDao = accountPropertiesDao,
            sessionManager = sessionManager
        )
    }

}