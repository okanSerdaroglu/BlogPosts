package com.example.blogposts.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.blogposts.BaseApplication
import com.example.blogposts.R
import com.example.blogposts.fragments.auth.AuthNavHostFragment
import com.example.blogposts.ui.BaseActivity
import com.example.blogposts.ui.auth.state.AuthStateEvent
import com.example.blogposts.ui.main.MainActivity
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.android.synthetic.main.activity_auth.*
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    val viewModel: AuthViewModel by viewModels {
        providerFactory
    }

    private fun onRestoreInstanceState() {
        val host = supportFragmentManager
            .findFragmentById(R.id.auth_fragments_container)
        host?.let {
            // do nothing
        } ?: createNavHost()
    }

    private fun createNavHost() {
        val navHost = AuthNavHostFragment.create(
            R.navigation.nav_graph_auth
        )
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.auth_fragments_container,
                navHost,
                getString(R.string.AuthNavHost)
            )
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }

    override fun inject() {
        (application as BaseApplication).authComponent()
            .inject(this)
    }

    override fun displayProgressBar(boolean: Boolean) {
        if (boolean) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        subscribeObservers()
        onRestoreInstanceState()
    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    private fun checkPreviousAuthUser() {
        viewModel.setStateEvent(
            event = AuthStateEvent.CheckPreviousAuthEvent()
        )
    }

    private fun subscribeObservers() {

        viewModel.dataState.observe(this, Observer { dataState ->
            onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        it.authToken?.let {
                            Log.d(TAG, "AuthActivity, DataState: ${it}")
                            viewModel.setAuthToken(it)
                        }
                    }
                }
                data.response?.let { event ->
                    event.peekContent().let { response ->
                        response.message?.let { message ->
                            if (message.equals(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE)) {
                                onFinishCheckPreviousAuthUser()
                            }
                        }
                    }
                }
            }
        })



        viewModel.viewState.observe(this, Observer { authViewState ->
            authViewState.authToken?.let { authToken ->
                sessionManager.login(authToken)
            }
        })
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Log.d(TAG, "AuthActivity : subscribeObservers: AuthToken: $authToken")
            if (authToken != null
                && authToken.account_pk != -1
                && authToken.token != null
            ) {
                navMainActivity()
            }
        })
    }

    private fun onFinishCheckPreviousAuthUser() {
        fragment_container.visibility = View.VISIBLE
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        (application as BaseApplication).releaseAuthComponent()
    }

    override fun expandAppbar() {
        TODO("Not yet implemented")
    }

}
