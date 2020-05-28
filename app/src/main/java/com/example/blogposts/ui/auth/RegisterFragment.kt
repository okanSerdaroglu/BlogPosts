package com.example.blogposts.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.blogposts.R
import com.example.blogposts.ui.auth.state.AuthStateEvent
import com.example.blogposts.ui.auth.state.RegistrationFields

import kotlinx.android.synthetic.main.fragment_change_password.*
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "RegisterFragment: ${viewModel.hashCode()}")
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
                input_confirm_new_password.text.toString()
            )
        )
    }
}