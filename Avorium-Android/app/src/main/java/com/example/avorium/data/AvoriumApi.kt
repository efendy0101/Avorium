package com.example.avorium.data

import okhttp3.MultipartBody
import retrofit2.http.*

interface AvoriumApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): UserResponse

    @PATCH("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UpdateProfileResponse

    @GET("ads")
    suspend fun getAds(
        @Query("type") type: String? = null,
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("city") city: String? = null,
        @Query("district") district: String? = null
    ): AdsResponse

    @GET("ads/{id}")
    suspend fun getAdById(@Path("id") id: String): AdDto

    @GET("ads/my/list")
    suspend fun getMyAds(): List<AdDto>

    @POST("ads")
    suspend fun createAd(@Body request: CreateAdRequest): CreateAdResponse

    @PUT("ads/my/{id}")
    suspend fun updateMyAd(@Path("id") id: String, @Body request: UpdateAdRequest): CreateAdResponse

    @Multipart
    @POST("upload")
    suspend fun uploadMedia(
        @Part images: List<MultipartBody.Part>,
        @Part video: MultipartBody.Part?
    ): UploadResponse

    @GET("categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("messages")
    suspend fun getMessages(): List<MessageDto>

    @GET("messages/conversation/{userId}")
    suspend fun getConversation(@Path("userId") userId: String): List<MessageDto>

    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): SendMessageResponse

    @DELETE("messages/{id}")
    suspend fun deleteMessage(@Path("id") id: String): retrofit2.Response<Unit>

    @GET("packages")
    suspend fun getPackages(): List<PackageDto>

    @POST("packages/verify")
    suspend fun verifyIap(@Body request: IapVerifyRequest): IapVerifyResponse

    @GET("notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): retrofit2.Response<Unit>

    @POST("reports")
    suspend fun createReport(@Body request: ReportRequest): retrofit2.Response<Unit>

    @GET("settings/maintenance")
    suspend fun getMaintenanceStatus(): MaintenanceResponse

    @GET("packages/my-orders")
    suspend fun getMyOrders(): List<IapOrderDto>

    @POST("verify/phone/send")
    suspend fun sendVerificationSms(): SmsSendResponse

    @POST("verify/phone/verify")
    suspend fun verifyPhone(@Body request: SmsVerifyRequest): UpdateProfileResponse

    @POST("verify/tc/verify")
    suspend fun verifyTc(@Body request: TcVerifyRequest): UpdateProfileResponse

    @GET("admin/pending-avatars")
    suspend fun getPendingAvatars(): List<UserDto>

    @PATCH("admin/users/{id}/avatar-status")
    suspend fun updateAvatarStatus(@Path("id") id: String, @Body status: Map<String, String>): UpdateProfileResponse
}
