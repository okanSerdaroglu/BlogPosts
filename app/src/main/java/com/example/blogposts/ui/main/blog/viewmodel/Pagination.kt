package com.example.blogposts.ui.main.blog.viewmodel

import android.util.Log
import com.example.blogposts.ui.main.blog.BlogViewModel
import com.example.blogposts.ui.main.blog.state.BlogStateEvent.*

fun BlogViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

fun BlogViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogSearchEvent())
}

fun BlogViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().blogFields.page
    update.blogFields.page = page + 1
    setViewState(update)
}

fun BlogViewModel.nextPage() {
    if (!getIsQueryExhausted()
        && !getIsQueryInProgress()
    ) {
        Log.d(TAG, "BlogViewModel:Attempting to load next page...")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(BlogSearchEvent())
    }
}