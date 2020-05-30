package com.example.blogposts.ui.auth

import androidx.lifecycle.LiveData
import com.example.blogposts.models.AuthToken

import com.example.blogposts.repository.auth.AuthRepository
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.auth.state.AuthStateEvent
import com.example.blogposts.ui.auth.state.AuthStateEvent.*
import com.example.blogposts.ui.auth.state.AuthViewState
import com.example.blogposts.ui.auth.state.LoginFields
import com.example.blogposts.ui.auth.state.RegistrationFields
import com.example.blogposts.utils.AbsentLiveData
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(val authRepository: AuthRepository) : BaseViewModel<AuthStateEvent, AuthViewState>() {


    @InternalCoroutinesApi
    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        return when (stateEvent) {
            is LoginAttemptEvent -> {
                return authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }

            is RegisterAttemptEvent -> {
                return authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirmPassword
                )
            }

            is CheckPreviousAuthEvent -> {
                AbsentLiveData.create()
            }

        }
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        _viewState.value = update
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        _viewState.value = update
    }

    override fun initViewState(): AuthViewState {
        return AuthViewState()
    }


    fun cancelActiveJobs() {
        authRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}