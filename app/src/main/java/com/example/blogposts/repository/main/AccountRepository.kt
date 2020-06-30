package com.example.blogposts.repository.main

import androidx.lifecycle.LiveData
import com.example.blogposts.api.GenericResponse
import com.example.blogposts.api.main.BlogPostsMainService
import com.example.blogposts.di.main.MainScope
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.models.AuthToken
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.repository.NetworkBoundResource
import com.example.blogposts.session.SessionManager
import com.example.blogposts.utils.DataState
import com.example.blogposts.utils.Response
import com.example.blogposts.utils.ResponseType
import com.example.blogposts.ui.main.account.state.AccountViewState
import com.example.blogposts.utils.ApiSuccessResponse
import com.example.blogposts.utils.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

@MainScope
class AccountRepository
@Inject
constructor(
    val blogPostsMainService: BlogPostsMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) : JobManager("AccountRepository") {
    private val TAG: String = "AppDebug"


    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object :
            NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.isConnectedToInternet(),
                isNetworkRequest = true,
                shouldCancelIfNoInternet = false,
                shouldLoadFromCache = true
            ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    // finish by viewing the db cache
                    result.addSource(loadFromCache()) { viewState ->
                        onCompleteJob(
                            DataState.data(
                                data = viewState,
                                response = null
                            )
                        )
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)

                // finish by viewing the cache
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return blogPostsMainService
                    .getAccountProperties(
                        "Token ${authToken.token}"
                    )
            }

            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                        object : LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }
            }

        }.asLiveData()
    }

    fun saveAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            // not used in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null)
                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                response.body.response,
                                ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogPostsMainService.saveAccountProperties(
                    "Token ${authToken.token!!}",
                    accountProperties.email,
                    accountProperties.username
                )
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    pk = accountProperties.pk,
                    email = accountProperties.email,
                    username = accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }
        }.asLiveData()
    }

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConnectedToInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            // not applicable
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                message = response.body.response,
                                responseType = ResponseType.Toast()
                            )

                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogPostsMainService.updatePassword(
                    "Token ${authToken.token!!}",
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmNewPassword = confirmNewPassword
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            //not applicable
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)
            }

        }.asLiveData()
    }

}