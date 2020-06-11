package com.example.blogposts.ui

import android.Manifest.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.ResponseType.*
import com.example.blogposts.utils.Constants.Companion.PERMISSION_REQUEST_READ_STORAGE
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(),
    DataStateChangeListener, UICommunicationListener {
    val TAG: String = "AppDebug"

    override fun onUIMessageReceived(uiMessage: UIMessage) {
        when (uiMessage.uiMessage) {

            is UIMessageType.AreYouSureDialog -> {
                areYouSureDialog(
                    uiMessage.message,
                    uiMessage.uiMessage.callback
                )
            }

            is UIMessageType.Toast -> {
                displayToast(uiMessage.message)
            }

            is UIMessageType.Dialog -> {
                displayInfoDialog(uiMessage.message)
            }

            is UIMessageType.None -> {
                Log.d(TAG, "onUIMessageReceived: ${uiMessage.message}")
            }


        }
    }

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

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    override fun isStoragePermissionGranted(): Boolean {
        if (
            ContextCompat.checkSelfPermission(
                this,
                permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    permission.READ_EXTERNAL_STORAGE,
                    permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_READ_STORAGE
            )
            return false
        } else {
            // Permission has already been granted
            return true
        }
    }

}