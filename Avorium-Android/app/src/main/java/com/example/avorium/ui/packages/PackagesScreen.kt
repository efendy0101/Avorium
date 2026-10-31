package com.example.avorium.ui.packages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avorium.data.PackageDto
import com.example.avorium.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    packages: List<PackageDto>,
    isLoading: Boolean,
    onVerifyIap: (String, String, String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        TopAppBar(
            title = { Text("İlan Paketleri", fontWeight = FontWeight.Bold, color = SeaWhite) },
            actions = {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Yenile", tint = OceanBlue60)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
        )

        if (isLoading) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = OceanBlue60, trackColor = Color.Transparent) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Paket satın alma özelliği şu anda kullanılamıyor.", color = SeaMuted, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            items(packages) { pkg ->
                PackageCard(pkg = pkg, isLoading = isLoading, onBuy = {})
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PackageCard(pkg: PackageDto, isLoading: Boolean, onBuy: () -> Unit) {
    val isPopular = pkg.price in 100.0..500.0
    val gradientColors = if (isPopular) listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)) else listOf(NavyCard, NavyElevated)
    val borderColor = if (isPopular) SuccessGreen else Color.Transparent

    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(2.dp, borderColor, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(gradientColors)).padding(24.dp)) {
            Column {
                if (isPopular) {
                    Text("EN ÇOK TERCİH EDİLEN", color = GoldenAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.background(GoldenAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    Spacer(Modifier.height(8.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                    Text("${pkg.price.toInt()} TL", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = if(isPopular) Color.White else GoldenAmber)
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if(isPopular) Color.White else SuccessGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${pkg.adLimit} İlan Hakkı", color = Color.White, fontWeight = FontWeight.Medium)
                }
                if (!pkg.features.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = if(isPopular) Color.White else SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(pkg.features, color = Color.White.copy(alpha = 0.8f))
                    }
                }
                if (!pkg.googleSkuId.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("SKU: ${pkg.googleSkuId}", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onBuy,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPopular) Color.White else OceanBlue60, contentColor = if (isPopular) Color(0xFF1B5E20) else Color.White),
                    enabled = false
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Yakında", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
