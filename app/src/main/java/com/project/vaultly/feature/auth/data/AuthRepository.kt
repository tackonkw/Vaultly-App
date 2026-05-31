package com.project.vaultly.feature.auth.data

import com.project.vaultly.core.network.ApiClient
import com.project.vaultly.core.network.ApiResult
import com.project.vaultly.core.network.NetworkCall

object AuthRepository {
    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): ApiResult<UserDto> {
        return NetworkCall.execute {
            ApiClient.service.register(
                RegisterRequest(
                    fullName = fullName,
                    email = email,
                    password = password
                )
            )
        }.also { result ->
            if (result is ApiResult.Success) {
                UserRepository.saveUserProfile(
                    email = result.data.email,
                    name = result.data.fullName.orEmpty()
                )
            }
        }
    }

    suspend fun login(email: String, password: String): ApiResult<LoginResponse> {
        return NetworkCall.execute {
            ApiClient.service.login(LoginRequest(email = email, password = password))
        }.also { result ->
            if (result is ApiResult.Success) {
                UserRepository.saveUserSession(
                    email = email,
                    accessToken = result.data.accessToken
                )
                refreshCurrentUser()
            }
        }
    }

    suspend fun refreshCurrentUser(): ApiResult<UserDto> {
        return NetworkCall.execute {
            ApiClient.service.me()
        }.also { result ->
            if (result is ApiResult.Success) {
                UserRepository.saveUserProfile(
                    email = result.data.email,
                    name = result.data.fullName.orEmpty()
                )
                UserRepository.saveCurrentEmail(result.data.email)
            }
        }
    }
}
