package com.example.blogposts.repository.auth

import com.example.blogposts.di.auth.AuthScope
import com.example.blogposts.ui.auth.state.AuthViewState
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@FlowPreview
@AuthScope
interface AuthRepository {

    fun attemptLogin(
        stateEvent: StateEvent,
        email: String,
        password: String
    ): Flow<DataState<AuthViewState>>

    fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>>

    fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>>

    fun saveAuthenticatedUserToPrefs(email: String)

    fun returnNoTokenFound(
        stateEvent: StateEvent
    ): DataState<AuthViewState>

}