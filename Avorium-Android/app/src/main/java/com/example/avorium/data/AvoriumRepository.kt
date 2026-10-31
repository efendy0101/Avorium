package com.example.avorium.data

import android.content.Context
import com.example.avorium.BuildConfig
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("avorium_session", Context.MODE_PRIVATE)
    private val gson = Gson()

    var token: String?
        get() = preferences.getString("token", null)
        set(value) {
            preferences.edit().putString("token", value).apply()
        }

    var user: UserDto?
        get() {
            val json = preferences.getString("user", null) ?: return null
            return runCatching { gson.fromJson(json, UserDto::class.java) }.getOrNull()
        }
        set(value) {
            preferences.edit().putString("user", value?.let(gson::toJson)).apply()
        }

    fun saveSession(token: String, user: UserDto) {
        preferences.edit()
            .putString("token", token)
            .putString("user", gson.toJson(user))
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }
}

class AvoriumRepository(private val api: AvoriumApi, private val sessionStore: SessionStore) {
    val cachedUser: UserDto?
        get() = sessionStore.user

    val hasToken: Boolean
        get() = !sessionStore.token.isNullOrBlank()

    suspend fun login(email: String, password: String): AuthResponse {
        val response = api.login(AuthRequest(email, password))
        sessionStore.saveSession(response.token, response.user)
        return response
    }

    suspend fun register(email: String, password: String, name: String, phone: String?): AuthResponse {
        val response = api.register(RegisterRequest(email, password, name, phone))
        sessionStore.saveSession(response.token, response.user)
        return response
    }

    suspend fun loadCurrentUser(): UserDto? {
        if (!hasToken) return null
        val user = api.me().user
        sessionStore.user = user
        return user
    }

    suspend fun updateProfile(name: String?, phone: String?, tcNo: String?, city: String? = null, district: String? = null, avatar: String? = null, avatarStatus: String? = null): UserDto {
        val response = api.updateProfile(UpdateProfileRequest(name = name, phone = phone, tcNo = tcNo, city = city, district = district, avatar = avatar, avatarStatus = avatarStatus))
        sessionStore.user = response.user
        return response.user
    }

    suspend fun getAds(type: String?, search: String?, categoryId: String? = null, city: String? = null, district: String? = null) =
        api.getAds(type, search, categoryId, city, district).ads

    suspend fun getAdById(id: String) = api.getAdById(id)

    suspend fun getMyAds() = api.getMyAds()

    suspend fun getCategories() = api.getCategories()

    suspend fun createAd(request: CreateAdRequest) = api.createAd(request)

    suspend fun updateMyAd(adId: String, request: UpdateAdRequest) = api.updateMyAd(adId, request)

    suspend fun uploadMedia(
        images: List<okhttp3.MultipartBody.Part>,
        video: okhttp3.MultipartBody.Part?
    ) = api.uploadMedia(images, video)

    suspend fun getMessages() = api.getMessages()

    suspend fun sendMessage(receiverId: String, adId: String, content: String): SendMessageResponse {
        return api.sendMessage(SendMessageRequest(receiverId, adId, content))
    }

    suspend fun getPackages() = api.getPackages()

    suspend fun verifyIap(packageId: String, purchaseToken: String, productId: String): IapVerifyResponse {
        return api.verifyIap(IapVerifyRequest(packageId, purchaseToken, productId))
    }

    suspend fun getConversation(userId: String) = api.getConversation(userId)

    suspend fun deleteMessage(id: String) = api.deleteMessage(id)

    suspend fun getNotifications() = api.getNotifications()

    suspend fun getUnreadCount() = api.getUnreadCount().count

    suspend fun markNotificationRead(id: String) = api.markNotificationRead(id)

    suspend fun createReport(adId: String, reason: String, detail: String? = null) =
        api.createReport(ReportRequest(adId, reason, detail))

    fun logout() {
        sessionStore.clear()
    }
    suspend fun getMaintenanceStatus() = api.getMaintenanceStatus()
    suspend fun getMyOrders() = api.getMyOrders()

    suspend fun sendVerificationSms() = api.sendVerificationSms()

    suspend fun verifyPhone(code: String): UserDto {
        val response = api.verifyPhone(SmsVerifyRequest(code))
        sessionStore.user = response.user
        return response.user
    }

    suspend fun verifyTc(tckn: String, firstName: String, lastName: String, birthYear: Int): UserDto {
        val response = api.verifyTc(TcVerifyRequest(tckn, firstName, lastName, birthYear))
        sessionStore.user = response.user
        return response.user
    }

    suspend fun getPendingAvatars() = api.getPendingAvatars()

    suspend fun updateAvatarStatus(userId: String, status: String): UserDto {
        val response = api.updateAvatarStatus(userId, mapOf("avatarStatus" to status))
        return response.user
    }
}

object AvoriumRepositoryFactory {
    fun create(context: Context): AvoriumRepository {
        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        val sessionStore = SessionStore(context)
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = sessionStore.token
                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logger)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return AvoriumRepository(retrofit.create(AvoriumApi::class.java), sessionStore)
    }
}
