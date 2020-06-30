package com.example.blogposts.repository.main


import android.util.Log
import androidx.lifecycle.LiveData
import com.example.blogposts.api.GenericResponse
import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.api.main.responses.BlogCreateUpdateResponse
import com.example.blogposts.api.main.responses.BlogListSearchResponse
import com.example.blogposts.di.main.MainScope
import com.example.blogposts.models.AuthToken
import com.example.blogposts.models.BlogPost
import com.example.blogposts.persistesnce.BlogPostDao
import com.example.blogposts.persistesnce.returnOrderedBlogQuery
import com.example.blogposts.repository.NetworkBoundResource
import com.example.blogposts.session.SessionManager
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.Response
import com.example.blogposts.utils.ResponseType
import com.example.blogposts.ui.main.blog.state.BlogViewState
import com.example.blogposts.ui.main.blog.state.BlogViewState.*
import com.example.blogposts.utils.*
import com.example.blogposts.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.example.blogposts.utils.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.lang.Exception
import javax.inject.Inject

@MainScope
class BlogRepository
@Inject
constructor(
    val blogPostsMainService: BlogPostsMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("BlogRepository") {
    private val TAG: String = "AppDebug"

    fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = false,
            shouldLoadFromCache = true
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    result.addSource(loadFromCache()) { viewState ->
                        viewState.blogFields.isQueryInProgress = false
                        if (page * Constants.PAGINATION_PAGE_SIZE > viewState.blogFields.blogList.size) {
                            viewState.blogFields.isQueryExhausted = true
                        }
                        onCompleteJob(
                            DataState.data(
                                data = viewState,
                                response = null
                            )
                        )
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {
                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for (blogPostResponse in response.body.results) {
                    blogPostList.add(
                        BlogPost(
                            pk = blogPostResponse.pk,
                            title = blogPostResponse.title,
                            slug = blogPostResponse.slug,
                            body = blogPostResponse.body,
                            image = blogPostResponse.image,
                            date_updated = DateUtil.convertServerStringDateToLong(
                                blogPostResponse.date_updated
                            ),
                            username = blogPostResponse.username
                        )
                    )
                }
                updateLocalDb(blogPostList)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return blogPostsMainService.searchListBlogPosts(
                    "Token ${authToken.token!!}",
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
                    .switchMap { blogList ->
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    BlogFields(
                                        blogList = blogList,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {
                if (cacheObject != null) {
                    withContext(IO) {
                        for (blogPost in cacheObject) {
                            try {
                                // launch each insert as a separate job to executed in parallel
                                val j = launch {
                                    Log.d(TAG, "updateLocalDB: inserting blog:$blogPost")
                                    blogPostDao.insert(blogPost)
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG, "updateLocalDB: error updating cache" +
                                            "on blog post with slug: ${blogPost.slug}"
                                    // optional error handling
                                )
                            }
                        }
                    }
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPosts", job)
            }

        }.asLiveData()
    }


    // for onSavedInstanceState
    fun restoreBlogListFromCache(
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = false,
            shouldCancelIfNoInternet = false,
            shouldLoadFromCache = true
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    result.addSource(loadFromCache()) { viewState ->
                        viewState.blogFields.isQueryInProgress = false
                        if (page * Constants.PAGINATION_PAGE_SIZE > viewState.blogFields.blogList.size) {
                            viewState.blogFields.isQueryExhausted = true
                        }
                        onCompleteJob(
                            DataState.data(
                                data = viewState,
                                response = null
                            )
                        )
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {}

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return AbsentLiveData.create()
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
                    .switchMap { blogList ->
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    BlogFields(
                                        blogList = blogList,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {}

            override fun setJob(job: Job) {
                addJob("restoreBlogListFromCache", job)
            }

        }.asLiveData()
    }


    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Main) {
                    Log.d(TAG, "handleApiSuccessResponse: ${response.body.response}")
                    val isAuthor = response.body.response == RESPONSE_HAS_PERMISSION_TO_EDIT
                    onCompleteJob(
                        DataState.data(
                            data = BlogViewState(
                                viewBlogFields = ViewBlogFields(
                                    isAuthorOfBLogPost = isAuthor
                                )
                            ),
                            response = null
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogPostsMainService.isAuthorOfBlogPost(
                    authorization = "Token ${authToken.token}",
                    slug = slug
                )
            }

            // not applicable
            override fun loadFromCache(): LiveData<BlogViewState> {
                TODO("Not yet implemented")
            }

            // not applicable
            override suspend fun updateLocalDb(cacheObject: Any?) {

            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }

        }.asLiveData()

    }

    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, BlogPost, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {} // not applicable

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                if (response.body.response == SUCCESS_BLOG_DELETED) {
                    updateLocalDb(blogPost)
                } else {
                    onCompleteJob(
                        DataState.error(
                            Response(
                                ERROR_UNKNOWN,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogPostsMainService.deleteBlogPost(
                    authorization = "Token ${authToken.token!!}",
                    slug = blogPost.slug
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.deleteBlogPost(blogPost)
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                SUCCESS_BLOG_DELETED,
                                ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob(
                    methodName = "deleteBlogPost",
                    job = job
                )
            }

        }.asLiveData()

    }

    fun updateBLogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, BlogViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            // not applicable
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                val updatedBLogPost = BlogPost(
                    pk = response.body.pk,
                    title = response.body.title,
                    slug = response.body.slug,
                    body = response.body.body,
                    image = response.body.image,
                    date_updated = DateUtil.convertServerStringDateToLong(
                        response.body.date_updated
                    ),
                    username = response.body.username
                )
                updateLocalDb(updatedBLogPost)
                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            data = BlogViewState(
                                viewBlogFields = ViewBlogFields(
                                    blogPost = updatedBLogPost
                                )
                            ),
                            response = Response(
                                response.body.response,
                                ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return blogPostsMainService.updateBlog(
                    authorization = "Token ${authToken.token}",
                    slug = slug,
                    title = title,
                    body = body,
                    image = image
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.updateBlogPost(
                        pk = blogPost.pk,
                        title = blogPost.title,
                        body = blogPost.body,
                        image = blogPost.image
                    )
                }
            }

            override fun setJob(job: Job) {
                addJob(
                    methodName = "updateBlogPost",
                    job = job
                )
            }

        }.asLiveData()
    }


}