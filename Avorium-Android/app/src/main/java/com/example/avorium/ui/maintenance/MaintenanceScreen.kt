package com.example.avorium.ui.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avorium.theme.*

@Composable
fun MaintenanceScreen(
    message: String? = null,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(DeepNavy),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\uD83D\uDD27", fontSize = 64.sp)
        Spacer(Modifier.height(24.dp))
        Text("Bakim Modu", fontWeight = FontWeight.Bold, color = SeaWhite, fontSize = 24.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = message ?: "Uygulama su anda bakim modunda. Lutfen daha sonra tekrar deneyin.",
            color = SeaMuted, fontSize = 15.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue60),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth()
        ) { Text("Tekrar Dene", fontWeight = FontWeight.Bold) }
    }
}