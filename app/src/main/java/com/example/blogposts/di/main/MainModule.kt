package com.example.blogposts.di.main

import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.persistesnce.AppDatabase
import com.example.blogposts.persistesnce.BlogPostDao
import com.example.blogposts.repository.main.AccountRepository
import com.example.blogposts.repository.main.BlogRepository
import com.example.blogposts.repository.main.CreateBlogRepository
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
    fun provideAccountRepository(
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

    @MainScope
    @Provides
    fun provideBLogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
        blogPostsMainService: BlogPostsMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(
            blogPostsMainService = blogPostsMainService,
            blogPostDao = blogPostDao,
            sessionManager = sessionManager
        )
    }

    @MainScope
    @Provides
    fun provideCreateRepository(
        blogPostsMainService: BlogPostsMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository(
            blogPostsMainService = blogPostsMainService,
            blogPostDao = blogPostDao,
            sessionManager = sessionManager
        )
    }


}