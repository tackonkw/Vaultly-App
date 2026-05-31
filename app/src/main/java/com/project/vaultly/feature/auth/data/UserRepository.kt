package com.project.vaultly.feature.auth.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.vaultly.feature.transaction.presentation.Transaction

data class User(
    val name: String,
    val email: String,
    val password: String
)

object UserRepository {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("vaultly_users", Context.MODE_PRIVATE)
    }

    // ============ USER MANAGEMENT ============

    fun register(user: User): Boolean {
        val existingPassword = prefs.getString("user_${user.email}_password", "")
        if (existingPassword?.isNotEmpty() == true) {
            return false
        }

        prefs.edit().apply {
            putString("user_${user.email}_name", user.name)
            putString("user_${user.email}_password", user.password)
            putStringSet("all_emails", getRegisteredEmails().toMutableSet().apply {
                add(user.email)
            })
            putString("user_${user.email}_transactions", "[]")
            apply()
        }
        return true
    }

    fun login(email: String, password: String): Boolean {
        val savedPassword = prefs.getString("user_${email}_password", "")
        return savedPassword == password
    }

    fun getUserName(email: String): String? {
        return prefs.getString("user_${email}_name", null)
    }

    fun getRegisteredEmails(): Set<String> {
        return prefs.getStringSet("all_emails", emptySet()) ?: emptySet()
    }

    fun saveUserSession(email: String, accessToken: String? = getAccessToken()) {
        prefs.edit().apply {
            putString("current_user_email", email)
            if (!accessToken.isNullOrBlank()) {
                putString("access_token", accessToken)
            }
            apply()
        }
    }

    fun saveCurrentEmail(email: String) {
        prefs.edit().putString("current_user_email", email).apply()
    }

    fun saveUserProfile(email: String, name: String) {
        if (name.isBlank()) return
        prefs.edit().putString("user_${email}_name", name).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }

    fun getCurrentUserEmail(): String? {
        return prefs.getString("current_user_email", null)
    }

    fun logout() {
        prefs.edit()
            .remove("current_user_email")
            .remove("access_token")
            .apply()
    }

    // ============ TRANSACTIONS PER USER ============

    fun saveTransactions(email: String, transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString("user_${email}_transactions", json).apply()
    }

    fun getTransactions(email: String): List<Transaction> {
        val json = prefs.getString("user_${email}_transactions", "[]") ?: "[]"
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    fun addTransaction(email: String, transaction: Transaction) {
        val currentTransactions = getTransactions(email).toMutableList()
        currentTransactions.add(0, transaction)
        saveTransactions(email, currentTransactions)
    }

    fun updateTransaction(email: String, updatedTransaction: Transaction) {
        val currentTransactions = getTransactions(email).toMutableList()
        val transactionIndex = currentTransactions.indexOfFirst { it.id == updatedTransaction.id }
        if (transactionIndex != -1) {
            currentTransactions[transactionIndex] = updatedTransaction
            saveTransactions(email, currentTransactions)
        }
    }

    fun deleteTransaction(email: String, transactionId: String) {
        val currentTransactions = getTransactions(email).toMutableList()
        currentTransactions.removeAll { it.id == transactionId }
        saveTransactions(email, currentTransactions)
    }

    // ============ BUDGETS PER USER ============

    fun saveBudgets(email: String, budgetsJson: String) {
        prefs.edit().putString("user_${email}_budgets", budgetsJson).apply()
    }

    fun getBudgets(email: String): String {
        return prefs.getString("user_${email}_budgets", "[]") ?: "[]"
    }
}
