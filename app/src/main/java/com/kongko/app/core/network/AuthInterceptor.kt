package com.kongko.app.core.network

import com.kongko.app.core.datastore.SessionDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth header for auth endpoints
        val path = originalRequest.url.encodedPath
        if (path.contains("auth/verify-token") || path.contains("auth/refresh-token")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { sessionDataStore.getAccessToken() }
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // Auto token refresh on 401
        if (response.code == 401) {
            response.close()
            synchronized(this) {
                val currentToken = runBlocking { sessionDataStore.getAccessToken() }
                if (currentToken == token) {
                    val refreshToken = runBlocking { sessionDataStore.getRefreshToken() }
                    if (!refreshToken.isNullOrBlank()) {
                        // Attempt token refresh synchronously or clear session if failed
                    }
                }
            }
            // Retry with updated token
            val newToken = runBlocking { sessionDataStore.getAccessToken() }
            if (!newToken.isNullOrBlank()) {
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retryRequest)
            }
        }

        return response
    }
}
