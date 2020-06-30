package com.example.blogposts.persistesnce

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.blogposts.models.AccountProperties

@Dao
interface AccountPropertiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAndReplace(accountProperties: AccountProperties): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(accountProperties: AccountProperties): Long

    @Query("SELECT * FROM account_properties WHERE pk=:pk")
    suspend fun searchByPk(pk: Int): AccountProperties

    @Query("SELECT * FROM account_properties WHERE email=:email")
    suspend fun searchByEmail(email: String): AccountProperties?

    @Query("UPDATE account_properties SET email= :email, username = :username WHERE pk = :pk")
    suspend fun updateAccountProperties(pk: Int, email: String, username: String)

}