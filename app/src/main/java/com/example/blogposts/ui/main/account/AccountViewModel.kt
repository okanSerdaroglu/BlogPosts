package com.example.blogposts.ui.main.account

import androidx.lifecycle.LiveData
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.repository.main.AccountRepositoryImpl
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.Loading
import com.example.blogposts.ui.main.account.state.AccountStateEvent
import com.example.blogposts.ui.main.account.state.AccountStateEvent.*
import com.example.blogposts.ui.main.account.state.AccountViewState
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepositoryImpl
) : BaseViewModel<AccountStateEvent, AccountViewState>() {

    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when (stateEvent) {
            is GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(
                        authToken = authToken
                    )
                } ?: AbsentLiveData.create()
            }

            is UpdateAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { pk ->
                        accountRepository.saveAccountProperties(
                            authToken = authToken,
                            accountProperties = AccountProperties(
                                pk = pk,
                                email = stateEvent.email,
                                username = stateEvent.username
                            )
                        )
                    }
                } ?: AbsentLiveData.create()
            }

            is ChangePasswordEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.updatePassword(
                        authToken = authToken,
                        currentPassword = stateEvent.currentPassword,
                        newPassword = stateEvent.newPassword,
                        confirmNewPassword = stateEvent.confirmNewPassword
                    )
                } ?: AbsentLiveData.create()
            }

            is None -> {
                return object : LiveData<DataState<AccountViewState>>() {
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

    override fun initViewState(): AccountViewState {
        return AccountViewState()
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }

        update.accountProperties = accountProperties
        setViewState(update)


    }

    fun logout() {
        sessionManager.logOut()
    }

    fun cancelActiveJobs() {
        handlePendingData()
        accountRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

}