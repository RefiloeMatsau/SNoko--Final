package com.snokonoko.app.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.snokonoko.app.data.User
import com.snokonoko.app.databinding.ActivityAdminBinding
import com.snokonoko.app.repository.FinanceRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var repo: FinanceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = FinanceRepository(this)

        binding.btnAdminLogout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        binding.fabAddUser.setOnClickListener { showAddUserDialog() }

        loadUsers()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val users = repo.adminGetAllUsers()
            renderUserList(users)
        }
    }

    private fun renderUserList(users: List<User>) {
        val container = binding.userListContainer
        container.removeAllViews()

        if (users.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "No registered users"
                setTextColor(Color.parseColor("#474747"))
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 40, 0, 40)
            })
            return
        }

        users.forEachIndexed { index, user ->
            container.addView(buildUserCard(user))
            if (index < users.size - 1) {
                container.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).also { it.setMargins(0, 4, 0, 4) }
                    setBackgroundColor(Color.parseColor("#1A1A1A"))
                })
            }
        }
    }

    private fun buildUserCard(user: User): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0F0F"))
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dp(8) }
        }

        // Name + email row
        card.addView(TextView(this).apply {
            text = "${user.firstName} ${user.surname}"
            setTextColor(Color.WHITE)
            textSize = 16f
        })
        card.addView(TextView(this).apply {
            text = user.email
            setTextColor(Color.parseColor("#8C8C8C"))
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = dp(12) }
        })

        // Action buttons row
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        btnRow.addView(actionButton("View Data") { showUserTransactions(user) })
        btnRow.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(8), 1)
        })
        btnRow.addView(actionButton("Edit") { showEditUserDialog(user) })
        btnRow.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(8), 1)
        })
        btnRow.addView(actionButton("Delete", danger = true) { confirmDeleteUser(user) })

        card.addView(btnRow)
        return card
    }

    private fun actionButton(label: String, danger: Boolean = false, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            setTextColor(if (danger) Color.parseColor("#FF453A") else Color.parseColor("#F49AC2"))
            setBackgroundColor(Color.parseColor("#1A1A1A"))
            stateListAnimator = null
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(36)
            )
            setPadding(dp(12), 0, dp(12), 0)
            setOnClickListener { onClick() }
        }
    }

    private fun showUserTransactions(user: User) {
        lifecycleScope.launch {
            val txList = repo.adminGetUserTransactions(user.id)
            val nf = NumberFormat.getInstance(Locale("en", "ZA")).apply {
                minimumFractionDigits = 2; maximumFractionDigits = 2
            }
            val summary = if (txList.isEmpty()) {
                "No transactions"
            } else {
                val income  = txList.filter { it.type == "income"  }.sumOf { it.amount }
                val expense = txList.filter { it.type == "expense" }.sumOf { it.amount }
                val net     = income - expense
                val header  = "Income: R${nf.format(income)}\nExpenses: R${nf.format(abs(expense))}\nNet: R${nf.format(abs(net))}\n\n--- Recent (${txList.size} total) ---\n"
                val rows = txList.take(20).joinToString("\n") { tx ->
                    val sign = if (tx.type == "income") "+" else "-"
                    "$sign R${nf.format(tx.amount)}  ${tx.category}  ${tx.date}"
                }
                header + rows
            }

            AlertDialog.Builder(this@AdminActivity)
                .setTitle("${user.firstName}'s Data")
                .setMessage(summary)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showAddUserDialog() {
        val layout = buildUserForm()
        AlertDialog.Builder(this)
            .setTitle("Add New User")
            .setView(layout.root)
            .setPositiveButton("Add") { _, _ ->
                val firstName = layout.etFirstName.text.toString().trim()
                val surname   = layout.etSurname.text.toString().trim()
                val email     = layout.etEmail.text.toString().trim()
                val password  = layout.etPassword.text.toString()
                if (firstName.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    val result = repo.adminInsertUser(User(firstName = firstName, surname = surname, email = email, password = password))
                    if (result > 0) {
                        Toast.makeText(this@AdminActivity, "User added", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    } else {
                        Toast.makeText(this@AdminActivity, "Email already in use", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditUserDialog(user: User) {
        val layout = buildUserForm(user)
        AlertDialog.Builder(this)
            .setTitle("Edit User")
            .setView(layout.root)
            .setPositiveButton("Save") { _, _ ->
                val firstName = layout.etFirstName.text.toString().trim()
                val surname   = layout.etSurname.text.toString().trim()
                val email     = layout.etEmail.text.toString().trim()
                val password  = layout.etPassword.text.toString()
                if (firstName.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    repo.adminUpdateUser(user.id, firstName, surname, email, password)
                    Toast.makeText(this@AdminActivity, "User updated", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Delete ${user.firstName}?")
            .setMessage("This will permanently delete the user and ALL their transaction data.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repo.adminDeleteUser(user)
                    Toast.makeText(this@AdminActivity, "User deleted", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private data class UserForm(
        val root: LinearLayout,
        val etFirstName: EditText,
        val etSurname: EditText,
        val etEmail: EditText,
        val etPassword: EditText
    )

    private fun buildUserForm(existing: User? = null): UserForm {
        val pad = dp(50)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, dp(20), pad, dp(10))
        }
        fun field(hint: String, value: String = "", inputType: Int = InputType.TYPE_CLASS_TEXT): EditText {
            return EditText(this).apply {
                this.hint = hint
                setText(value)
                this.inputType = inputType
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dp(8) }
            }
        }
        val etFirst = field("First Name", existing?.firstName ?: "")
        val etSur   = field("Surname", existing?.surname ?: "")
        val etEmail = field("Email", existing?.email ?: "", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val etPass  = field("Password", existing?.password ?: "", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        root.addView(etFirst); root.addView(etSur); root.addView(etEmail); root.addView(etPass)
        return UserForm(root, etFirst, etSur, etEmail, etPass)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
