package com.example.avorium.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avorium.data.IapOrderDto
import com.example.avorium.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    orders: List<IapOrderDto>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        TopAppBar(
            title = { Text("Paketlerim", fontWeight = FontWeight.Bold, color = SeaWhite) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = SeaWhite) } },
            actions = { IconButton(onClick = onRefresh) { Icon(Icons.Default.ArrowBack, contentDescription = "Yenile", tint = OceanBlue60) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
        )
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = OceanBlue60) }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("\uD83D\uDCE6", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Henüz paket satın almadınız.", color = SeaMuted, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(orders) { order ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = NavyCard), shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(order.packageName, fontWeight = FontWeight.Bold, color = SeaWhite, fontSize = 16.sp)
                                val statusColor = when (order.status) { "COMPLETED" -> Color(0xFF2ECC71); "PENDING" -> Color(0xFFF39C12); else -> SeaMuted }
                                val statusText = when (order.status) { "COMPLETED" -> "Tamamlandı"; "PENDING" -> "Bekliyor"; else -> order.status }
                                Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(" ilan hakkı", color = OceanBlue60, fontSize = 14.sp)
                            Text(order.createdAt.take(10), color = SeaMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}