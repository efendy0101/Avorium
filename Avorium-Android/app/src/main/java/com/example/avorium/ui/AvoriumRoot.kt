package com.example.avorium.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.avorium.data.AdDto
import com.example.avorium.ui.auth.LoginScreen
import com.example.avorium.ui.dashboard.DashboardScreen
import com.example.avorium.ui.dashboard.AdminScreen
import com.example.avorium.ui.home.AddAdScreen
import com.example.avorium.ui.home.AdDetailScreen
import com.example.avorium.ui.home.HomeScreen
import com.example.avorium.ui.messages.MessagesScreen
import com.example.avorium.ui.messages.ChatScreen
import com.example.avorium.ui.messages.NotificationsScreen
import com.example.avorium.ui.messages.SettingsScreen
import com.example.avorium.ui.packages.PackagesScreen
import com.example.avorium.ui.packages.MyOrdersScreen
import com.example.avorium.ui.maintenance.MaintenanceScreen

private enum class AppTab(val label: String) {
    Home("İlanlar"),
    Add("İlan Ver"),
    Messages("Mesajlar"),
    Packages("Paketler"),
    Account("Hesabım")
}

@Composable
fun AvoriumRoot(viewModel: AvoriumViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    var editingAd by remember { mutableStateOf<AdDto?>(null) }
    var chatUserId by remember { mutableStateOf<String?>(null) }
    var chatUserName by remember { mutableStateOf("") }
    var showNotifications by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showMyOrders by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.checkMaintenance() }

    // After ad created, go to Account > İlanlarım
    LaunchedEffect(state.adCreatedSuccess) {
        if (state.adCreatedSuccess) {
            selectedTab = AppTab.Account
            viewModel.clearAdCreatedSuccess()
        }
    }

    // BackHandler
    BackHandler(enabled = state.selectedAd != null || editingAd != null || chatUserId != null || showNotifications || showSettings || showAdminPanel) {
        when {
            showAdminPanel -> showAdminPanel = false
            showSettings -> showSettings = false
            showNotifications -> showNotifications = false
            chatUserId != null -> chatUserId = null
            editingAd != null -> editingAd = null
            else -> viewModel.selectAd(null)
        }
    }

    LaunchedEffect(state.error, state.notice) {
        val message = state.error ?: state.notice
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    // Maintenance mode
    if (state.isUnderMaintenance) {
        MaintenanceScreen(message = state.maintenanceMessage, onRetry = { viewModel.checkMaintenance() })
        return
    }

    val user = state.user
    if (user == null) {
        LoginScreen(
            isLoading = state.isLoading,
            errorMessage = state.error,
            onLogin = viewModel::login,
            onRegister = viewModel::register
        )
        return
    }

    // Settings screen
    if (showSettings) {
        SettingsScreen(
            user = user,
            onBack = { showSettings = false },
            onLogout = { viewModel.logout(); showSettings = false }
        )
        return
    }

    // MyOrders screen
    if (showMyOrders) {
        MyOrdersScreen(orders = state.myOrders, isLoading = state.isLoading, onBack = { showMyOrders = false }, onRefresh = { viewModel.loadMyOrders() })
        return
    }

    // Notifications screen
    if (showNotifications) {
        NotificationsScreen(
            notifications = state.notifications,
            onBack = { showNotifications = false },
            onMarkRead = viewModel::markNotificationRead,
            onMarkAllRead = viewModel::markAllNotificationsRead
        )
        return
    }

    // Admin Panel screen
    if (showAdminPanel) {
        AdminScreen(
            onBack = { showAdminPanel = false },
            pendingUsers = emptyList(), // For demo purposes
            pendingAvatars = state.pendingAvatars,
            onApproveAvatar = viewModel::approveAvatar,
            onRejectAvatar = viewModel::rejectAvatar
        )
        return
    }

    // Chat screen
    if (chatUserId != null) {
        ChatScreen(
            userId = chatUserId!!,
            userName = chatUserName,
            currentUserId = user.id,
            conversation = state.conversation,
            isLoading = state.isLoading,
            onBack = { chatUserId = null },
            onSendMessage = { receiverId, adId, content ->
                viewModel.sendMessage(receiverId, adId, content)
            },
            onDeleteMessage = viewModel::deleteMessage,
            onLoadConversation = viewModel::loadConversation
        )
        return
    }

    // Edit Ad screen
    if (editingAd != null) {
        val ad = editingAd!!
        AddAdScreen(
            categories = state.categories,
            isLoading = state.isLoading,
            editAd = ad,
            onSubmit = viewModel::createAdWithMedia,
            onEditSubmit = { title, desc, price, isUrgent, city, district, catId, type, boat, gear, images, showPhone ->
                viewModel.updateMyAd(ad.id, title, desc, price, isUrgent, city, district, catId, type, boat, gear, images, showPhone)
                editingAd = null
                selectedTab = AppTab.Account
            },
            onCancel = { editingAd = null }
        )
        return
    }

    // Detail screen
    if (state.selectedAd != null) {
        AdDetailScreen(
            ad = state.selectedAd!!,
            currentUserId = user.id,
            onBack = { viewModel.selectAd(null) },
            onSendMessage = viewModel::sendMessage,
            onReport = viewModel::createReport
        )
        return
    }
    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            if (tab == AppTab.Messages && state.unreadNotificationCount > 0) {
                                BadgedBox(badge = { Badge { Text("${state.unreadNotificationCount}") } }) {
                                    Icon(Icons.Default.Mail, contentDescription = tab.label)
                                }
                            } else {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.Home -> Icons.Default.Home
                                        AppTab.Add -> Icons.Default.AddCircle
                                        AppTab.Messages -> Icons.Default.Mail
                                        AppTab.Packages -> Icons.Default.ShoppingCart
                                        AppTab.Account -> Icons.Default.Person
                                    },
                                    contentDescription = tab.label
                                )
                            }
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally()).togetherWith(fadeOut() + slideOutHorizontally())
                },
                label = "tab_animation"
            ) { tab ->
                when (tab) {
                    AppTab.Home -> HomeScreen(
                        ads = state.ads,
                        selectedCategory = state.selectedCategoryType,
                        selectedCategoryId = state.selectedCategoryId,
                        selectedParentCategoryId = state.selectedParentCategoryId,
                        selectedCity = state.selectedCity,
                        selectedDistrict = state.selectedDistrict,
                        categories = state.categories,
                        search = state.search,
                        onCategorySelect = viewModel::setSelectedCategory,
                        onParentCategorySelect = viewModel::setSelectedParentCategory,
                        onCategoryIdSelect = viewModel::setSelectedCategoryId,
                        onCitySelect = viewModel::setSelectedCity,
                        onDistrictSelect = viewModel::setSelectedDistrict,
                        onSearchChange = viewModel::setSearch,
                        onAdClick = { ad -> viewModel.selectAd(ad) }
                    )
                    AppTab.Add -> AddAdScreen(
                        categories = state.categories,
                        isLoading = state.isLoading,
                        onSubmit = viewModel::createAdWithMedia
                    )
                    AppTab.Messages -> MessagesScreen(
                        messages = state.messages,
                        currentUserId = user.id,
                        onConversationClick = { userId, userName ->
                            chatUserId = userId
                            chatUserName = userName
                            viewModel.loadConversation(userId)
                        },
                        onNotificationsClick = { showNotifications = true },
                        unreadNotificationCount = state.unreadNotificationCount
                    )
                    AppTab.Packages -> PackagesScreen(
                        packages = state.packages,
                        isLoading = state.isLoading,
                        onVerifyIap = viewModel::verifyIap,
                        onRefresh = viewModel::refreshPublicData
                    )
                    AppTab.Account -> DashboardScreen(
                        user = user,
                        myAds = state.myAds,
                        onRefresh = { viewModel.refreshPrivateData() },
                        onLogout = { viewModel.logout() },
                        onUpdateProfile = viewModel::updateProfile,
                        onVerifyPhoneRequest = viewModel::sendVerificationSms,
                        onVerifyPhoneSubmit = viewModel::verifyPhone,
                        onVerifyTcSubmit = viewModel::verifyTc,
                        onEditAd = { ad -> editingAd = ad },
                        onSettingsClick = { showSettings = true },
                        onMyOrdersClick = { viewModel.loadMyOrders(); showMyOrders = true },
                        onAdminPanelClick = { viewModel.loadPendingAvatars(); showAdminPanel = true },
                        onAvatarSelect = viewModel::updateAvatarWithMedia
                    )
                }
            }
        }
    }
}