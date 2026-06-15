package com.snokonoko.app.repository

import android.content.Context
import com.snokonoko.app.data.AppDatabase
import com.snokonoko.app.data.User

class UserRepository(context: Context) {

    private val userDao = AppDatabase.getDatabase(context).userDao()

    suspend fun registerUser(
        firstName: String,
        surname: String,
        email: String,
        password: String
    ): Long {
        if (userDao.emailExists(email) > 0) return -1L
        return userDao.insertUser(
            User(firstName = firstName, surname = surname, email = email, password = password)
        )
    }

    suspend fun loginUser(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email) ?: return null
        return if (user.password == password) user else null
    }
}
