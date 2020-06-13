package com.example.blogposts.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.blogposts.R
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.main.account.state.AccountStateEvent
import com.example.blogposts.utils.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import kotlinx.android.synthetic.main.fragment_change_password.*

class ChangePasswordFragment : BaseAccountFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update_password_button.setOnClickListener {
            viewModel.setStateEvent(
                AccountStateEvent.ChangePasswordEvent(
                    currentPassword = input_current_password.text.toString(),
                    newPassword = input_new_password.text.toString(),
                    confirmNewPassword = input_confirm_new_password.text.toString()
                )
            )
        }
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)
                Log.d(TAG, "ChangePasswordFragment,DataState: $dataState")
                dataState.data?.let { data ->
                    data.response?.let { event ->
                        if (event.peekContent()
                                .message
                                .equals(RESPONSE_PASSWORD_UPDATE_SUCCESS)
                        ) {
                            stateChangeListener.hideSoftKeyboard()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })
    }

}