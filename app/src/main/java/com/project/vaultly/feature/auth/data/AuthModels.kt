package com.project.vaultly.feature.auth.data

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("full_name")
    val fullName: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Long
)

data class UserDto(
    val id: Long,
    @SerializedName("full_name")
    val fullName: String?,
    val email: String,
    @SerializedName("created_at")
    val createdAt: String? = null
)
