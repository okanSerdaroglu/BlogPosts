package com.example.blogposts.di

import com.example.blogposts.di.auth.AuthComponent
import com.example.blogposts.di.main.MainComponent
import dagger.Module

@Module(
    subcomponents = [
        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentModule