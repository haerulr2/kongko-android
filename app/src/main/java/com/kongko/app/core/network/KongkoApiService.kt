package com.kongko.app.core.network

import com.kongko.app.core.network.dto.AgoraTokenResponse
import com.kongko.app.core.network.dto.ApiResponse
import com.kongko.app.core.network.dto.AuthResponse
import com.kongko.app.core.network.dto.CallDto
import com.kongko.app.core.network.dto.ChatDto
import com.kongko.app.core.network.dto.CreateChatRequest
import com.kongko.app.core.network.dto.MessageDto
import com.kongko.app.core.network.dto.RefreshTokenRequest
import com.kongko.app.core.network.dto.RegisterFcmTokenRequest
import com.kongko.app.core.network.dto.SyncContactsRequest
import com.kongko.app.core.network.dto.UpdateProfileRequest
import com.kongko.app.core.network.dto.UserDto
import com.kongko.app.core.network.dto.VerifyTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface KongkoApiService {

    // Auth
    @POST("auth/verify-token")
    suspend fun verifyToken(
        @Body request: VerifyTokenRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<AuthResponse>>

    // User & Profile
    @GET("users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    @PUT("users/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserDto>>

    @POST("users/sync-contacts")
    suspend fun syncContacts(
        @Body request: SyncContactsRequest
    ): Response<ApiResponse<List<UserDto>>>

    @POST("users/fcm-token")
    suspend fun registerFcmToken(
        @Body request: RegisterFcmTokenRequest
    ): Response<ApiResponse<Unit>>

    // Chats
    @GET("chats")
    suspend fun getChats(): Response<ApiResponse<List<ChatDto>>>

    @POST("chats")
    suspend fun createChat(
        @Body request: CreateChatRequest
    ): Response<ApiResponse<ChatDto>>

    @GET("chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<ApiResponse<List<MessageDto>>>

    // Calls
    @GET("calls")
    suspend fun getCalls(
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<List<CallDto>>>

    @POST("calls/token")
    suspend fun getAgoraToken(
        @Body request: Map<String, String>
    ): Response<ApiResponse<AgoraTokenResponse>>
}
