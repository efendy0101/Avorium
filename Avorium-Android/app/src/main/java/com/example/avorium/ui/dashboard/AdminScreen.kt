package com.example.avorium.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.avorium.data.UserDto
import com.example.avorium.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    pendingUsers: List<UserDto> = emptyList(),
    pendingAvatars: List<UserDto> = emptyList(),
    onApproveTc: (String) -> Unit = {},
    onRejectTc: (String) -> Unit = {},
    onApproveAvatar: (String) -> Unit = {},
    onRejectAvatar: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Paneli", fontWeight = FontWeight.Bold, color = SeaWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = SeaWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(DeepNavy).padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text("TCKN Doğrulama Talepleri", color = SeaWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                if (pendingUsers.isEmpty()) {
                    Text("Bekleyen TCKN talebi bulunamadı.", color = SeaMuted, fontSize = 14.sp)
                }
            }

            items(pendingUsers) { user ->
                VerificationRequestCard(
                    user = user,
                    onApprove = { onApproveTc(user.id) },
                    onReject = { onRejectTc(user.id) }
                )
            }

            item {
                Text("Profil Resmi Onay Talepleri", color = SeaWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                if (pendingAvatars.isEmpty()) {
                    Text("Bekleyen resim talebi bulunamadı.", color = SeaMuted, fontSize = 14.sp)
                }
            }

            items(pendingAvatars) { user ->
                AvatarRequestCard(
                    user = user,
                    onApprove = { onApproveAvatar(user.id) },
                    onReject = { onRejectAvatar(user.id) }
                )
            }
        }
    }
}

@Composable
fun AvatarRequestCard(
    user: UserDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                AsyncImage(
                    model = user.avatar ?: user.pendingAvatar,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(user.name, color = SeaWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(user.email, color = SeaMuted, fontSize = 11.sp)
                }
            }
            Row {
                IconButton(onClick = onReject, colors = IconButtonDefaults.iconButtonColors(containerColor = ErrorRed.copy(alpha = 0.1f))) {
                    Icon(Icons.Default.Close, contentDescription = "Reddet", tint = ErrorRed)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onApprove, colors = IconButtonDefaults.iconButtonColors(containerColor = SuccessGreen.copy(alpha = 0.1f))) {
                    Icon(Icons.Default.Check, contentDescription = "Onayla", tint = SuccessGreen)
                }
            }
        }
    }
}

@Composable
fun VerificationRequestCard(
    user: UserDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OceanBlue60, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(user.name, color = SeaWhite, fontWeight = FontWeight.Bold)
                }
                Text("TC: ${user.tcNo ?: "Bilinmiyor"}", color = SeaMuted, fontSize = 12.sp)
                Text(user.email, color = SeaMuted.copy(alpha = 0.6f), fontSize = 11.sp)
            }
            Row {
                IconButton(onClick = onReject, colors = IconButtonDefaults.iconButtonColors(containerColor = ErrorRed.copy(alpha = 0.1f))) {
                    Icon(Icons.Default.Close, contentDescription = "Reddet", tint = ErrorRed)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onApprove, colors = IconButtonDefaults.iconButtonColors(containerColor = SuccessGreen.copy(alpha = 0.1f))) {
                    Icon(Icons.Default.Check, contentDescription = "Onayla", tint = SuccessGreen)
                }
            }
        }
    }
}
