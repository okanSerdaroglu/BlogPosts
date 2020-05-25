package com.example.blogposts.persistesnce

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.models.AuthToken

@Database(entities = [AuthToken::class, AccountProperties::class], version = 1)
abstract class AppDatabase : RoomDatabase(){

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    companion object {
        const val DATABASE_NAME = "app_db"
    }

}