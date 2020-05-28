package com.example.blogposts.session

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blogposts.models.AuthToken
import com.example.blogposts.persistesnce.AuthTokenDao
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {

    private val TAG: String = "AppDebug"

    private val _cachedToken = MutableLiveData<AuthToken?>()

    val cachedToken: LiveData<AuthToken?>
        get() = _cachedToken

    /**
     * it is the same with above method
     * fun getCachedToken():LiveData<AuthToken>{
    return _cachedToken
    }*/

    fun login(newValue: AuthToken) {
        setValue(newValue)
    }

    fun logOut() {
        Log.d(TAG, "logout...")

        GlobalScope.launch(IO) {
            var errorMessage: String? = null
            try {
                cachedToken.value!!.account_pk?.let {
                    authTokenDao.nullifyToken(it)
                }
            } catch (e: CancellationException) {
                Log.e(TAG, "logout: ${e.message}")
                errorMessage = e.message
            } catch (e: Exception) {
                Log.e(TAG, "logout: ${e.message}")
                errorMessage = errorMessage + "\n" + e.message
            } finally {
                errorMessage?.let {
                    Log.e(TAG, "logout: $errorMessage")
                }
                Log.d(TAG, "logout: finally...")
                setValue(null)
            }
        }
    }

    fun setValue(newValue: AuthToken?) {
        GlobalScope.launch(Main) {
            if (_cachedToken.value != newValue
                && _cachedToken.value != null) {
                _cachedToken.value = newValue
            }
        }
    }

    fun isConnectedToInternet(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
          return cm.activeNetworkInfo.isConnected
        } catch (e: Exception){
            Log.e(TAG,"isConnectedToInternet: ${e.message}")
        }
        return false
    }

}