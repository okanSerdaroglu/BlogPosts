package com.example.blogposts.ui

import android.util.Log
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.ResponseType.*
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(),
    DataStateChangeListener {
    val TAG: String = "AppDebug"

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onDataStateChange(dataState: DataState<*>?) {

        dataState?.let { dataState ->
            GlobalScope.launch(Main) {
                displayProgressBar(dataState.loading.isLoading)
                dataState.error?.let { errorEvent ->
                    handleStateError(errorEvent)
                }

                dataState.data?.let {
                    it.response?.let { responseEvent ->
                        handleStateResponse(responseEvent)
                    }
                }

            }
        }

    }

    private fun handleStateError(errorEvent: Event<StateError>) {
        errorEvent.getContentIfNotHandled()?.let { stateError ->
            when (stateError.response.responseType) {
                is Toast -> {
                    stateError.response.message?.let { message ->
                        displayToast(message)
                    }
                }

                is Dialog -> {
                    stateError.response.message?.let { message ->
                        displayErrorDialog(message)
                    }
                }

                is None -> {
                    Log.d(TAG, "handleStateError: ${stateError.response.message}")
                }
            }
        }
    }

    private fun handleStateResponse(errorEvent: Event<Response>) {
        errorEvent.getContentIfNotHandled()?.let { response ->
            when (response.responseType) {
                is Toast -> {
                    response.message?.let { message ->
                        displayToast(message)
                    }
                }

                is Dialog -> {
                    response.message?.let { message ->
                        displayErrorDialog(message)
                    }
                }

                is None -> {
                    Log.d(TAG, "handleStateResponse: ${response.message}")
                }
            }
        }
    }


    abstract fun displayProgressBar(boolean: Boolean)

}