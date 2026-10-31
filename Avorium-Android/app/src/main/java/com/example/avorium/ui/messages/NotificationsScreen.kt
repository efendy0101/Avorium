package com.example.avorium.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avorium.data.NotificationDto
import com.example.avorium.theme.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationDto>,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        TopAppBar(
            title = { Text("Bildirimler", fontWeight = FontWeight.Bold, color = SeaWhite) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = SeaWhite)
                }
            },
            actions = {
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Tumunu oku", color = OceanBlue, fontSize = 12.sp)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
        )
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = SeaMuted, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Henuz bildirim yok.", color = SeaMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isRead) NavyCard else NavyElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { if (!notif.isRead) onMarkRead(notif.id) }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!notif.isRead) {
                                Box(modifier = Modifier.size(8.dp).background(OceanBlue, RoundedCornerShape(50)))
                                Spacer(Modifier.width(10.dp))
                            } else {
                                Spacer(Modifier.width(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.title, fontWeight = FontWeight.SemiBold, color = SeaWhite, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(notif.body, color = SeaMuted, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    notif.createdAt?.take(16)?.replace("T", " ") ?: "",
                                    color = SeaMuted, fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}