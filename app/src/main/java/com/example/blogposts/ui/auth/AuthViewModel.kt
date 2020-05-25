package com.example.blogposts.ui.auth

import androidx.lifecycle.ViewModel
import com.example.blogposts.repository.auth.AuthRepository

class AuthViewModel
constructor(val authRepository: AuthRepository) : ViewModel() {
}