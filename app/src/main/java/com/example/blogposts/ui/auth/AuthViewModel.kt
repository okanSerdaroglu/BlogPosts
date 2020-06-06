package com.example.blogposts.ui.auth

import androidx.lifecycle.LiveData
import com.example.blogposts.models.AuthToken

import com.example.blogposts.repository.auth.AuthRepository
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.Loading
import com.example.blogposts.ui.auth.state.AuthStateEvent
import com.example.blogposts.ui.auth.state.AuthStateEvent.*
import com.example.blogposts.ui.auth.state.AuthViewState
import com.example.blogposts.ui.auth.state.LoginFields
import com.example.blogposts.ui.auth.state.RegistrationFields


import javax.inject.Inject

class AuthViewModel
@Inject
constructor(val authRepository: AuthRepository) : BaseViewModel<AuthStateEvent, AuthViewState>() {


    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        return when (stateEvent) {
            is LoginAttemptEvent -> {
                authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }

            is RegisterAttemptEvent -> {
                authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirmPassword
                )
            }

            is CheckPreviousAuthEvent -> {
                authRepository.checkPreviousAuthUser()
            }

            is None -> {
                return object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            error = null,
                            loading = Loading(false),
                            data = null
                        )
                    }
                }
            }
        }
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        setViewState(update)
    }

    override fun initViewState(): AuthViewState {
        return AuthViewState()
    }


    fun cancelActiveJobs() {
        handlePendingData()
        authRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

}