package com.project.vaultly.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Routes : NavKey {

    @Serializable
    data object SplashRoute : Routes()

    @Serializable
    data object LoginRoute : Routes()

    @Serializable
    data object RegisterRoute : Routes()

    @Serializable
    data class HomeRoute(val email: String) : Routes()

    @Serializable
    data object ListTransactionRoute : Routes()

    @Serializable
    data class CreateTransactionRoute(
        val transactionId: String? = null
    ) : Routes()

    @Serializable
    data class DetailTransactionRoute(
        val transactionId: String,
        val isFromDeficit: Boolean = false
    ) : Routes()

    @Serializable
    data object BudgetPlanningRoute : Routes()

    @Serializable
    data class DeficitAlertRoute(
        val deficitAmount: Double
    ) : Routes()

    @Serializable
    data class AdaptiveReallocationRoute(
        val deficitAmount: Double,
        val recommendations: String? = null
    ) : Routes()

    @Serializable
    data object ReportRoute : Routes()
}
