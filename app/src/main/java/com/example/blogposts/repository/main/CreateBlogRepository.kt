package com.example.blogposts.repository.main

import androidx.lifecycle.LiveData
import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.api.main.responses.BlogCreateUpdateResponse
import com.example.blogposts.di.main.MainScope
import com.example.blogposts.models.AuthToken
import com.example.blogposts.models.BlogPost
import com.example.blogposts.persistesnce.BlogPostDao
import com.example.blogposts.repository.JobManager
import com.example.blogposts.repository.NetworkBoundResource
import com.example.blogposts.session.SessionManager
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.Response
import com.example.blogposts.utils.ResponseType
import com.example.blogposts.ui.main.create_blog.state.CreateBlogViewState
import com.example.blogposts.utils.ApiSuccessResponse
import com.example.blogposts.utils.DateUtil
import com.example.blogposts.utils.GenericApiResponse
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@MainScope
class CreateBlogRepository
@Inject
constructor(
    val blogPostsMainService: BlogPostsMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
) : JobManager("CreateBlogRepository") {
    private val TAG: String = "CreateBlogRepository"

    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateBlogViewState>> {
        return object :
            NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
                isNetworkAvailable = sessionManager.isConnectedToInternet(),
                isNetworkRequest = true,
                shouldCancelIfNoInternet = true,
                shouldLoadFromCache = false
            ) {
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                if (response.body.response != RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER) {
                    val updateBlogPost = BlogPost(
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
                    updateLocalDb(updateBlogPost)
                }
                withContext(Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                response.body.response,
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return blogPostsMainService.createBlog(
                    authorization = "Token ${authToken.token}",
                    title = title,
                    body = body,
                    image = image
                )
            }

            override fun loadFromCache(): LiveData<CreateBlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let { blogPost ->
                    blogPostDao.insert(blogPost)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewBlogPost", job)
            }

        }.asLiveData()
    }

}
