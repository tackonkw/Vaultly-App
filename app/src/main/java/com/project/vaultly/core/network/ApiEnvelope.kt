package com.project.vaultly.core.network

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: String? = null,
    val details: JsonElement? = null
)

data class ApiItems<T>(
    val items: List<T> = emptyList()
)

data class ApiErrorBody(
    val success: Boolean? = null,
    val error: String? = null,
    val message: String? = null,
    val details: JsonElement? = null
)

enum class ApiErrorType {
    NETWORK,
    TIMEOUT,
    UNAUTHORIZED,
    VALIDATION,
    CONFLICT,
    NOT_FOUND,
    SERVER,
    EMPTY_RESPONSE,
    UNKNOWN
}

sealed class ApiResult<out T> {
    data class Success<T>(
        val data: T,
        val message: String? = null
    ) : ApiResult<T>()

    data class Failure(
        val message: String,
        val type: ApiErrorType,
        val code: Int? = null,
        val apiError: String? = null
    ) : ApiResult<Nothing>()
}

data class DeleteResponse(
    @SerializedName("deleted")
    val deleted: Boolean = true
)
