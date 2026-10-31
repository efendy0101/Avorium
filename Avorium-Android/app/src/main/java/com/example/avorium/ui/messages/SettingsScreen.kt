package com.example.avorium.ui.messages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avorium.BuildConfig
import com.example.avorium.data.UserDto
import com.example.avorium.theme.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: UserDto,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Çıkış Yap", color = SeaWhite) },
            text = { Text("Hesabınızdan çıkmak istediğinize emin misiniz?", color = SeaMuted) },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Çıkış Yap", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("İptal", color = OceanBlue) }
            },
            containerColor = NavyCard
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        TopAppBar(
            title = { Text("Ayarlar", fontWeight = FontWeight.Bold, color = SeaWhite) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = SeaWhite)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(user.name, fontWeight = FontWeight.Bold, color = SeaWhite, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(user.email, color = SeaMuted, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                SettingsSection(title = "Uygulama") {
                    SettingsItem(title = "Hakkımızda", onClick = { openUrl("https://avorium.com") })
                    SettingsItem(title = "Yardım ve Destek", onClick = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:info@avorium.com")))
                    })
                    SettingsItem(title = "Sık Sorulan Sorular", onClick = { openUrl("https://avorium.com") })
                }
            }

            item {
                SettingsSection(title = "Yasal") {
                    SettingsItem(title = "Gizlilik Politikası", onClick = { openUrl("https://avorium.com/privacy.html") })
                    SettingsItem(title = "KVKK", onClick = { openUrl("https://avorium.com/kvkk.html") })
                    SettingsItem(title = "Kullanıcı Sözleşmesi", onClick = { openUrl("https://avorium.com/terms.html") })
                    SettingsItem(title = "Hesap Silme", onClick = { openUrl("https://avorium.com/account-delete.html") })
                }
            }

            item {
                SettingsSection(title = "Hesap") {
                    SettingsItem(
                        title = "Çıkış Yap",
                        titleColor = ErrorRed,
                        showArrow = false,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Avorium v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    color = SeaMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, color = SeaMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { content() }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SettingsItem(
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = SeaWhite,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = titleColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (showArrow) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = SeaMuted)
        }
    }
    HorizontalDivider(color = NavyContainer, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}