package com.example.blogposts.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.blogposts.R
import com.example.blogposts.di.auth.AuthScope
import com.example.blogposts.ui.auth.state.AuthStateEvent
import com.example.blogposts.ui.auth.state.RegistrationFields

import kotlinx.android.synthetic.main.fragment_change_password.*
import kotlinx.android.synthetic.main.fragment_register.*
import javax.inject.Inject

@AuthScope
class RegisterFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : Fragment(R.layout.fragment_register) {

    val viewModel: AuthViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.cancelActiveJobs()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register_button.setOnClickListener {
            register()
        }
        subscribeObservers()


    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.registrationFields?.let { registerFields ->
                registerFields.registrationEmail?.let { email ->
                    input_email.setText(email)
                }
                registerFields.registrationUsername?.let { username ->
                    input_username.setText(username)
                }
                registerFields.registrationPassword?.let { password ->
                    input_password.setText(password)
                }
                registerFields.registrationConfirmPassword?.let { confirmPassword ->
                    input_confirm_new_password.setText(confirmPassword)
                }
            }
        })
    }

    fun register() {
        viewModel.setStateEvent(
            AuthStateEvent.RegisterAttemptEvent(
                input_email.toString(),
                input_password.toString(),
                input_password.toString(),
                input_password_confirm.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setRegistrationFields(
            RegistrationFields(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            )
        )
    }
}