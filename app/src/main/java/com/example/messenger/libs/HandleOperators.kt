package com.example.messenger.libs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

object HandleOperators {
    suspend fun <T> handleRequest(request: suspend () -> Response<T>): Response<T> {
        return withContext(Dispatchers.IO) {
            try {
                request.invoke()
            } catch (e: Exception) {
                Response.error(999, e.message?.toResponseBody() ?: "Network error".toResponseBody())
            }
        }
    }
}