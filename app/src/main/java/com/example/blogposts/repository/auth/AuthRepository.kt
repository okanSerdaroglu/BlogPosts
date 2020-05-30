package com.example.blogposts.repository.auth

import android.util.Log
import androidx.lifecycle.LiveData

import com.example.blogposts.api.auth.BlogPostsAuthService
import com.example.blogposts.api.auth.network_responses.LoginResponse
import com.example.blogposts.api.auth.network_responses.RegistrationResponse
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

import com.example.blogposts.utils.ApiSuccessResponse
import com.example.blogposts.utils.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.example.blogposts.utils.GenericApiResponse
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogPostsAuthService: BlogPostsAuthService,
    val sessionManager: SessionManager
) {

    private var TAG = "AuthRepository"
    private var repositoryJob: Job? = null

    @InternalCoroutinesApi
    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (loginFieldErrors != LoginFields.LoginError.none()) {
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }
        return object : NetworkBoundResource<LoginResponse, AuthViewState>(
            sessionManager.isConnectedToInternet()
        ) {
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

        }.asLiveData()
    }

    fun cancelActiveJobs() {
        Log.d(TAG, "AuthRepository: Cancelling on going jobs...")
        repositoryJob?.cancel()
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

    @InternalCoroutinesApi
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

        return object : NetworkBoundResource<RegistrationResponse, AuthViewState>(
            sessionManager.isConnectedToInternet()
        ) {
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
        }.asLiveData()
    }

}