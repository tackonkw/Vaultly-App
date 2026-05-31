package com.project.vaultly.feature.report.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.vaultly.core.network.ApiResult
import com.project.vaultly.feature.report.data.BudgetActualItemDto
import com.project.vaultly.feature.report.data.DeficitReportDto
import com.project.vaultly.feature.report.data.ReportDateRange
import com.project.vaultly.feature.report.data.ReportRepository
import com.project.vaultly.feature.report.data.SummaryReportDto
import com.project.vaultly.feature.transaction.data.TransactionRepository
import com.project.vaultly.feature.transaction.presentation.Transaction
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")
        private set

    var summary by mutableStateOf<SummaryReportDto?>(null)
        private set

    var deficit by mutableStateOf<DeficitReportDto?>(null)
        private set

    private val _budgetActualItems = mutableStateListOf<BudgetActualItemDto>()
    val budgetActualItems: List<BudgetActualItemDto> = _budgetActualItems

    private val _transactions = mutableStateListOf<Transaction>()
    val transactions: List<Transaction> = _transactions

    fun load(period: String) {
        val range = ReportDateRange.fromPeriod(period)
        isLoading = true
        errorMessage = ""

        viewModelScope.launch {
            val summaryResult = ReportRepository.getSummary(range)
            val deficitResult = ReportRepository.getDeficit(range.month)
            val budgetResult = ReportRepository.getBudgetVsActual(range)
            val transactionResult = TransactionRepository.getTransactions(
                startDate = range.startDate,
                endDate = range.endDate
            )

            if (summaryResult is ApiResult.Success) {
                summary = summaryResult.data
            }

            if (deficitResult is ApiResult.Success) {
                deficit = deficitResult.data
            }

            if (budgetResult is ApiResult.Success) {
                _budgetActualItems.clear()
                _budgetActualItems.addAll(budgetResult.data.items)
            }

            if (transactionResult is ApiResult.Success) {
                _transactions.clear()
                _transactions.addAll(transactionResult.data)
            }

            errorMessage = listOf(summaryResult, deficitResult, budgetResult, transactionResult)
                .filterIsInstance<ApiResult.Failure>()
                .firstOrNull()
                ?.message
                .orEmpty()

            isLoading = false
        }
    }
}
