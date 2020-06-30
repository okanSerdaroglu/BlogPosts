package com.example.blogposts.repository.auth

import android.content.SharedPreferences
import android.util.Log
import com.example.blogposts.api.auth.BlogPostsAuthService
import com.example.blogposts.api.auth.network_responses.LoginResponse
import com.example.blogposts.api.auth.network_responses.RegistrationResponse
import com.example.blogposts.di.auth.AuthScope
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.models.AuthToken
import com.example.blogposts.persistesnce.AccountPropertiesDao
import com.example.blogposts.persistesnce.AuthTokenDao
import com.example.blogposts.repository.buildError
import com.example.blogposts.repository.safeApiCall
import com.example.blogposts.repository.safeCacheCall
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.auth.state.AuthViewState
import com.example.blogposts.ui.auth.state.LoginFields
import com.example.blogposts.ui.auth.state.RegistrationFields
import com.example.blogposts.utils.*
import com.example.blogposts.utils.ErrorHandling.Companion.ERROR_SAVE_ACCOUNT_PROPERTIES
import com.example.blogposts.utils.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.example.blogposts.utils.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.example.blogposts.utils.ErrorHandling.Companion.INVALID_CREDENTIALS
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@FlowPreview
@AuthScope
class AuthRepositoryImpl
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    private val blogPostsAuthService: BlogPostsAuthService,
    val sessionManager: SessionManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : AuthRepository {

    private val tag: String = "AppDebug"

    override fun attemptLogin(
        stateEvent: StateEvent,
        email: String,
        password: String
    ): Flow<DataState<AuthViewState>> = flow {

        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (loginFieldErrors == LoginFields.LoginError.none()) {
            val apiResult = safeApiCall(IO) {
                blogPostsAuthService.login(email, password)
            }
            emit(
                object : ApiResponseHandler<AuthViewState, LoginResponse>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: LoginResponse): DataState<AuthViewState> {

                        Log.d(tag, "handleSuccess ")

                        // Incorrect login credentials counts as a 200 response from server, so need to handle that
                        if (resultObj.response == GENERIC_AUTH_ERROR) {
                            return DataState.error(
                                response = Response(
                                    INVALID_CREDENTIALS,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        accountPropertiesDao.insertOrIgnore(
                            AccountProperties(
                                resultObj.pk,
                                resultObj.email,
                                ""
                            )
                        )

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.pk,
                            resultObj.token
                        )
                        val result = authTokenDao.insert(authToken)
                        if (result < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(email)

                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }

                }.getResult()
            )
        } else {
            Log.d(tag, "emitting error: $loginFieldErrors")
            emit(
                buildError(
                    loginFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }
    }

    override fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>> = flow {
        val registrationFieldErrors =
            RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if (registrationFieldErrors == RegistrationFields.RegistrationError.none()) {

            val apiResult = safeApiCall(IO) {
                blogPostsAuthService.register(
                    email,
                    username,
                    password,
                    confirmPassword
                )
            }
            emit(
                object : ApiResponseHandler<AuthViewState, RegistrationResponse>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: RegistrationResponse): DataState<AuthViewState> {
                        if (resultObj.response == GENERIC_AUTH_ERROR) {
                            return DataState.error(
                                response = Response(
                                    resultObj.errorMessage,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        val result1 = accountPropertiesDao.insertAndReplace(
                            AccountProperties(
                                resultObj.pk,
                                resultObj.email,
                                resultObj.username
                            )
                        )
                        // will return -1 if failure
                        if (result1 < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_ACCOUNT_PROPERTIES,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.pk,
                            resultObj.token
                        )
                        val result2 = authTokenDao.insert(authToken)
                        if (result2 < 0) {
                            return DataState.error(
                                response = Response(
                                    ERROR_SAVE_AUTH_TOKEN,
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(email)
                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }
                }.getResult()
            )

        } else {
            emit(
                buildError(
                    registrationFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }

    }


    override fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>> = flow {
        Log.d(tag, "checkPreviousAuthUser: ")
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(tag, "checkPreviousAuthUser: No previously authenticated user found.")
            emit(returnNoTokenFound(stateEvent))
        } else {
            val apiResult = safeCacheCall(IO) {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail)
            }
            emit(
                object : CacheResponseHandler<AuthViewState, AccountProperties>(
                    response = apiResult,
                    stateEvent = stateEvent
                ) {
                    override suspend fun handleSuccess(resultObj: AccountProperties): DataState<AuthViewState> {

                        if (resultObj.pk > -1) {
                            authTokenDao.searchByPk(resultObj.pk).let { authToken ->
                                if (authToken != null) {
                                    if (authToken.token != null) {
                                        return DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            ),
                                            response = null,
                                            stateEvent = stateEvent
                                        )
                                    }
                                }
                            }
                        }
                        Log.d(tag, "createCacheRequestAndReturn: AuthToken not found...")
                        return DataState.error(
                            response = Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                UIComponentType.None(),
                                MessageType.Error()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                }.getResult()
            )
        }
    }

    override fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    override fun returnNoTokenFound(
        stateEvent: StateEvent
    ): DataState<AuthViewState> {

        return DataState.error(
            response = Response(
                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                UIComponentType.None(),
                MessageType.Error()
            ),
            stateEvent = stateEvent
        )
    }


}