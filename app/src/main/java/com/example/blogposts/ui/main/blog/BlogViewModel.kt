package com.example.blogposts.ui.main.blog

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.example.blogposts.repository.main.BlogRepository
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.main.blog.state.BlogStateEvent
import com.example.blogposts.ui.main.blog.state.BlogStateEvent.*
import com.example.blogposts.ui.main.blog.state.BlogViewState
import com.example.blogposts.ui.main.blog.viewmodel.getPage
import com.example.blogposts.ui.main.blog.viewmodel.getSearchQuery
import com.example.blogposts.utils.AbsentLiveData
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val requestManager: RequestManager
) : BaseViewModel<BlogStateEvent, BlogViewState>() {

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        return when (stateEvent) {
            is BlogSearchEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken = authToken,
                        query = getSearchQuery(),
                        page = getPage()
                    )
                } ?: AbsentLiveData.create()
            }

            is CheckAuthorBlogPostsEvent -> {
                AbsentLiveData.create()
            }

            is None -> {
                AbsentLiveData.create()
            }

        }
    }

    override fun initViewState(): BlogViewState {
        return BlogViewState()
    }


    fun cancelActiveJobs() {
        blogRepository.cancelActiveJobs()
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
