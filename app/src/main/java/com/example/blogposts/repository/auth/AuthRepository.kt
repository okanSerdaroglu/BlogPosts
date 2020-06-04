package com.example.blogposts.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData

import com.example.blogposts.api.auth.BlogPostsAuthService
import com.example.blogposts.api.auth.network_responses.LoginResponse
import com.example.blogposts.api.auth.network_responses.RegistrationResponse
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.models.AuthToken
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.persistesnce.AuthTokenDao
import com.example.blogposts.repository.NetworkBoundResource
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.Response
import com.example.blogposts.ui.ResponseType
import com.example.blogposts.ui.auth.state.AuthViewState
import com.example.blogposts.ui.auth.state.LoginFields
import com.example.blogposts.ui.auth.state.RegistrationFields
import com.example.blogposts.utils.AbsentLiveData

import com.example.blogposts.utils.ApiSuccessResponse
import com.example.blogposts.utils.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.example.blogposts.utils.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.example.blogposts.utils.GenericApiResponse
import com.example.blogposts.utils.PreferenceKeys
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostsAuthService: BlogPostsAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPreferencesEditor: SharedPreferences.Editor
) {

    private var TAG = "AuthRepository"
    private var repositoryJob: Job? = null

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (loginFieldErrors != LoginFields.LoginError.none()) {
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }
        return object : NetworkBoundResource<LoginResponse,Any, AuthViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = true,
            shouldLoadFromCache = false
        ) {

            // not used in this case
            override suspend fun createCacheRequestAndReturn() {}

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "handle api success response:${response}")
                // Incorrect login credentials counts as a 200 response from server, so need to handle that
                if (response.body.response == GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        response.body.errorMessage,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                // don't care about results. Just insert if it does not exist b/c foreign key relationship
                // with AuthToken table

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        pk = response.body.pk,
                        email = response.body.email,
                        username = ""
                    )
                )

                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )

                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(
                                response.body.pk,
                                response.body.token
                            )
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return blogPostsAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            // ignore
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // ignore
            override suspend fun updateLocalDb(cacheObject: Any?) {}


        }.asLiveData()
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPreferencesEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPreferencesEditor.apply()
    }

    fun cancelActiveJobs() {
        Log.d(TAG, "AuthRepository: Cancelling on going jobs...")
        repositoryJob?.cancel()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No Previously authenticated user found...")
            return returnNoTokenFound()
        }

        return object : NetworkBoundResource<Void, Any,AuthViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = false,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                    Log.d(TAG, "checkPreviousAuthUser: searching for token:$accountProperties")
                    accountProperties?.let {
                        if (accountProperties.pk > -1) {
                            authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                if (authToken != null) {
                                    onCompleteJob(
                                        DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            )
                                        )
                                    )
                                    return
                                }
                            }
                        }
                    }
                    Log.d(TAG, "checkPreviousAuthUser: AuthToken not found...")
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                ResponseType.None()
                            )
                        )
                    )
                }
            }

            // not used in this case
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {}

            // not used in this case
            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // ignore
            override suspend fun updateLocalDb(cacheObject: Any?) {}

        }.asLiveData()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }

    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType.Dialog
    ): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }

    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {
        val registrationFieldErrors =
            RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if (registrationFieldErrors != RegistrationFields.RegistrationError.none()) {
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<RegistrationResponse, Any,AuthViewState>(
            isNetworkAvailable = sessionManager.isConnectedToInternet(),
            isNetworkRequest = true,
            shouldLoadFromCache = false
        ) {

            override suspend fun createCacheRequestAndReturn() {
                // not used in this case
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Log.d(TAG, "handle api success response:${response}")
                // Incorrect login credentials counts as a 200 response from server, so need to handle that
                if (response.body.response == GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        response.body.errorMessage,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                // don't care about results. Just insert if it does not exist b/c foreign key relationship
                // with AuthToken table

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        pk = response.body.pk,
                        email = response.body.email,
                        username = ""
                    )
                )

                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )

                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(
                                response.body.pk,
                                response.body.token
                            )
                        )
                    )
                )

            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return blogPostsAuthService.register(
                    email = email,
                    username = username,
                    password = password,
                    password2 = confirmPassword
                )
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // ignore
            override suspend fun updateLocalDb(cacheObject: Any?) {}


        }.asLiveData()
    }

}