package com.example.blogposts.ui

import com.example.blogposts.utils.DataState

interface DataStateChangeListener {

    fun onDataStateChange(dataState: DataState<*>?)

}