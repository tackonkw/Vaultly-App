package com.project.vaultly.feature.budgeting.data

import com.google.gson.JsonElement
import com.project.vaultly.core.network.ApiClient
import com.project.vaultly.core.network.ApiItems
import com.project.vaultly.core.network.ApiResult
import com.project.vaultly.core.network.NetworkCall
import com.project.vaultly.feature.budgeting.presentation.Budget
import com.project.vaultly.feature.report.data.ReportDateRange
import com.project.vaultly.feature.report.data.ReportRepository

object BudgetRepository {
    suspend fun getBudgets(): ApiResult<List<Budget>> {
        val actualByCategory = when (val actual = ReportRepository.getBudgetVsActual(ReportDateRange.currentMonth())) {
            is ApiResult.Success -> actual.data.items.associate { it.category to it.actualExpense }
            is ApiResult.Failure -> emptyMap()
        }

        return when (val result = NetworkCall.execute({ ApiItems<BudgetDto>() }) {
            ApiClient.service.getBudgets()
        }) {
            is ApiResult.Success -> ApiResult.Success(
                data = result.data.items.map { it.toBudget(spent = actualByCategory[it.category] ?: 0.0) },
                message = result.message
            )
            is ApiResult.Failure -> result
        }
    }

    suspend fun createBudget(category: String, amount: Double): ApiResult<Unit> {
        return when (val result = NetworkCall.execute(emptyData = { null as JsonElement? }) {
            ApiClient.service.createBudget(BudgetRequest(category = category, amount = amount))
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.message)
            is ApiResult.Failure -> result
        }
    }

    suspend fun updateBudget(id: String, category: String, amount: Double): ApiResult<Unit> {
        return when (val result = NetworkCall.execute(emptyData = { null as JsonElement? }) {
            ApiClient.service.updateBudget(
                id = id,
                request = BudgetRequest(category = category, amount = amount)
            )
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.message)
            is ApiResult.Failure -> result
        }
    }

    suspend fun deleteBudget(id: String): ApiResult<Unit> {
        return when (val result = NetworkCall.execute(emptyData = { null as JsonElement? }) {
            ApiClient.service.deleteBudget(id)
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.message)
            is ApiResult.Failure -> result
        }
    }
}
