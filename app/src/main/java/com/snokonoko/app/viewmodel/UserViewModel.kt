package com.snokonoko.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.snokonoko.app.data.User
import com.snokonoko.app.repository.UserRepository
import kotlinx.coroutines.launch

sealed class SignupResult {
    object Success : SignupResult()
    object EmailTaken : SignupResult()
}

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    object Failed : LoginResult()
}

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    val signupResult = MutableLiveData<SignupResult>()
    val loginResult = MutableLiveData<LoginResult>()

    fun signup(firstName: String, surname: String, email: String, password: String) {
        viewModelScope.launch {
            val id = repository.registerUser(firstName, surname, email, password)
            if (id > 0) signupResult.postValue(SignupResult.Success)
            else signupResult.postValue(SignupResult.EmailTaken)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = repository.loginUser(email, password)
            if (user != null) loginResult.postValue(LoginResult.Success(user))
            else loginResult.postValue(LoginResult.Failed)
        }
    }
}
