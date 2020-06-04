package com.example.blogposts.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.Response
import com.example.blogposts.ui.ResponseType
import com.example.blogposts.utils.*
import com.example.blogposts.utils.Constants.Companion.NETWORK_TIMEOUT
import com.example.blogposts.utils.Constants.Companion.TESTING_CACHE_DELAY
import com.example.blogposts.utils.Constants.Companion.TESTING_NETWORK_DELAY
import com.example.blogposts.utils.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.example.blogposts.utils.ErrorHandling.Companion.ERROR_UNKNOWN
import com.example.blogposts.utils.ErrorHandling.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.example.blogposts.utils.ErrorHandling.Companion.UNABLE_TO_RESOLVE_HOST
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

abstract class NetworkBoundResource<ResponseObject, CacheObject, ViewStateType>(
    isNetworkAvailable: Boolean, // is there a network connection
    isNetworkRequest: Boolean, // is this a network request
    shouldCancelIfNoInternet: Boolean, // should this job be cancelled if there is no network
    shouldLoadFromCache: Boolean // should the cache data be loaded?
) {

    val TAG: String = "AppDebug"

    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope


    init {
        setJob(initNewJob())
        setValue(DataState.loading(isLoading = true, cachedData = null))

        if (shouldLoadFromCache) {
            val dbSource = loadFromCache()
            result.addSource(dbSource) { viewStateType ->
                result.removeSource(dbSource)
                setValue(
                    DataState.loading(
                        isLoading = true,
                        cachedData = viewStateType
                    )
                )
            }
        }

        if (isNetworkRequest) {
            if (isNetworkAvailable) {
                doNetworkRequest()
            } else {
                if (shouldCancelIfNoInternet) {
                    onErrorReturn(
                        UNABLE_TODO_OPERATION_WO_INTERNET,
                        shouldUseDialog = true,
                        shouldUseToast = false
                    )
                } else {
                    doCacheRequest()
                }
            }
        } else {
            doCacheRequest()
        }


    }

    private fun doCacheRequest() {
        coroutineScope.launch {
            // fake delay or testing cache
            delay(TESTING_CACHE_DELAY)

            // View data from cache only and return
            createCacheRequestAndReturn()
        }
    }

    private fun doNetworkRequest() {
        coroutineScope.launch {
            // simulate a network delay for testing
            delay(TESTING_NETWORK_DELAY)
            withContext(Main) {
                // make network call
                val apiResponse = createCall()
                result.addSource(apiResponse) { response ->
                    result.removeSource(apiResponse)
                    coroutineScope.launch {
                        handleNetworkCall(response)
                    }
                }
            }
        }
        GlobalScope.launch(IO) {
            delay(NETWORK_TIMEOUT)
            if (!job.isCompleted) {
                Log.e(TAG, "NetworkBoundResource: JOB NETWORK TIMEOUT.")
                job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
            }
        }
    }


    suspend fun handleNetworkCall(response: GenericApiResponse<ResponseObject>?) {

        when (response) {
            is ApiSuccessResponse -> {
                handleApiSuccessResponse(response)
            }

            is ApiErrorResponse -> {
                Log.e(TAG, "NetworkBoundResource: ${response.errorMessage}")
                onErrorReturn(
                    response.errorMessage,
                    shouldUseDialog = true,
                    shouldUseToast = false
                )
            }

            is ApiEmptyResponse -> {
                Log.e(TAG, "NetworkBoundResource: REQUEST RETURN NOTHING")
                onErrorReturn(
                    "HTTP 204. RETURNED NOTHING",
                    shouldUseDialog = true,
                    shouldUseToast = false
                )

            }
        }
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dataState)
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean) {
        var message = errorMessage
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()
        if (message == null) {
            message = ERROR_UNKNOWN
        } else if (ErrorHandling.isNetworkError(message)) {
            message = ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false
        }

        if (shouldUseToast) {
            responseType = ResponseType.Toast()
        }

        if (useDialog) {
            responseType = ResponseType.Dialog()
        }

        onCompleteJob(
            DataState.error(
                response = Response(
                    message = message,
                    responseType = responseType
                )
            )
        )

    }


    @OptIn(InternalCoroutinesApi::class)
    private fun initNewJob(): Job {
        Log.d(TAG, "initNewJob: called...")
        job = Job()
        job.invokeOnCompletion(onCancelling = true, // this method called when job completed or cancelled
            invokeImmediately = true,
            handler = object : CompletionHandler {
                override fun invoke(cause: Throwable?) {
                    if (job.isCancelled) {
                        Log.e(TAG, "NetworkBoundResource: Job has been cancelled.")
                        cause?.let { throwable ->
                            // TODO("show error dialog")
                            onErrorReturn(
                                throwable.message,
                                shouldUseDialog = false,
                                shouldUseToast = true
                            )
                        } ?: onErrorReturn(
                            ERROR_UNKNOWN,
                            shouldUseDialog = false,
                            shouldUseToast = true
                        )
                    } else if (job.isCompleted) {
                        Log.e(TAG, "NetworkBoundResource: Job has been completed...")
                        // Do nothing. Should be handled already.
                    }
                }
            })
        coroutineScope = CoroutineScope(IO + job) // IO + job means background thread and a job
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun createCacheRequestAndReturn()

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)

    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>

    abstract fun loadFromCache(): LiveData<ViewStateType>

    abstract suspend fun updateLocalDb(cacheObject: CacheObject?)

    abstract fun setJob(job: Job)

}
