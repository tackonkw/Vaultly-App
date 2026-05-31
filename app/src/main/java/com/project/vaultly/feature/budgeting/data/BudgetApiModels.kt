package com.project.vaultly.feature.budgeting.data

import com.google.gson.annotations.SerializedName
import com.project.vaultly.feature.budgeting.presentation.Budget

data class BudgetDto(
    val id: Long,
    val category: String,
    val amount: Double,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class BudgetRequest(
    val category: String,
    val amount: Double
)

fun BudgetDto.toBudget(spent: Double = 0.0): Budget {
    return Budget(
        id = id.toString(),
        category = category,
        amount = amount,
        spent = spent
    )
}
