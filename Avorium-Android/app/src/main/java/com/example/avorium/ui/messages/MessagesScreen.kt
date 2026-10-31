package com.example.avorium.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avorium.data.MessageDto
import com.example.avorium.theme.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    messages: List<MessageDto>,
    currentUserId: String,
    onConversationClick: (String, String) -> Unit,
    onNotificationsClick: () -> Unit,
    unreadNotificationCount: Int = 0
) {
    // Group messages by conversation partner
    val conversations = remember(messages, currentUserId) {
        messages
            .groupBy { msg ->
                if (msg.senderId == currentUserId) msg.receiverId ?: "" else msg.senderId ?: ""
            }
            .filter { it.key.isNotEmpty() }
            .map { (otherId, msgs) ->
                val lastMsg = msgs.maxByOrNull { it.createdAt }!!
                val otherUser = if (lastMsg.senderId == currentUserId) lastMsg.receiver else lastMsg.sender
                val unread = msgs.count { !it.isRead && it.senderId != currentUserId }
                Triple(otherId, otherUser?.name ?: "Kullanici", Pair(lastMsg, unread))
            }
            .sortedByDescending { it.third.first.createdAt }
    }

    Column(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        TopAppBar(
            title = { Text("Mesajlar", fontWeight = FontWeight.Bold, color = SeaWhite) },
            actions = {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) Badge { Text("$unreadNotificationCount") }
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Bildirimler", tint = SeaWhite)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
        )

        if (conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Mail, contentDescription = null, tint = SeaMuted, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Henuz hic mesajiniz yok.", color = SeaMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations, key = { it.first }) { (userId, userName, msgPair) ->
                    val (lastMsg, unreadCount) = msgPair
                    val isMe = lastMsg.senderId == currentUserId
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            onConversationClick(userId, userName)
                        },
                        colors = CardDefaults.cardColors(containerColor = NavyElevated),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(OceanBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = userName,
                                        fontWeight = FontWeight.Bold,
                                        color = SeaWhite,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = lastMsg.createdAt?.take(10) ?: "",
                                        color = SeaMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isMe) Text("Sen: ", color = SeaMuted, fontSize = 12.sp)
                                    Text(
                                        text = lastMsg.content,
                                        color = if (unreadCount > 0) SeaWhite else SeaMuted,
                                        fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (unreadCount > 0) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier.size(20.dp).clip(CircleShape).background(OceanBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$unreadCount", color = androidx.compose.ui.graphics.Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}