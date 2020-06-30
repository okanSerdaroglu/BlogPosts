package com.example.blogposts.repository.main


import android.util.Log
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
import com.example.blogposts.repository.buildError
import com.example.blogposts.repository.safeApiCall
import com.example.blogposts.session.SessionManager
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.Response
import com.example.blogposts.ui.main.blog.state.BlogViewState
import com.example.blogposts.ui.main.blog.state.BlogViewState.*
import com.example.blogposts.utils.*
import com.example.blogposts.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import com.example.blogposts.utils.SuccessHandling.Companion.SUCCESS_BLOG_DELETED
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.lang.Exception
import javax.inject.Inject

@FlowPreview
@MainScope
class BlogRepositoryImpl
@Inject
constructor(
    val blogPostsMainService: BlogPostsMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): BlogRepository
{

    private val tag: String = "AppDebug"
    override fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>> {
        return object: NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            dispatcher = IO,
            stateEvent = stateEvent,
            apiCall = {
                blogPostsMainService.searchListBlogPosts(
                    "Token ${authToken.token!!}",
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            },
            cacheCall = {
                blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
            }
        ){
            override suspend fun updateCache(networkObject: BlogListSearchResponse) {
                val blogPostList = networkObject.toList()
                withContext(IO) {
                    for(blogPost in blogPostList){
                        try{
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Log.d(tag, "updateLocalDb: inserting blog: $blogPost")
                                blogPostDao.insert(blogPost)
                            }
                        }catch (e: Exception){
                            Log.e(tag, "updateLocalDb: error updating cache data on blog post with slug: ${blogPost.slug}. " +
                                    "${e.message}")
                            // Could send an error report here or something but I don't think you should throw an error to the UI
                            // Since there could be many blog posts being inserted/updated.
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<BlogPost>): DataState<BlogViewState> {
                val viewState = BlogViewState(
                    blogFields = BlogFields(
                        blogList = resultObj
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }

        }.result
    }

    override fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO){
            blogPostsMainService.isAuthorOfBlogPost(
                "Token ${authToken.token!!}",
                slug
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: GenericResponse): DataState<BlogViewState> {
                    val viewState = BlogViewState(
                        viewBlogFields = ViewBlogFields(
                            isAuthorOfBlogPost = false
                        )
                    )
                    return when (resultObj.response) {
                        RESPONSE_NO_PERMISSION_TO_EDIT -> {
                            DataState.data(
                                response = null,
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        }
                        RESPONSE_HAS_PERMISSION_TO_EDIT -> {
                            viewState.viewBlogFields.isAuthorOfBlogPost = true
                            DataState.data(
                                response = null,
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        }
                        else -> {
                            buildError(
                                ERROR_UNKNOWN,
                                UIComponentType.None(),
                                stateEvent
                            )
                        }
                    }
                }
            }.getResult()
        )
    }

    override fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost,
        stateEvent: StateEvent
    ) =  flow {
        val apiResult = safeApiCall(IO){
            blogPostsMainService.deleteBlogPost(
                "Token ${authToken.token!!}",
                blogPost.slug
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: GenericResponse): DataState<BlogViewState> {

                    if(resultObj.response == SUCCESS_BLOG_DELETED){
                        blogPostDao.deleteBlogPost(blogPost)
                        return DataState.data(
                            response = Response(
                                message = SUCCESS_BLOG_DELETED,
                                uiComponentType = UIComponentType.Toast(),
                                messageType = MessageType.Success()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                    else{
                        return buildError(
                            ERROR_UNKNOWN,
                            UIComponentType.Dialog(),
                            stateEvent
                        )
                    }
                }
            }.getResult()
        )
    }

    override fun updateBlogPost(
        authToken: AuthToken,
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow{

        val apiResult = safeApiCall(IO){
            blogPostsMainService.updateBlog(
                "Token ${authToken.token!!}",
                slug,
                title,
                body,
                image
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, BlogCreateUpdateResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<BlogViewState> {

                    val updatedBlogPost = resultObj.toBlogPost()

                    blogPostDao.updateBlogPost(
                        updatedBlogPost.pk,
                        updatedBlogPost.title,
                        updatedBlogPost.body,
                        updatedBlogPost.image
                    )

                    return DataState.data(
                        response = Response(
                            message = resultObj.response,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data =  BlogViewState(
                            viewBlogFields = ViewBlogFields(
                                blogPost = updatedBlogPost
                            ),
                            updatedBlogFields = UpdatedBlogFields(
                                updatedBlogTitle = updatedBlogPost.title,
                                updatedBlogBody = updatedBlogPost.body,
                                updatedImageUri = null
                            )
                        ),
                        stateEvent = stateEvent
                    )

                }

            }.getResult()
        )
    }



}