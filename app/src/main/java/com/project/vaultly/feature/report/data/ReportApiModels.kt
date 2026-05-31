package com.project.vaultly.feature.report.data

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class SummaryReportDto(
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("end_date")
    val endDate: String?,
    @SerializedName("total_income")
    val totalIncome: Double,
    @SerializedName("total_expense")
    val totalExpense: Double,
    val balance: Double
)

data class DeficitReportDto(
    val month: String,
    @SerializedName("total_income")
    val totalIncome: Double,
    @SerializedName("total_expense")
    val totalExpense: Double,
    val balance: Double,
    @SerializedName("is_deficit")
    val isDeficit: Boolean
)

data class BudgetVsActualReportDto(
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("end_date")
    val endDate: String?,
    val items: List<BudgetActualItemDto>
)

data class BudgetActualItemDto(
    val id: Long,
    val category: String,
    @SerializedName("budget_amount")
    val budgetAmount: Double,
    @SerializedName("actual_expense")
    val actualExpense: Double,
    val remaining: Double,
    @SerializedName("is_over_budget")
    val isOverBudget: Boolean
)

data class ReportDateRange(
    val startDate: String?,
    val endDate: String?,
    val month: String?
) {
    companion object {
        fun fromPeriod(period: String): ReportDateRange {
            val calendar = Calendar.getInstance()
            return when (period) {
                "Bulan Ini" -> calendar.monthRange()
                "Bulan Lalu" -> {
                    calendar.add(Calendar.MONTH, -1)
                    calendar.monthRange()
                }
                "3 Bulan Terakhir" -> {
                    val end = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, -3)
                    ReportDateRange(calendar.apiDate(), end.apiDate(), end.apiMonth())
                }
                "Tahun Ini" -> {
                    val end = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    ReportDateRange(calendar.apiDate(), end.apiDate(), end.apiMonth())
                }
                else -> ReportDateRange(null, null, Calendar.getInstance().apiMonth())
            }
        }

        fun currentMonth(): ReportDateRange = fromPeriod("Bulan Ini")
    }
}

private fun Calendar.monthRange(): ReportDateRange {
    val start = clone() as Calendar
    start.set(Calendar.DAY_OF_MONTH, 1)

    val end = clone() as Calendar
    end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))

    return ReportDateRange(start.apiDate(), end.apiDate(), apiMonth())
}

private fun Calendar.apiDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(time)
}

private fun Calendar.apiMonth(): String {
    return SimpleDateFormat("yyyy-MM", Locale.US).format(time)
}
