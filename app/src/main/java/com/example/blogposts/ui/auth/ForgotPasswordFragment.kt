package com.example.blogposts.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.blogposts.R
import com.example.blogposts.di.auth.AuthScope
import com.example.blogposts.utils.DataState
import com.example.blogposts.ui.DataStateChangeListener
import com.example.blogposts.utils.Response
import com.example.blogposts.utils.ResponseType
import com.example.blogposts.ui.auth.ForgotPasswordFragment.WebAppInterface.OnWebInteractionCallback
import com.example.blogposts.utils.Constants.Companion.PASSWORD_RESET_URL
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ClassCastException
import javax.inject.Inject

@AuthScope
class ForgotPasswordFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_forgot_password) {

    private val TAG = "ForgotPasswordFragment"

    val viewModel: AuthViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
    }

    lateinit var vebView: WebView

    lateinit var stateChangeListener: DataStateChangeListener

    val webInteractionCallback: OnWebInteractionCallback = object : OnWebInteractionCallback {
        override fun onSuccess(email: String) {
            Log.d(TAG, "onSuccess: reset link will be sent to $email")
            onPasswordResetLinkSent()
        }

        override fun onError(errorMessage: String) {

            Log.e(TAG, errorMessage)
            val dataState = DataState.error<Any>(
                response = Response(
                    errorMessage,
                    ResponseType.Dialog()
                )
            )

            stateChangeListener.onDataStateChange(
                dataState = dataState
            )


        }

        override fun onLoading(isLoading: Boolean) {
            Log.d(TAG, "onLoading...")
            GlobalScope.launch(Main) {
                stateChangeListener.onDataStateChange(
                    DataState.loading(isLoading = isLoading, cachedData = null)
                )
            }
        }
    }

    private fun onPasswordResetLinkSent() {
        GlobalScope.launch(Main) {
            parent_view.removeView(vebView)
            vebView.destroy()

            val animation = TranslateAnimation(
                password_reset_done_container.width.toFloat(),
                0f,
                0f,
                0f
            )
            animation.duration = 500
            password_reset_done_container.startAnimation(animation)
            password_reset_done_container.visibility = View.VISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must be implement DataStateChangeListener.")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadPasswordResetWebView() {
        stateChangeListener.onDataStateChange(
            DataState.loading(isLoading = true, cachedData = null)
        )
        vebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                stateChangeListener.onDataStateChange(
                    DataState.loading(
                        isLoading = false,
                        cachedData = null
                    )
                )
            }
        }
        webview.loadUrl(PASSWORD_RESET_URL)
        webview.settings.javaScriptEnabled = true
        webview.addJavascriptInterface(
            WebAppInterface(callback = webInteractionCallback),
            "AndroidTextListener"
        )
    }

    class WebAppInterface
    constructor(
        private val callback: OnWebInteractionCallback
    ) {

        private val TAG: String = "AppDebug"

        @JavascriptInterface
        fun onSuccess(email: String) {
            callback.onSuccess(email = email)
        }

        @JavascriptInterface
        fun onError(errorMessage: String) {
            callback.onError(errorMessage = errorMessage)
        }

        @JavascriptInterface
        fun onLoading(isLoading: Boolean) {
            callback.onLoading(isLoading = isLoading)
        }


        interface OnWebInteractionCallback {

            fun onSuccess(email: String)

            fun onError(errorMessage: String)

            fun onLoading(isLoading: Boolean)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vebView = view.findViewById(R.id.webview)
        loadPasswordResetWebView()
        return_to_launcher_fragment.setOnClickListener {
            findNavController().popBackStack()
        }
        Log.d(TAG, "ForgotPasswordFragment: ${viewModel.hashCode()}")
    }

}