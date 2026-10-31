package com.example.avorium.ui.dashboard

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.avorium.data.AdDto
import com.example.avorium.data.UserDto
import com.example.avorium.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: UserDto,
    myAds: List<AdDto>,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (String, String?, String?, String?, String?) -> Unit,
    onVerifyPhoneRequest: () -> Unit = {},
    onVerifyPhoneSubmit: (String) -> Unit = {},
    onVerifyTcSubmit: (String, String, String, Int) -> Unit = { _, _, _, _ -> },
    onEditAd: ((AdDto) -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onMyOrdersClick: (() -> Unit)? = null,
    onAdminPanelClick: (() -> Unit)? = null,
    onAvatarSelect: ((Uri) -> Unit)? = null
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user.name) }
    var editPhone by remember { mutableStateOf(user.phone ?: "") }
    var editTcNo by remember { mutableStateOf(user.tcNo ?: "") }
    var showPhoneVerifyDialog by remember { mutableStateOf(false) }
    var showTcVerifyDialog by remember { mutableStateOf(false) }
    
    val avatarPicker = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onAvatarSelect?.invoke(uri)
    }

    val isPremiumVisible = user.role == "ADMIN" || (user.isPremium && user.remainingAdLimit > 0)

    Column(
        modifier = Modifier.fillMaxSize().background(DeepNavy)
    ) {
        TopAppBar(
            title = { Text("Hesabım", fontWeight = FontWeight.Bold, color = SeaWhite) },
            actions = {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Yenile", tint = OceanBlue60)
                }
                if (onMyOrdersClick != null) {
                    IconButton(onClick = { onMyOrdersClick() }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Paketlerim", tint = GoldenAmber)
                    }
                }
                if (onSettingsClick != null) {
                    IconButton(onClick = { onSettingsClick() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = SeaMuted)
                    }
                }
                if (user.role == "ADMIN" && onAdminPanelClick != null) {
                    IconButton(onClick = onAdminPanelClick) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Panel", tint = Color(0xFFFF5722))
                    }
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış", tint = ErrorRed)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF1A3A5C), Color(0xFF0D2137))), shape = RoundedCornerShape(20.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(72.dp).clip(CircleShape)
                                        .background(Brush.linearGradient(
                                            if (isPremiumVisible) listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                                            else listOf(OceanBlue60, Color(0xFF2196F3))
                                        ))
                                        .clickable { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (user.avatar != null || user.pendingAvatar != null) {
                                        AsyncImage(
                                            model = user.avatar ?: user.pendingAvatar,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop,
                                            alpha = if (user.avatar == null) 0.5f else 1f
                                        )
                                        if (user.avatar == null) {
                                            Text("Onay Bekliyor", color = Color.White, fontSize = 8.sp, 
                                                modifier = Modifier.background(Color.Black.copy(alpha = 0.6f)).padding(2.dp).align(Alignment.BottomCenter))
                                        }
                                    } else {
                                        Text(user.name.take(2).uppercase(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SeaWhite)
                                    }
                                    if (isPremiumVisible) {
                                        Spacer(Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("PREMIUM ÜYE", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                    Text(user.email, color = SeaMuted, fontSize = 13.sp)
                                    Text("Üye No: #${user.memberNumber}", color = OceanBlue60.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    
                                    Spacer(Modifier.height(4.dp))
                                    
                                    // Verification Badges & Actions
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        // TC Status
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(!user.isTcVerified) { showTcVerifyDialog = true }) {
                                            Icon(
                                                imageVector = if (user.isTcVerified) Icons.Default.Verified else Icons.Default.GppMaybe,
                                                contentDescription = null,
                                                tint = if (user.isTcVerified) SuccessGreen else WarningOrange,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (user.isTcVerified) "TC Onaylı" else "TC Doğrula",
                                                color = if (user.isTcVerified) SuccessGreen else OceanBlue60,
                                                fontSize = 11.sp,
                                                fontWeight = if (user.isTcVerified) FontWeight.Normal else FontWeight.Bold
                                            )
                                        }

                                        // Phone Status
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(!user.isPhoneVerified) { 
                                            onVerifyPhoneRequest()
                                            showPhoneVerifyDialog = true 
                                        }) {
                                            Icon(
                                                imageVector = if (user.isPhoneVerified) Icons.Default.VerifiedUser else Icons.Default.PhonePaused,
                                                contentDescription = null,
                                                tint = if (user.isPhoneVerified) SuccessGreen else WarningOrange,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (user.isPhoneVerified) "Tel Onaylı" else "Tel Doğrula",
                                                color = if (user.isPhoneVerified) SuccessGreen else OceanBlue60,
                                                fontSize = 11.sp,
                                                fontWeight = if (user.isPhoneVerified) FontWeight.Normal else FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoChip(icon = Icons.Default.Phone, label = user.phone ?: "Telefon eklenmedi", color = if (user.phone != null) Teal60 else SeaMuted, modifier = Modifier.weight(1f))
                                if (user.role != "ADMIN") {
                                    InfoChip(icon = Icons.Default.Star, label = "${user.remainingAdLimit} İlan Hakkı", color = GoldenAmber, modifier = Modifier.weight(1f))
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { editName = user.name; editPhone = user.phone ?: ""; editTcNo = user.tcNo ?: ""; showEditDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue60.copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Profili Düzenle", color = OceanBlue60)
                            }
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(icon = Icons.Default.List, value = "${myAds.size}", label = "Toplam İlan", color = OceanBlue60, modifier = Modifier.weight(1f))
                    StatCard(icon = Icons.Default.CheckCircle, value = "${myAds.count { it.status == "ACTIVE" }}", label = "Aktif", color = SuccessGreen, modifier = Modifier.weight(1f))
                    StatCard(icon = Icons.Default.Info, value = "${myAds.count { it.status == "PENDING_REVIEW" }}", label = "Bekliyor", color = WarningOrange, modifier = Modifier.weight(1f))
                }
            }

            item { Text("İlanlarım", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SeaWhite) }

            if (myAds.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(NavyCard).padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SeaMuted, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Henüz ilan vermediniz.", color = SeaMuted)
                        }
                    }
                }
            }

            items(myAds, key = { it.id }) { ad ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ad.images.firstOrNull()?.url,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp).clip(RoundedCornerShape(10.dp)).background(NavyCard),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ad.title, fontWeight = FontWeight.Bold, color = SeaWhite, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                                if (ad.isUrgent) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE53935).copy(alpha = 0.2f)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                                        Text("ACIL", color = Color(0xFFE53935), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(ad.category?.name ?: "", color = SeaMuted, fontSize = 12.sp)
                            Text("İlan No: #${ad.adNumber}", color = SeaMuted.copy(alpha = 0.6f), fontSize = 10.sp)
                            Text("${ad.price.toInt()} TL", fontWeight = FontWeight.ExtraBold, color = GoldenAmber, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val statusColor = when (ad.status) { "ACTIVE" -> SuccessGreen; "PENDING_REVIEW" -> WarningOrange; "SOLD" -> OceanBlue60; else -> SeaMuted }
                            val statusText = when (ad.status) { "ACTIVE" -> "Aktif"; "PENDING_REVIEW" -> "Bekliyor"; "SOLD" -> "Satıldı"; "PASSIVE" -> "Pasif"; else -> ad.status }
                            Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            if (onEditAd != null && ad.editCount < 2) {
                                Spacer(Modifier.height(8.dp))
                                IconButton(onClick = { onEditAd(ad) }, modifier = Modifier.size(32.dp).background(OceanBlue60.copy(alpha = 0.1f), CircleShape)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", modifier = Modifier.size(16.dp), tint = OceanBlue60)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Profili Düzenle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Ad Soyad") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Telefon") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editTcNo, onValueChange = { if (it.length <= 11) editTcNo = it }, label = { Text("TC Kimlik No") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = { onUpdateProfile(editName, editPhone.ifBlank { null }, editTcNo.ifBlank { null }, null, null); showEditDialog = false }) { Text("Kaydet") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("İptal") } }
        )
    }

    if (showPhoneVerifyDialog) {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPhoneVerifyDialog = false },
            title = { Text("Telefon Doğrulama", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Lütfen telefonunuza gelen 6 haneli doğrulama kodunu giriniz.", color = SeaMuted, fontSize = 13.sp)
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6) code = it },
                        label = { Text("Doğrulama Kodu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onVerifyPhoneSubmit(code); showPhoneVerifyDialog = false },
                    enabled = code.length == 6
                ) { Text("Doğrula") }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneVerifyDialog = false }) { Text("İptal") }
            }
        )
    }

    if (showTcVerifyDialog) {
        var tckn by remember { mutableStateOf(user.tcNo ?: "") }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var birthYear by remember { mutableStateOf(user.birthYear?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showTcVerifyDialog = false },
            title = { Text("TCKN Doğrulama (MERNİS)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Bilgileriniz MERNİS üzerinden sorgulanacaktır. Lütfen kimliğinizdeki gibi giriniz.", color = SeaMuted, fontSize = 12.sp)
                    OutlinedTextField(value = tckn, onValueChange = { if (it.length <= 11) tckn = it }, label = { Text("TC Kimlik No") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Ad") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Soyad") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = birthYear, onValueChange = { if (it.length <= 4) birthYear = it }, label = { Text("Doğum Yılı (Örn: 1990)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onVerifyTcSubmit(tckn, firstName, lastName, birthYear.toIntOrNull() ?: 0)
                        showTcVerifyDialog = false 
                    },
                    enabled = tckn.length == 11 && firstName.isNotBlank() && lastName.isNotBlank() && birthYear.length == 4
                ) { Text("Sorgula") }
            },
            dismissButton = {
                TextButton(onClick = { showTcVerifyDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun InfoChip(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun StatCard(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SeaWhite)
            Text(label, color = SeaMuted, fontSize = 11.sp)
        }
    }
}
