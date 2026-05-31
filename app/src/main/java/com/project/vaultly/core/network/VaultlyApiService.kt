package com.project.vaultly.core.network

import com.google.gson.JsonElement
import com.project.vaultly.core.network.ApiItems
import com.project.vaultly.feature.auth.data.LoginRequest
import com.project.vaultly.feature.auth.data.LoginResponse
import com.project.vaultly.feature.auth.data.RegisterRequest
import com.project.vaultly.feature.auth.data.UserDto
import com.project.vaultly.feature.budgeting.data.BudgetDto
import com.project.vaultly.feature.budgeting.data.BudgetRequest
import com.project.vaultly.feature.report.data.BudgetVsActualReportDto
import com.project.vaultly.feature.report.data.DeficitReportDto
import com.project.vaultly.feature.report.data.SummaryReportDto
import com.project.vaultly.feature.transaction.data.TransactionDto
import com.project.vaultly.feature.transaction.data.TransactionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VaultlyApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiEnvelope<UserDto>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<LoginResponse>>

    @GET("auth/me")
    suspend fun me(): Response<ApiEnvelope<UserDto>>

    @GET("transactions")
    suspend fun getTransactions(
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ApiEnvelope<ApiItems<TransactionDto>>>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") id: String): Response<ApiEnvelope<TransactionDto>>

    @POST("transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): Response<ApiEnvelope<JsonElement?>>

    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body request: TransactionRequest
    ): Response<ApiEnvelope<JsonElement?>>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<ApiEnvelope<JsonElement?>>

    @GET("budgets")
    suspend fun getBudgets(): Response<ApiEnvelope<ApiItems<BudgetDto>>>

    @POST("budgets")
    suspend fun createBudget(@Body request: BudgetRequest): Response<ApiEnvelope<JsonElement?>>

    @PUT("budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: String,
        @Body request: BudgetRequest
    ): Response<ApiEnvelope<JsonElement?>>

    @DELETE("budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String): Response<ApiEnvelope<JsonElement?>>

    @GET("reports/summary")
    suspend fun getSummaryReport(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ApiEnvelope<SummaryReportDto>>

    @GET("reports/deficit")
    suspend fun getDeficitReport(@Query("month") month: String? = null): Response<ApiEnvelope<DeficitReportDto>>

    @GET("reports/budget-vs-actual")
    suspend fun getBudgetVsActualReport(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): Response<ApiEnvelope<BudgetVsActualReportDto>>
}
