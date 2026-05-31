package com.project.vaultly.feature.budgeting.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.vaultly.core.network.ApiResult
import com.project.vaultly.feature.budgeting.data.BudgetRepository
import com.project.vaultly.feature.transaction.presentation.Transaction
import kotlinx.coroutines.launch

data class Budget(
    val id: String,
    val category: String,
    val amount: Double,
    val spent: Double = 0.0
)

class BudgetViewModel : ViewModel() {
    private val _budgets = mutableStateListOf<Budget>()
    val budgets: SnapshotStateList<Budget> = _budgets

    var isLoading by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    fun loadBudgetsForUser(email: String) {
        loadBudgets()
    }

    fun loadBudgets() {
        isLoading = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = BudgetRepository.getBudgets()) {
                is ApiResult.Success -> {
                    _budgets.clear()
                    _budgets.addAll(result.data)
                }
                is ApiResult.Failure -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun recalculateSpent(transactions: List<Transaction>) {
        val categorySpent = mutableMapOf<String, Double>()

        transactions.filter { it.isExpense }.forEach { transaction ->
            val currentSpent = categorySpent[transaction.category] ?: 0.0
            categorySpent[transaction.category] = currentSpent + transaction.amount
        }

        for (i in _budgets.indices) {
            val budget = _budgets[i]
            val spent = categorySpent[budget.category] ?: 0.0
            _budgets[i] = budget.copy(spent = spent)
        }
    }

    fun addBudget(category: String, amount: Double) {
        isSaving = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = BudgetRepository.createBudget(category, amount)) {
                is ApiResult.Success -> loadBudgets()
                is ApiResult.Failure -> errorMessage = result.message
            }
            isSaving = false
        }
    }

    fun updateBudget(id: String, amount: Double) {
        val index = _budgets.indexOfFirst { it.id == id }
        if (index != -1) {
            val currentBudget = _budgets[index]
            isSaving = true
            errorMessage = ""
            viewModelScope.launch {
                when (val result = BudgetRepository.updateBudget(id, currentBudget.category, amount)) {
                    is ApiResult.Success -> loadBudgets()
                    is ApiResult.Failure -> errorMessage = result.message
                }
                isSaving = false
            }
        }
    }

    fun deleteBudget(id: String) {
        isSaving = true
        errorMessage = ""
        viewModelScope.launch {
            when (val result = BudgetRepository.deleteBudget(id)) {
                is ApiResult.Success -> loadBudgets()
                is ApiResult.Failure -> errorMessage = result.message
            }
            isSaving = false
        }
    }

    fun getTotalBudget(): Double = _budgets.sumOf { it.amount }
    fun getTotalSpent(): Double = _budgets.sumOf { it.spent }
    fun getRemainingBudget(): Double = getTotalBudget() - getTotalSpent()
}
