package com.project.vaultly.feature.transaction.data

import com.google.gson.JsonElement
import com.project.vaultly.core.network.ApiItems
import com.project.vaultly.core.network.ApiClient
import com.project.vaultly.core.network.ApiResult
import com.project.vaultly.core.network.NetworkCall
import com.project.vaultly.feature.transaction.presentation.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TransactionRepository {
    suspend fun getTransactions(
        type: String? = null,
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): ApiResult<List<Transaction>> {
        return when (val result = NetworkCall.execute({
            ApiItems<TransactionDto>()
        }) {
            ApiClient.service.getTransactions(
                type = type,
                category = category,
                startDate = startDate,
                endDate = endDate
            )
        }) {
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toTransaction() }, result.message)
            is ApiResult.Failure -> result
        }
    }

    suspend fun getTransaction(id: String): ApiResult<Transaction> {
        return when (val result = NetworkCall.execute {
            ApiClient.service.getTransaction(id)
        }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toTransaction(), result.message)
            is ApiResult.Failure -> result
        }
    }

    suspend fun createTransaction(
        title: String,
        amount: Double,
        category: String,
        isExpense: Boolean,
        transactionAt: String = nowForApi()
    ): ApiResult<Unit> {
        return when (val result = NetworkCall.execute(emptyData = { null as JsonElement? }) {
            ApiClient.service.createTransaction(
                TransactionRequest(
                    title = title,
                    amount = amount,
                    category = category,
                    transactionType = if (isExpense) "EXPENSE" else "INCOME",
                    transactionAt = transactionAt
                )
            )
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.message)
            is ApiResult.Failure -> result
        }
    }

    suspend fun updateTransaction(
        id: String,
        title: String,
        amount: Double,
        category: String,
        isExpense: Boolean,
        transactionAt: String
    ): ApiResult<Unit> {
        return when (val result = NetworkCall.execute(emptyData = { null as JsonElement? }) {
            ApiClient.service.updateTransaction(
                id = id,
                request = TransactionRequest(
                    title = title,
                    amount = amount,
                    category = category,
                    transactionType = if (isExpense) "EXPENSE" else "INCOME",
                    transactionAt = transactionAt.ifBlank { nowForApi() }
                )
            )
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.message)
            is ApiResult.Failure -> result
        }
    }

    suspend fun deleteTransaction(id: String): ApiResult<Unit> {
        return when (val result = NetworkCall.execute(emptyData = { null as JsonElement? }) {
            ApiClient.service.deleteTransaction(id)
        }) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.message)
            is ApiResult.Failure -> result
        }
    }

    fun nowForApi(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())
    }
}
