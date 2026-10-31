package com.example.avorium.ui

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.avorium.data.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class AppUiState(
    val user: UserDto? = null,
    val selectedCategoryType: String = "FISHING_GEAR",
    val selectedCategoryId: String? = null,
    val selectedParentCategoryId: String? = null,
    val selectedCity: String? = null,
    val selectedDistrict: String? = null,
    val search: String = "",
    val ads: List<AdDto> = emptyList(),
    val myAds: List<AdDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList(),
    val messages: List<MessageDto> = emptyList(),
    val conversation: List<MessageDto> = emptyList(),
    val packages: List<PackageDto> = emptyList(),
    val notifications: List<NotificationDto> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val selectedAd: AdDto? = null,
    val adCreatedSuccess: Boolean = false,
    val isUnderMaintenance: Boolean = false,
    val maintenanceMessage: String? = null,
    val myOrders: List<IapOrderDto> = emptyList(),
    val pendingAvatars: List<UserDto> = emptyList()
)
class AvoriumViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AvoriumRepositoryFactory.create(application)
    private val _uiState = MutableStateFlow(AppUiState(user = repository.cachedUser))
    val uiState: StateFlow<AppUiState> = _uiState
    private val gson = Gson()

    init { refreshPublicData(); restoreSession() }

    fun login(email: String, password: String) = runRequest {
        val response = repository.login(email, password)
        _uiState.update { it.copy(user = response.user, notice = response.message) }
        refreshPrivateData()
    }

    fun register(email: String, password: String, name: String, phone: String?) = runRequest {
        val response = repository.register(email, password, name, phone)
        _uiState.update { it.copy(user = response.user, notice = response.message) }
        refreshPrivateData()
    }

    fun logout() {
        repository.logout()
        _uiState.update { it.copy(user = null, myAds = emptyList(), messages = emptyList(), notifications = emptyList()) }
    }

    fun updateProfile(name: String, phone: String?, tcNo: String? = null, city: String? = null, district: String? = null) = runRequest {
        val user = repository.updateProfile(name, phone, tcNo, city, district)
        _uiState.update { it.copy(user = user, notice = "Profil güncellendi.") }
    }

    fun setSelectedCategory(type: String) {
        _uiState.update { it.copy(selectedCategoryType = type, selectedCategoryId = null, selectedParentCategoryId = null) }
        loadAds()
    }

    fun setSelectedParentCategory(parentId: String?) {
        _uiState.update { it.copy(selectedParentCategoryId = parentId, selectedCategoryId = null) }
        loadAds()
    }

    fun setSelectedCategoryId(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadAds()
    }

    fun setSelectedCity(city: String?) {
        _uiState.update { it.copy(selectedCity = city, selectedDistrict = null) }
        loadAds()
    }

    fun setSelectedDistrict(district: String?) {
        _uiState.update { it.copy(selectedDistrict = district) }
        loadAds()
    }

    fun setSearch(value: String) {
        _uiState.update { it.copy(search = value) }
        if (value.length >= 3 || value.isEmpty()) loadAds()
    }

    fun refreshPublicData() { checkMaintenance(); loadAds(); loadCategories(); loadPackages() }

    fun refreshPrivateData() {
        if (_uiState.value.user == null) return
        loadMyAds(); loadMessages(); loadNotifications()
    }

    fun checkMaintenance() = runRequest {
        val r = repository.getMaintenanceStatus()
        _uiState.update { it.copy(isUnderMaintenance = r.isUnderMaintenance, maintenanceMessage = r.message) }
    }

    fun loadMyOrders() = runRequest {
        val orders = repository.getMyOrders()
        _uiState.update { it.copy(myOrders = orders) }
    }

    fun clearAdCreatedSuccess() { _uiState.update { it.copy(adCreatedSuccess = false) } }
    private fun getFileFromUri(uri: Uri): File? {
        try {
            val contentResolver = getApplication<Application>().contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val fileName = nameIndex?.let { cursor.getString(it) } ?: "file_"
            cursor?.close()
            val tempFile = File(getApplication<Application>().cacheDir, fileName)
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close(); outputStream.close()
            return tempFile
        } catch (e: Exception) { return null }
    }

    fun createAdWithMedia(
        title: String, description: String, price: Double, categoryId: String, type: String,
        boatDetails: BoatDetailsRequest?, fishingGearDetails: FishingGearDetailsRequest?,
        imageUris: List<Uri>, videoUri: Uri?, showPhone: Boolean = false,
        isUrgent: Boolean = false, city: String? = null, district: String? = null
    ) = runRequest {
        var imageUrls = emptyList<String>()
        var videoUrl: String? = null
        if (imageUris.isNotEmpty() || videoUri != null) {
            val imageParts = imageUris.mapNotNull { uri ->
                getFileFromUri(uri)?.let { file ->
                    val rb = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("images", file.name, rb)
                }
            }
            val videoPart = videoUri?.let { uri ->
                getFileFromUri(uri)?.let { file ->
                    val rb = file.asRequestBody("video/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("video", file.name, rb)
                }
            }
            val uploadRes = repository.uploadMedia(imageParts, videoPart)
            imageUrls = uploadRes.urls; videoUrl = uploadRes.videoUrl
        }
        val response = repository.createAd(CreateAdRequest(
            title = title, description = description, price = price, categoryId = categoryId,
            type = type, videoUrl = videoUrl, showPhone = showPhone, isUrgent = isUrgent,
            city = city, district = district, boatDetails = boatDetails,
            fishingGearDetails = fishingGearDetails, images = imageUrls))
        _uiState.update { it.copy(notice = response.message, adCreatedSuccess = true) }
        loadMyAds()
    }

    fun updateMyAd(
        adId: String,
        title: String,
        description: String,
        price: Double,
        isUrgent: Boolean,
        city: String?,
        district: String?,
        categoryId: String? = null,
        type: String? = null,
        boatDetails: BoatDetailsRequest? = null,
        fishingGearDetails: FishingGearDetailsRequest? = null,
        images: List<String>? = null,
        showPhone: Boolean? = null
    ) = runRequest {
        val response = repository.updateMyAd(
            adId,
            UpdateAdRequest(title, description, price, categoryId, type, showPhone, isUrgent, city, district, boatDetails, fishingGearDetails, images)
        )
        _uiState.update { it.copy(notice = response.message) }
        loadMyAds(); loadAds()
    }

    fun verifyIap(packageId: String, purchaseToken: String, productId: String) = runRequest {
        val response = repository.verifyIap(packageId, purchaseToken, productId)
        _uiState.update { it.copy(notice = response.message) }
        restoreSession()
    }

    fun sendMessage(receiverId: String, adId: String, content: String) = runRequest {
        repository.sendMessage(receiverId, adId, content)
        _uiState.update { it.copy(notice = "Mesaj gonderildi.") }
        loadMessages(); loadConversation(receiverId)
    }

    fun loadConversation(userId: String) = runRequest(showLoading = false) {
        val msgs = repository.getConversation(userId)
        _uiState.update { it.copy(conversation = msgs) }
    }

    fun deleteMessage(messageId: String) = runRequest(showLoading = false) {
        repository.deleteMessage(messageId); loadMessages()
    }

    fun createReport(adId: String, reason: String, detail: String? = null) = runRequest {
        repository.createReport(adId, reason, detail)
        _uiState.update { it.copy(notice = "Sikayet iletildi. Tesekkurler.") }
    }

    fun markNotificationRead(id: String) = runRequest(showLoading = false) {
        repository.markNotificationRead(id); loadNotifications()
    }

    fun markAllNotificationsRead() = runRequest(showLoading = false) {
        repository.markNotificationRead("all"); loadNotifications()
    }

    fun clearMessages() { _uiState.update { it.copy(error = null, notice = null) } }
    fun selectAd(ad: AdDto?) { _uiState.update { it.copy(selectedAd = ad) } }

    fun loadAdDetail(adId: String) = runRequest {
        val ad = repository.getAdById(adId)
        _uiState.update { it.copy(selectedAd = ad) }
    }

    fun sendVerificationSms() = runRequest {
        val res = repository.sendVerificationSms()
        _uiState.update { it.copy(notice = res.message) }
    }

    fun verifyPhone(code: String) = runRequest {
        val user = repository.verifyPhone(code)
        _uiState.update { it.copy(user = user, notice = "Telefon doğrulandı.") }
    }

    fun verifyTc(tckn: String, firstName: String, lastName: String, birthYear: Int) = runRequest {
        val user = repository.verifyTc(tckn, firstName, lastName, birthYear)
        _uiState.update { it.copy(user = user, notice = "TCKN doğrulandı.") }
    }

    fun updateAvatarWithMedia(uri: Uri) = runRequest {
        val file = getFileFromUri(uri) ?: return@runRequest
        val part = MultipartBody.Part.createFormData(
            "images", file.name,
            file.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val uploadResponse = repository.uploadMedia(listOf(part), null)
        val imageUrl = uploadResponse.urls.firstOrNull() ?: return@runRequest
        
        // Update profile with pending avatar
        val user = repository.updateProfile(
            name = null, phone = null, tcNo = null, city = null, district = null,
            avatar = imageUrl
        )
        _uiState.update { it.copy(user = user, notice = "Profil resmi yüklendi, onay bekliyor.") }
    }

    fun loadPendingAvatars() = runRequest(showLoading = false) {
        val users = repository.getPendingAvatars()
        _uiState.update { it.copy(pendingAvatars = users) }
    }

    fun approveAvatar(userId: String) = runRequest {
        repository.updateAvatarStatus(userId, "APPROVED")
        loadPendingAvatars()
    }

    fun rejectAvatar(userId: String) = runRequest {
        repository.updateAvatarStatus(userId, "REJECTED")
        loadPendingAvatars()
    }

    private fun restoreSession() = runRequest(showLoading = false) {
        val user = repository.loadCurrentUser()
        if (user != null) { _uiState.update { it.copy(user = user) }; refreshPrivateData() }
    }

    private fun loadAds() = runRequest(showLoading = false, reportError = false) {
        val state = _uiState.value
        val searchQuery = state.search.let { if (it.length >= 3) it else null }
        val ads = repository.getAds(state.selectedCategoryType, searchQuery, state.selectedCategoryId, state.selectedCity, state.selectedDistrict)
        _uiState.update { it.copy(ads = ads) }
    }

    private fun loadCategories() = runRequest(showLoading = false, reportError = false) {
        val categories = repository.getCategories()
        val sortedAndEnhanced = (categories + listOf(
            CategoryDto("extra-1", "Olta Takımları", "FISHING_GEAR"),
            CategoryDto("extra-2", "Misinalar", "FISHING_GEAR"),
            CategoryDto("extra-3", "İğneler", "FISHING_GEAR")
        )).distinctBy { it.name }.sortedBy { it.name }
        _uiState.update { it.copy(categories = sortedAndEnhanced) }
    }

    private fun loadPackages() = runRequest(showLoading = false, reportError = false) {
        val packages = repository.getPackages()
        _uiState.update { it.copy(packages = packages) }
    }

    private fun loadMyAds() = runRequest(showLoading = false) {
        val ads = repository.getMyAds()
        _uiState.update { it.copy(myAds = ads) }
    }

    private fun loadMessages() = runRequest(showLoading = false) {
        val messages = repository.getMessages()
        _uiState.update { it.copy(messages = messages) }
    }

    private fun loadNotifications() = runRequest(showLoading = false, reportError = false) {
        val notifications = repository.getNotifications()
        val unread = notifications.count { !it.isRead }
        _uiState.update { it.copy(notifications = notifications, unreadNotificationCount = unread) }
    }

    private fun runRequest(showLoading: Boolean = true, reportError: Boolean = true, block: suspend () -> Unit) {
        viewModelScope.launch {
            if (showLoading) _uiState.update { it.copy(isLoading = true, error = null, notice = null) }
            try { block() } catch (error: Exception) {
                if (reportError) _uiState.update { it.copy(error = error.toUserMessage()) }
            } finally {
                if (showLoading) _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun Exception.toUserMessage(): String = when (this) {
        is HttpException -> {
            val rawError = response()?.errorBody()?.string()
            val apiMessage = rawError?.let { runCatching { com.google.gson.Gson().fromJson(it, ErrorResponse::class.java).message }.getOrNull() }
            apiMessage ?: "Sunucu istegi tamamlayamadi. Kod: ${code()}"
        }
        is ConnectException, is UnknownHostException, is SocketTimeoutException -> "API sunucusuna ulasilamadi."
        else -> message ?: "Islem tamamlanamadi."
    }
}