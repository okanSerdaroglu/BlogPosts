package com.example.blogposts.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.blogposts.repository.main.CreateBlogRepositoryImpl
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.Loading
import com.example.blogposts.ui.main.create_blog.state.CreateBlogStateEvent
import com.example.blogposts.ui.main.create_blog.state.CreateBlogStateEvent.*
import com.example.blogposts.ui.main.create_blog.state.CreateBlogViewState
import com.example.blogposts.ui.main.create_blog.state.CreateBlogViewState.*
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogViewModel
@Inject
constructor(
    val createBlogRepository: CreateBlogRepositoryImpl,
    val sessionManager: SessionManager
) : BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {
    override fun handleStateEvent(stateEvent: CreateBlogStateEvent): LiveData<DataState<CreateBlogViewState>> {

        when (stateEvent) {
            is CreateNewBlogEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->

                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )

                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    createBlogRepository.createNewBlogPost(
                        authToken = authToken,
                        title = title,
                        body = body,
                        image = stateEvent.image
                    )

                }?:AbsentLiveData.create()
            }

            is None -> {
                return liveData {
                    emit(
                        DataState(
                            error = null,
                            loading = Loading(false),
                            data = null
                        )
                    )
                }
            }

        }

    }

    override fun initViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let { newBlogFields.newBlogTitle = it }
        body?.let { newBlogFields.newBlogBody = it }
        uri?.let { newBlogFields.newImageUri = it }
        update.blogFields = newBlogFields
        setViewState(update)
    }


    fun getNewImageUri():Uri?{
        getCurrentViewStateOrNew().let {viewState->
            viewState.blogFields.let {newBlogFields->
                return newBlogFields.newImageUri
            }
        }
    }

    fun clearNewBlogFields() {
        val update = getCurrentViewStateOrNew()
        update.blogFields = NewBlogFields()
        setViewState(update)
    }

    fun cancelActiveJobs() {
        createBlogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}