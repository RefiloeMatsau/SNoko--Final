package com.snokonoko.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.snokonoko.app.MainActivity
import com.snokonoko.app.databinding.ActivitySignupBinding
import com.snokonoko.app.viewmodel.SignupResult
import com.snokonoko.app.viewmodel.UserViewModel

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val surname = binding.etSurname.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            when {
                firstName.isEmpty() -> showError("Please enter your first name.")
                surname.isEmpty() -> showError("Please enter your surname.")
                email.isEmpty() -> showError("Please enter your email.")
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    showError("Please enter a valid email address.")
                password.length < 6 -> showError("Password must be at least 6 characters.")
                password != confirm -> showError("Passwords do not match.")
                else -> {
                    hideError()
                    viewModel.signup(firstName, surname, email, password)
                }
            }
        }

        binding.tvGoLogin.setOnClickListener { finish() }

        viewModel.signupResult.observe(this) { result ->
            when (result) {
                is SignupResult.Success -> {
                    android.widget.Toast.makeText(this, "Account created! Please log in.", android.widget.Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SignupResult.EmailTaken ->
                    showError("An account with this email already exists.")
            }
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
}
