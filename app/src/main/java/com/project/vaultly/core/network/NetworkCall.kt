package com.project.vaultly.core.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object NetworkCall {
    private val gson = Gson()

    suspend fun <T> execute(
        emptyData: (() -> T)? = null,
        request: suspend () -> Response<ApiEnvelope<T>>
    ): ApiResult<T> {
        return try {
            val response = request()
            if (response.isSuccessful) {
                val body = response.body()
                when {
                    body == null -> emptyData?.let { ApiResult.Success(it()) }
                        ?: ApiResult.Failure(
                            message = "Server tidak mengirim response yang bisa dibaca.",
                            type = ApiErrorType.EMPTY_RESPONSE,
                            code = response.code()
                        )

                    body.success && body.data != null -> ApiResult.Success(body.data, body.message)
                    body.success && emptyData != null -> ApiResult.Success(emptyData(), body.message)
                    else -> ApiResult.Failure(
                        message = body.message ?: "Request gagal diproses server.",
                        type = mapApiError(body.error, response.code()),
                        code = response.code(),
                        apiError = body.error
                    )
                }
            } else {
                parseHttpError(response)
            }
        } catch (_: UnknownHostException) {
            networkFailure()
        } catch (_: SocketTimeoutException) {
            ApiResult.Failure(
                message = "Koneksi ke server terlalu lama. Periksa koneksi internet atau status API.",
                type = ApiErrorType.TIMEOUT
            )
        } catch (e: JsonSyntaxException) {
            ApiResult.Failure(
                message = "Format response API tidak sesuai. Operasi mungkin berhasil, silakan refresh data.",
                type = ApiErrorType.UNKNOWN,
                apiError = e.message
            )
        } catch (e: MalformedJsonException) {
            ApiResult.Failure(
                message = "Response dari API rusak/tidak lengkap. Operasi mungkin berhasil, silakan refresh data.",
                type = ApiErrorType.UNKNOWN,
                apiError = e.message
            )
        } catch (_: IOException) {
            networkFailure()
        } catch (_: Exception) {
            ApiResult.Failure(
                message = "Terjadi kesalahan tak terduga saat menghubungi API.",
                type = ApiErrorType.UNKNOWN
            )
        }
    }

    private fun networkFailure(): ApiResult.Failure {
        return ApiResult.Failure(
            message = "Tidak bisa terhubung ke API. Pastikan server berjalan dan base URL benar.",
            type = ApiErrorType.NETWORK
        )
    }

    private fun <T> parseHttpError(response: Response<ApiEnvelope<T>>): ApiResult.Failure {
        val errorBody = response.errorBody()?.string()
        val apiError = runCatching {
            gson.fromJson(errorBody, ApiErrorBody::class.java)
        }.getOrNull()

        return ApiResult.Failure(
            message = apiError?.message ?: defaultHttpMessage(response.code()),
            type = mapApiError(apiError?.error, response.code()),
            code = response.code(),
            apiError = apiError?.error
        )
    }

    private fun defaultHttpMessage(code: Int): String {
        return when (code) {
            400 -> "Request tidak valid. Cek kembali data yang diisi."
            401 -> "Sesi login tidak valid atau sudah kedaluwarsa. Silakan login ulang."
            403 -> "Akses ditolak oleh server."
            404 -> "Data atau endpoint tidak ditemukan."
            409 -> "Data konflik dengan data yang sudah ada."
            in 500..599 -> "Server sedang bermasalah. Coba lagi beberapa saat."
            else -> "Request gagal dengan kode HTTP $code."
        }
    }

    private fun mapApiError(error: String?, code: Int): ApiErrorType {
        return when {
            code == 401 -> ApiErrorType.UNAUTHORIZED
            code == 404 -> ApiErrorType.NOT_FOUND
            code == 409 -> ApiErrorType.CONFLICT
            code in 500..599 -> ApiErrorType.SERVER
            error == "VALIDATION_ERROR" || code == 422 || code == 400 -> ApiErrorType.VALIDATION
            else -> ApiErrorType.UNKNOWN
        }
    }
}
