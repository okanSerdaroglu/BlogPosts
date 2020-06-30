package com.example.blogposts.di.main

import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.persistesnce.AppDatabase
import com.example.blogposts.persistesnce.BlogPostDao
import com.example.blogposts.repository.main.*
import com.example.blogposts.session.SessionManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit

@FlowPreview
@Module
object MainModule {

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogPostMainService(
        retrofitBuilder: Retrofit.Builder
    ): BlogPostsMainService {
        return retrofitBuilder
            .build()
            .create(BlogPostsMainService::class.java)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideAccountRepository(
        blogPostsMainService: BlogPostsMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepositoryImpl(
            blogPostsMainService = blogPostsMainService,
            accountPropertiesDao = accountPropertiesDao,
            sessionManager = sessionManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBLogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogRepository(
        blogPostsMainService: BlogPostsMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepositoryImpl(
            blogPostsMainService = blogPostsMainService,
            blogPostDao = blogPostDao,
            sessionManager = sessionManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideCreateRepository(
        blogPostsMainService: BlogPostsMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepositoryImpl(
            blogPostsMainService = blogPostsMainService,
            blogPostDao = blogPostDao,
            sessionManager = sessionManager
        )
    }


}