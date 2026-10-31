package com.example.avorium.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.avorium.data.AdDto
import com.example.avorium.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AdDetailScreen(
    ad: AdDto,
    currentUserId: String = "",
    onBack: () -> Unit,
    onSendMessage: (String, String, String) -> Unit,
    onReport: ((String, String, String?) -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val priceFormatter = NumberFormat.getNumberInstance(Locale("tr", "TR"))
    val context = LocalContext.current

    var showMessageDialog by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }
    var galleryIndex by remember { mutableStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("FAKE_AD") }
    var reportDetail by remember { mutableStateOf("") }
    var messageContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ad.category?.name ?: "İlan Detayı", fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyCard,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
                .fillMaxSize()
                .background(DeepNavy)
        ) {
            // Fotoğraf galerisi
            if (ad.images.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ad.images.indices.toList()) { idx ->
                        val img = ad.images[idx]
                        AsyncImage(
                            model = img.url,
                            contentDescription = "Fotoğraf",
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { galleryIndex = idx; showGallery = true },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(listOf(NavyCard, NavyElevated))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Fotoğraf Yok", color = SeaMuted)
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Baslik ve fiyat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = ad.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = priceFormatter.format(ad.price) + " TL",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldenAmber
                        )
                        val statusColor = when (ad.status) {
                            "ACTIVE" -> Color(0xFF2ECC71)
                            "PENDING_REVIEW" -> Color(0xFFF39C12)
                            "SOLD" -> OceanBlue60
                            else -> SeaMuted
                        }
                        val statusText = when (ad.status) {
                            "ACTIVE" -> "Aktif"
                            "PENDING_REVIEW" -> "Onay Bekliyor"
                            "SOLD" -> "Satıldı"
                            "PASSIVE" -> "Pasif"
                            else -> ad.status
                        }
                        Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Açıklama
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Açıklama", fontWeight = FontWeight.Bold, color = SeaMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(ad.description, color = Color.White, fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Tekne detaylari
                ad.boatDetails?.let { boat ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tekne Bilgileri", fontWeight = FontWeight.Bold, color = OceanBlue60, fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            val items = listOf(
                                "Marka" to boat.make,
                                "Model" to boat.model,
                                "Yıl" to boat.year.toString(),
                                "Boy" to "${boat.length} m",
                                "Gövde" to boat.hullMaterial,
                                "Motor" to "${boat.enginePower} HP"
                            )
                            items.chunked(2).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { (label, value) ->
                                        Column(Modifier.weight(1f)) {
                                            Text(label, fontSize = 11.sp, color = SeaMuted)
                                            Text(value, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                                        }
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Balikcilik malzeme detaylari
                ad.fishingGearDetails?.let { gear ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Malzeme Bilgileri", fontWeight = FontWeight.Bold, color = Color(0xFF2ECC71), fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            listOf("Marka" to gear.brand, "Tür" to gear.gearType, "Durum" to if(gear.condition=="NEW") "Sıfır" else "İkinci El").forEach { (l,v) ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(l, color = SeaMuted, fontSize = 13.sp)
                                    Text(v, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Satıcı kartı
                ad.user?.let { seller ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Satıcı", fontWeight = FontWeight.Bold, color = SeaMuted, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(seller.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Sadece showPhone true ise ve telefon varsa Ara butonu çıksın
                                if (ad.showPhone && seller.phone != null) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${seller.phone}"))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue60),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Ara")
                                    }
                                }
                                OutlinedButton(
                                    onClick = { showMessageDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Mesaj")
                                }
                            }
                            if (onReport != null && currentUserId.isNotEmpty() && ad.user?.id != currentUserId) {
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { showReportDialog = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Şikayet Et", color = ErrorRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("Satıcıya Mesaj Gönder", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = messageContent,
                    onValueChange = { messageContent = it },
                    label = { Text("Mesajınız") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        ad.user?.id?.let { receiverId ->
                            onSendMessage(receiverId, ad.id, messageContent)
                        }
                        showMessageDialog = false
                        messageContent = ""
                    },
                    enabled = messageContent.isNotBlank()
                ) { Text("Gönder") }
            },
            dismissButton = {
                TextButton(onClick = { showMessageDialog = false }) { Text("İptal") }
            }
        )
    }
    if (showReportDialog && onReport != null) {
        val reasons = listOf("FAKE_AD" to "Sahte İlan", "WRONG_INFO" to "Yanlış Bilgi", "SPAM" to "Spam", "INAPPROPRIATE" to "Uygunsuz", "FRAUD" to "Dolandırıcılık", "OTHER" to "Diğer")
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Şikayeti Bildir", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reasons.forEach { (value, label) ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { reportReason = value }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = reportReason == value, onClick = { reportReason = value })
                            Spacer(Modifier.width(8.dp))
                            Text(label, color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                    OutlinedTextField(value = reportDetail, onValueChange = { reportDetail = it }, label = { Text("Açıklama (opsiyonel)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onReport(ad.id, reportReason, reportDetail.ifBlank { null })
                    showReportDialog = false
                    reportDetail = ""
                }) { Text("Gönder") }
            },
            dismissButton = { TextButton(onClick = { showReportDialog = false }) { Text("İptal") } }
        )
    }
    if (showGallery && ad.images.isNotEmpty()) {
        val pagerState = rememberPagerState(initialPage = galleryIndex) { ad.images.size }
        val galleryScope = rememberCoroutineScope()
        androidx.compose.ui.window.Dialog(onDismissRequest = { showGallery = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    AsyncImage(model = ad.images[page].url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
                Text(
                    text = "${pagerState.currentPage + 1} / ${ad.images.size}",
                    color = Color.White, fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp)
                )
                if (pagerState.currentPage > 0) {
                    IconButton(onClick = { galleryScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }, modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Önceki", tint = Color.White)
                    }
                }
                if (pagerState.currentPage < ad.images.size - 1) {
                    IconButton(onClick = { galleryScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Sonraki", tint = Color.White)
                    }
                }
                IconButton(onClick = { showGallery = false }, modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 8.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                }
            }
        }
    }
}