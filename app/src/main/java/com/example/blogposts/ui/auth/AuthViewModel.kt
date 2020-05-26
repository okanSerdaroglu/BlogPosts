package com.example.blogposts.ui.auth

import androidx.lifecycle.ViewModel
import com.example.blogposts.repository.auth.AuthRepository
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(val authRepository: AuthRepository) : ViewModel() {
}