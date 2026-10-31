package com.example.avorium.data

data class AuthRequest(val email: String, val password: String)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String? = null
)

data class AuthResponse(
    val message: String,
    val token: String,
    val user: UserDto
)

data class UserResponse(val user: UserDto)

data class ErrorResponse(val message: String? = null)

data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val phone: String? = null,
    val avatar: String? = null,
    val avatarStatus: String? = "APPROVED", // APPROVED, PENDING, REJECTED
    val pendingAvatar: String? = null,
    val role: String,
    val remainingAdLimit: Int = 0,
    val isPremium: Boolean = false,
    val memberNumber: Int = 0,
    val tcNo: String? = null,
    val birthYear: Int? = null,
    val isTcVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val city: String? = null,
    val district: String? = null,
    val createdAt: String? = null
)

data class CategoryDto(
    val id: String,
    val name: String,
    val type: String,
    val parentId: String? = null,
    val children: List<CategoryDto>? = null
)

data class AdImageDto(
    val id: String,
    val url: String,
    val isMain: Boolean
)

data class AdUserDto(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val avatar: String? = null
)

data class BoatDetailsDto(
    val make: String,
    val model: String,
    val year: Int,
    val length: Double,
    val hullMaterial: String,
    val enginePower: Int
)

data class FishingGearDetailsDto(
    val condition: String,
    val brand: String,
    val gearType: String
)

data class AdDto(
    val id: String,
    val adNumber: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    val status: String,
    val views: Int = 0,
    val isUrgent: Boolean = false,
    val editCount: Int = 0,
    val videoUrl: String? = null,
    val showPhone: Boolean = false,
    val city: String? = null,
    val district: String? = null,
    val expiresAt: String? = null,
    val createdAt: String? = null,
    val category: CategoryDto? = null,
    val user: AdUserDto? = null,
    val images: List<AdImageDto> = emptyList(),
    val boatDetails: BoatDetailsDto? = null,
    val fishingGearDetails: FishingGearDetailsDto? = null
)

data class AdsResponse(
    val ads: List<AdDto>,
    val total: Int,
    val page: Int,
    val pages: Int
)

data class CreateAdRequest(
    val title: String,
    val description: String,
    val price: Double,
    val categoryId: String,
    val type: String,
    val videoUrl: String? = null,
    val showPhone: Boolean = false,
    val isUrgent: Boolean = false,
    val city: String? = null,
    val district: String? = null,
    val boatDetails: BoatDetailsRequest? = null,
    val fishingGearDetails: FishingGearDetailsRequest? = null,
    val images: List<String> = emptyList()
)

data class UpdateAdRequest(
    val title: String,
    val description: String,
    val price: Double,
    val categoryId: String? = null,
    val type: String? = null,
    val showPhone: Boolean? = null,
    val isUrgent: Boolean = false,
    val city: String? = null,
    val district: String? = null,
    val boatDetails: BoatDetailsRequest? = null,
    val fishingGearDetails: FishingGearDetailsRequest? = null,
    val images: List<String>? = null
)

data class BoatDetailsRequest(
    val make: String,
    val model: String,
    val year: Int,
    val length: Double,
    val hullMaterial: String,
    val enginePower: Int
)

data class FishingGearDetailsRequest(
    val condition: String,
    val brand: String,
    val gearType: String
)

data class CreateAdResponse(
    val message: String,
    val ad: AdDto
)

data class MessageDto(
    val id: String,
    val content: String,
    val isRead: Boolean,
    val createdAt: String,
    val senderId: String? = null,
    val receiverId: String? = null,
    val adId: String? = null,
    val ad: MessageAdDto? = null,
    val sender: MessageUserDto? = null,
    val receiver: MessageUserDto? = null
)

data class MessageAdDto(
    val title: String,
    val images: List<AdImageDto> = emptyList()
)

data class MessageUserDto(
    val name: String,
    val avatar: String? = null
)

data class SendMessageRequest(
    val receiverId: String,
    val adId: String,
    val content: String
)

data class SendMessageResponse(
    val message: String,
    val data: MessageDto
)

data class PackageDto(
    val id: String,
    val name: String,
    val price: Double,
    val adLimit: Int,
    val features: String? = null,
    val googleSkuId: String? = null
)

data class IapVerifyRequest(
    val packageId: String,
    val purchaseToken: String,
    val productId: String
)

data class IapVerifyResponse(
    val message: String,
    val adLimit: Int = 0
)

data class UploadResponse(
    val urls: List<String>,
    val videoUrl: String?
)

data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val avatar: String? = null,
    val avatarStatus: String? = null,
    val tcNo: String? = null,
    val birthYear: Int? = null,
    val city: String? = null,
    val district: String? = null
)

data class SmsSendResponse(val success: Boolean, val message: String)
data class SmsVerifyRequest(val code: String)
data class TcVerifyRequest(val tckn: String, val firstName: String, val lastName: String, val birthYear: Int)

data class UpdateProfileResponse(
    val message: String,
    val user: UserDto
)
data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val data: String? = null,
    val createdAt: String? = null
)

data class UnreadCountResponse(val count: Int)

data class ReportRequest(
    val adId: String,
    val reason: String,
    val detail: String? = null
)

data class ConversationDto(
    val otherUserId: String,
    val otherUserName: String,
    val otherUserAvatar: String?,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val adId: String,
    val adTitle: String
)
data class MaintenanceResponse(
    val isUnderMaintenance: Boolean = false,
    val message: String? = null
)

data class IapOrderDto(
    val id: String,
    val packageName: String,
    val adLimit: Int,
    val status: String,
    val purchaseToken: String?,
    val createdAt: String
)