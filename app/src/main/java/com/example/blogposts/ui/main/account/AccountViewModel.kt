package com.example.blogposts.ui.main.account

import androidx.lifecycle.LiveData
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.repository.main.AccountRepository
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.main.account.state.AccountStateEvent
import com.example.blogposts.ui.main.account.state.AccountStateEvent.*
import com.example.blogposts.ui.main.account.state.AccountViewState
import com.example.blogposts.utils.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {
    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when (stateEvent) {
            is GetAccountPropertiesEvent -> {
                return AbsentLiveData.create()
            }

            is UpdateAccountPropertiesEvent -> {
                return AbsentLiveData.create()
            }

            is ChangePasswordEvent -> {
                return AbsentLiveData.create()
            }

            is None -> {
                return AbsentLiveData.create()
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

        update.accountProperties == accountProperties
        _viewState.value = update

    }

    fun logout() {
        sessionManager.logOut()
    }

}