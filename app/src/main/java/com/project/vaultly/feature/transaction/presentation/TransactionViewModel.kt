package com.project.vaultly.feature.transaction.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.vaultly.core.network.ApiErrorType
import com.project.vaultly.core.network.ApiResult
import com.project.vaultly.feature.transaction.data.TransactionRepository
import kotlinx.coroutines.launch

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val isExpense: Boolean,
    val transactionAt: String = ""
)

class TransactionViewModel : ViewModel() {
    private val _transactions = mutableStateListOf<Transaction>()
    val transactions: SnapshotStateList<Transaction> = _transactions

    var selectedTransaction by mutableStateOf<Transaction?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    fun loadTransactionsForUser(email: String) {
        loadTransactions()
    }

    fun loadTransactions(
        type: String? = null,
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        isLoading = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = TransactionRepository.getTransactions(type, category, startDate, endDate)) {
                is ApiResult.Success -> {
                    _transactions.clear()
                    _transactions.addAll(result.data)
                }
                is ApiResult.Failure -> handleFailure(result)
            }
            isLoading = false
        }
    }

    fun loadTransaction(transactionId: String) {
        isLoading = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = TransactionRepository.getTransaction(transactionId)) {
                is ApiResult.Success -> selectedTransaction = result.data
                is ApiResult.Failure -> handleFailure(result)
            }
            isLoading = false
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        category: String,
        isExpense: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        isSaving = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = TransactionRepository.createTransaction(title, amount, category, isExpense)) {
                is ApiResult.Success -> {
                    loadTransactions()
                    onSuccess()
                }
                is ApiResult.Failure -> handleFailure(result)
            }
            isSaving = false
        }
    }

    fun updateTransaction(
        transactionId: String,
        title: String,
        amount: Double,
        category: String,
        isExpense: Boolean,
        transactionAt: String,
        onSuccess: () -> Unit = {}
    ) {
        isSaving = true
        errorMessage = ""
        viewModelScope.launch {
            when (
                val result = TransactionRepository.updateTransaction(
                    id = transactionId,
                    title = title,
                    amount = amount,
                    category = category,
                    isExpense = isExpense,
                    transactionAt = transactionAt
                )
            ) {
                is ApiResult.Success -> {
                    loadTransactions()
                    loadTransaction(transactionId)
                    onSuccess()
                }
                is ApiResult.Failure -> handleFailure(result)
            }
            isSaving = false
        }
    }

    fun deleteTransaction(transactionId: String, onSuccess: () -> Unit = {}) {
        isSaving = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = TransactionRepository.deleteTransaction(transactionId)) {
                is ApiResult.Success -> {
                    _transactions.removeAll { it.id == transactionId }
                    selectedTransaction = null
                    onSuccess()
                }
                is ApiResult.Failure -> handleFailure(result)
            }
            isSaving = false
        }
    }

    fun getTotalIncome(): Double {
        return _transactions.filter { !it.isExpense }.sumOf { it.amount }
    }

    fun getTotalExpense(): Double {
        return _transactions.filter { it.isExpense }.sumOf { it.amount }
    }

    fun getBalance(): Double {
        return getTotalIncome() - getTotalExpense()
    }

    fun getTransactionById(id: String): Transaction? {
        return _transactions.find { it.id == id } ?: selectedTransaction?.takeIf { it.id == id }
    }

    fun clearError() {
        errorMessage = ""
    }

    private fun handleFailure(result: ApiResult.Failure) {
        errorMessage = if (result.type == ApiErrorType.UNAUTHORIZED) {
            "Sesi login tidak valid. Silakan logout lalu login kembali."
        } else {
            result.message
        }
    }
}
