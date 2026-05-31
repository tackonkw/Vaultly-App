package com.project.vaultly.feature.transaction.data

import com.google.gson.annotations.SerializedName
import com.project.vaultly.feature.transaction.presentation.Transaction

data class TransactionDto(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    @SerializedName("transaction_type")
    val transactionType: String,
    @SerializedName("transaction_at")
    val transactionAt: String?,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class TransactionRequest(
    val title: String,
    val amount: Double,
    val category: String,
    @SerializedName("transaction_type")
    val transactionType: String,
    @SerializedName("transaction_at")
    val transactionAt: String
)

fun TransactionDto.toTransaction(): Transaction {
    return Transaction(
        id = id.toString(),
        title = title,
        amount = amount,
        category = category,
        isExpense = transactionType == "EXPENSE",
        transactionAt = transactionAt.orEmpty()
    )
}
