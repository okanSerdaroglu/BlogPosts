package com.example.blogposts.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.blogposts.api.auth.network_responses.LoginResponse
import com.example.blogposts.api.auth.network_responses.RegistrationResponse
import com.example.blogposts.repository.auth.AuthRepository
import com.example.blogposts.utils.GenericApiResponse
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(val authRepository: AuthRepository) : ViewModel() {

    fun testLogin(): LiveData<GenericApiResponse<LoginResponse>> {
        return authRepository
            .testLoginRequest(
                "your_email",
                "your_password"
            )
    }

    fun testRegister(): LiveData<GenericApiResponse<RegistrationResponse>> {
        return authRepository.testRegistrationRequest(
            "your_email",
            "your_userName",
            "your_password",
            "your_password"
        )
    }

}