package com.snokonoko.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.snokonoko.app.MainActivity
import com.snokonoko.app.databinding.ActivityLoginBinding
import com.snokonoko.app.repository.FinanceRepository
import com.snokonoko.app.viewmodel.LoginResult
import com.snokonoko.app.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: UserViewModel by viewModels()

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request camera permission on first launch
        checkAndRequestCameraPermission()

        // Check for existing session
        val prefs = getSharedPreferences("snokonoko_prefs", MODE_PRIVATE)
        val savedUserId = prefs.getInt("user_id", 0)
        val savedName = prefs.getString("user_name", null)
        val savedEmail = prefs.getString("user_email", null)
        if (savedUserId != 0 && savedName != null) {
            goToMain(savedName, savedEmail ?: "")
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            when {
                email.isEmpty() -> showError("Please enter your email.")
                password.isEmpty() -> showError("Please enter your password.")
                email == "admin" && password == "admin" -> {
                    startActivity(Intent(this, AdminActivity::class.java))
                    finish()
                }
                else -> {
                    hideError()
                    viewModel.login(email, password)
                }
            }
        }

        binding.tvGoSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Success -> {
                    val name = "${result.user.firstName} ${result.user.surname}"
                    val email = result.user.email
                    val userId = result.user.id
                    // Save session
                    prefs.edit()
                        .putInt("user_id", userId)
                        .putString("user_name", name)
                        .putString("user_email", email)
                        .apply()
                    // Seed default categories for this user
                    lifecycleScope.launch {
                        FinanceRepository(this@LoginActivity).seedDefaultCategories()
                    }
                    goToMain(name, email)
                }
                is LoginResult.Failed -> showError("Incorrect email or password.")
            }
        }
    }

    private fun goToMain(name: String, email: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_NAME", name)
            putExtra("USER_EMAIL", email)
        }
        startActivity(intent)
        finish()
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }

    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }
}
