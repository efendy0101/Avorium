package com.example.avorium.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avorium.data.MessageDto
import com.example.avorium.theme.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    userName: String,
    currentUserId: String,
    conversation: List<MessageDto>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSendMessage: (String, String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onLoadConversation: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val adId = conversation.firstOrNull()?.adId ?: ""
    LaunchedEffect(userId) { onLoadConversation(userId) }
    LaunchedEffect(conversation.size) {
        if (conversation.isNotEmpty()) listState.animateScrollToItem(conversation.size - 1)
    }
    Column(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        TopAppBar(
            title = { Text(userName, fontWeight = FontWeight.Bold, color = SeaWhite) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = SeaWhite)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(conversation, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUserId
                var showDelete by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Box(modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onLongPress = { if (isMe) showDelete = true })
                    }) {
                        Text(
                            text = msg.content,
                            color = if (isMe) Color.White else SeaWhite,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .background(
                                    color = if (isMe) OceanBlue else NavyElevated,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp, topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .widthIn(max = 280.dp)
                        )
                    }
                    if (showDelete && isMe) {
                        TextButton(onClick = { onDeleteMessage(msg.id); showDelete = false }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sil", color = ErrorRed, fontSize = 12.sp)
                        }
                    }
                    Text(
                        text = msg.createdAt?.take(16)?.replace("T", " ") ?: "",
                        color = SeaMuted, fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(NavyCard).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mesaj yaz...", color = SeaMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SeaWhite, unfocusedTextColor = SeaWhite,
                    focusedContainerColor = NavyElevated, unfocusedContainerColor = NavyElevated,
                    focusedBorderColor = OceanBlue, unfocusedBorderColor = NavyContainer
                ),
                shape = RoundedCornerShape(24.dp), maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (messageText.isNotBlank()) { onSendMessage(userId, adId, messageText.trim()); messageText = "" }
                })
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { if (messageText.isNotBlank()) { onSendMessage(userId, adId, messageText.trim()); messageText = "" } },
                enabled = messageText.isNotBlank() && !isLoading,
                modifier = Modifier.background(OceanBlue, RoundedCornerShape(50))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", tint = Color.White)
            }
        }
    }
}