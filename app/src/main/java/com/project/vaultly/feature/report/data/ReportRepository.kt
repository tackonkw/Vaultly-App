package com.project.vaultly.feature.report.data

import com.project.vaultly.core.network.ApiClient
import com.project.vaultly.core.network.ApiResult
import com.project.vaultly.core.network.NetworkCall

object ReportRepository {
    suspend fun getSummary(range: ReportDateRange): ApiResult<SummaryReportDto> {
        return NetworkCall.execute {
            ApiClient.service.getSummaryReport(
                startDate = range.startDate,
                endDate = range.endDate
            )
        }
    }

    suspend fun getDeficit(month: String?): ApiResult<DeficitReportDto> {
        return NetworkCall.execute {
            ApiClient.service.getDeficitReport(month = month)
        }
    }

    suspend fun getBudgetVsActual(range: ReportDateRange): ApiResult<BudgetVsActualReportDto> {
        return NetworkCall.execute({
            BudgetVsActualReportDto(
                startDate = range.startDate,
                endDate = range.endDate,
                items = emptyList()
            )
        }) {
            ApiClient.service.getBudgetVsActualReport(
                startDate = range.startDate,
                endDate = range.endDate
            )
        }
    }
}
