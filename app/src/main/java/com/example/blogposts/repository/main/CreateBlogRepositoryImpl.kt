package com.example.blogposts.repository.main

import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.api.main.responses.BlogCreateUpdateResponse
import com.example.blogposts.di.main.MainScope
import com.example.blogposts.models.AuthToken
import com.example.blogposts.persistesnce.BlogPostDao
import com.example.blogposts.repository.safeApiCall
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.main.create_blog.state.CreateBlogViewState
import com.example.blogposts.utils.*
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER

import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow


@FlowPreview
@MainScope
class CreateBlogRepositoryImpl
@Inject
constructor(
    val blogPostsMainService: BlogPostsMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): CreateBlogRepository {


    override fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow{

        val apiResult = safeApiCall(IO){
            blogPostsMainService.createBlog(
                "Token ${authToken.token!!}",
                title,
                body,
                image
            )
        }

        emit(
            object: ApiResponseHandler<CreateBlogViewState, BlogCreateUpdateResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<CreateBlogViewState> {

                    // If they don't have a paid membership account it will still return a 200
                    // Need to account for that
                    if (resultObj.response != RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER) {
                        val updatedBlogPost = resultObj.toBlogPost()
                        blogPostDao.insert(updatedBlogPost)
                    }
                    return DataState.data(
                        response = Response(
                            message = resultObj.response,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

}


