package com.example.avorium.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.avorium.data.*
import com.example.avorium.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAdScreen(
    categories: List<CategoryDto> = emptyList(),
    isLoading: Boolean = false,
    editAd: AdDto? = null,
    onSubmit: (String, String, Double, String, String, BoatDetailsRequest?, FishingGearDetailsRequest?, List<Uri>, Uri?, Boolean, Boolean) -> Unit,
    onEditSubmit: ((String, String, Double, Boolean, String?, String?, String?, String?, BoatDetailsRequest?, FishingGearDetailsRequest?, List<String>?, Boolean?) -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(editAd?.title ?: "") }
    var description by remember { mutableStateOf(editAd?.description ?: "") }
    var price by remember { mutableStateOf(editAd?.price?.toInt()?.toString() ?: "") }
    var showPhone by remember { mutableStateOf(editAd?.showPhone ?: true) }
    var isUrgent by remember { mutableStateOf(editAd?.isUrgent ?: false) }
    var selectedCity by remember { mutableStateOf(editAd?.city ?: "") }
    var selectedDistrict by remember { mutableStateOf(editAd?.district ?: "") }
    var showCityDialog by remember { mutableStateOf(false) }
    var showDistrictDialog by remember { mutableStateOf(false) }
    val availableDistricts = if (selectedCity.isNotBlank()) TurkeyLocations.getDistricts(selectedCity) else emptyList()
    var selectedType by remember { mutableStateOf<String?>(editAd?.category?.type) }
    var selectedParentCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedSubCategoryId by remember { mutableStateOf<String?>(editAd?.category?.id) }
    val parentCategories = categories.filter { it.type == selectedType && it.parentId == null }.sortedBy { it.name }
    val subCategories = categories.filter { it.parentId == selectedParentCategoryId }.sortedBy { it.name }
    var make by remember { mutableStateOf(editAd?.boatDetails?.make ?: "") }
    var model by remember { mutableStateOf(editAd?.boatDetails?.model ?: "") }
    var year by remember { mutableStateOf(editAd?.boatDetails?.year?.toString() ?: "") }
    var length by remember { mutableStateOf(editAd?.boatDetails?.length?.toString() ?: "") }
    var hullMaterial by remember { mutableStateOf(editAd?.boatDetails?.hullMaterial ?: "") }
    var enginePower by remember { mutableStateOf(editAd?.boatDetails?.enginePower?.toString() ?: "") }
    var condition by remember { mutableStateOf("NEW") }
    var gearBrand by remember { mutableStateOf(editAd?.fishingGearDetails?.brand ?: "") }
    var gearType by remember { mutableStateOf(editAd?.fishingGearDetails?.gearType ?: "") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    val multiplePhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris -> imageUris = uris }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) videoUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editAd != null) "İlanı Düzenle" else "Yeni İlan Ver", fontWeight = FontWeight.Bold, color = SeaWhite) },
                navigationIcon = { if (onCancel != null) IconButton(onClick = onCancel) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = SeaWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyCard)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().background(DeepNavy).padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("İlan Türü", fontWeight = FontWeight.Bold, color = SeaWhite, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TypeCard(emoji = "\uD83C\uDFA3", title = "Balık Av Malzemeleri", isSelected = selectedType == "FISHING_GEAR", onClick = { selectedType = "FISHING_GEAR"; selectedParentCategoryId = null; selectedSubCategoryId = null }, modifier = Modifier.weight(1f))
                    TypeCard(emoji = "\u26F5", title = "Tekne", isSelected = selectedType == "BOAT", onClick = { selectedType = "BOAT"; selectedParentCategoryId = null; selectedSubCategoryId = null }, modifier = Modifier.weight(1f))
                }
            }
            if (selectedType != null) {
                item {
                    Text("Kategori", fontWeight = FontWeight.Bold, color = SeaWhite, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    if (parentCategories.isNotEmpty()) {
                        CategoryDropdownField(label = "Ana Kategori", options = parentCategories, selectedId = selectedParentCategoryId, onSelect = { selectedParentCategoryId = it; selectedSubCategoryId = null })
                        if (selectedParentCategoryId != null && subCategories.isNotEmpty()) { Spacer(Modifier.height(8.dp)); CategoryDropdownField(label = "Alt Kategori", options = subCategories, selectedId = selectedSubCategoryId, onSelect = { selectedSubCategoryId = it }) }
                    }
                }
                if (selectedSubCategoryId != null || parentCategories.isEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Medya", fontWeight = FontWeight.Bold, color = SeaWhite, fontSize = 16.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { multiplePhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, colors = ButtonDefaults.buttonColors(containerColor = NavyCard), modifier = Modifier.weight(1f)) { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Fotoğraf Ekle") }
                                Button(onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }, colors = ButtonDefaults.buttonColors(containerColor = NavyCard), modifier = Modifier.weight(1f)) { Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(if (videoUri == null) "Video Ekle" else "Video Seçildi") }
                            }
                            if (imageUris.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(imageUris) { uri ->
                                        Box { AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop); IconButton(onClick = { imageUris = imageUris - uri }, modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))) { Icon(Icons.Default.Close, contentDescription = "Kaldır", tint = Color.White, modifier = Modifier.size(12.dp)) } }
                                    }
                                }
                            }
                            if (editAd != null && editAd.images.isNotEmpty()) {
                                Text("Mevcut Fotoğraflar", color = SeaMuted, fontSize = 12.sp)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(editAd.images) { img ->
                                        AsyncImage(model = img.url, contentDescription = null, modifier = Modifier.size(60.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AvoriumTextField(value = title, onValueChange = { title = it }, label = "İlan Başlığı")
                            AvoriumTextField(value = description, onValueChange = { description = it }, label = "Açıklama", singleLine = false, minLines = 3)
                            AvoriumTextField(value = price, onValueChange = { price = it }, label = "Fiyat (TL)", keyboardType = KeyboardType.Number)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column { Text("Telefon Numaram Görünsün", color = SeaWhite, fontWeight = FontWeight.SemiBold); Text("Telefonunuzu görebilir", color = SeaMuted, fontSize = 11.sp) }
                                Switch(checked = showPhone, onCheckedChange = { showPhone = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = OceanBlue60))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Acil Satılsın", color = SeaWhite, fontWeight = FontWeight.SemiBold)
                                Switch(checked = isUrgent, onCheckedChange = { isUrgent = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFE53935)))
                            }
                        }
                    }
                    if (selectedType == "BOAT") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Tekne Detayları", fontWeight = FontWeight.Bold, color = SeaWhite, fontSize = 16.sp)
                                AvoriumTextField(value = make, onValueChange = { make = it }, label = "Marka")
                                AvoriumTextField(value = model, onValueChange = { model = it }, label = "Model")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AvoriumTextField(value = year, onValueChange = { year = it }, label = "Yıl", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                                    AvoriumTextField(value = length, onValueChange = { length = it }, label = "Boy (m)", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                                }
                                AvoriumTextField(value = hullMaterial, onValueChange = { hullMaterial = it }, label = "Gövde Malzemesi")
                                AvoriumTextField(value = enginePower, onValueChange = { enginePower = it }, label = "Motor Gücü (HP)", keyboardType = KeyboardType.Number)
                            }
                        }
                    } else if (selectedType == "FISHING_GEAR") {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Malzeme Detayları", fontWeight = FontWeight.Bold, color = SeaWhite, fontSize = 16.sp)
                                AvoriumTextField(value = gearBrand, onValueChange = { gearBrand = it }, label = "Marka")
                                AvoriumTextField(value = gearType, onValueChange = { gearType = it }, label = "Malzeme Türü")
                                Text("Durum", color = SeaMuted, fontSize = 13.sp)
                                val conds = listOf("NEW" to "Sıfır", "LIKE_NEW" to "Az Kullanılmış", "GOOD" to "İyi", "FAIR" to "Orta", "POOR" to "Kötü")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(conds) { (v, lbl) -> FilterChip(selected = condition == v, onClick = { condition = v }, label = { Text(lbl, fontSize = 11.sp) }) }
                                }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Konum (Opsiyonel)", color = SeaMuted, fontSize = 13.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = selectedCity, onValueChange = {}, modifier = Modifier.weight(1f).clickable { showCityDialog = true }, label = { Text("İl") }, readOnly = true, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.White, disabledLabelColor = SeaMuted, disabledBorderColor = NavyCard))
                                OutlinedTextField(value = selectedDistrict, onValueChange = {}, modifier = Modifier.weight(1f).clickable(enabled = selectedCity.isNotBlank()) { showDistrictDialog = true }, label = { Text("İlçe") }, readOnly = true, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.White, disabledLabelColor = SeaMuted, disabledBorderColor = NavyCard))
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val pr = price.toDoubleOrNull() ?: 0.0
                                    val boat = if (selectedType == "BOAT") BoatDetailsRequest(make, model, year.toIntOrNull() ?: 0, length.toDoubleOrNull() ?: 0.0, hullMaterial, enginePower.toIntOrNull() ?: 0) else null
                                    val gear = if (selectedType == "FISHING_GEAR") FishingGearDetailsRequest(condition, gearBrand, gearType) else null
                                    
                                    if (editAd != null) {
                                        onEditSubmit?.invoke(title, description, pr, isUrgent, selectedCity.ifBlank { null }, selectedDistrict.ifBlank { null }, selectedSubCategoryId ?: selectedParentCategoryId, selectedType, boat, gear, null, showPhone)
                                    } else {
                                        onSubmit(title, description, pr, selectedSubCategoryId ?: selectedParentCategoryId ?: "", selectedType!!, boat, gear, imageUris, videoUri, showPhone, isUrgent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue60),
                                enabled = !isLoading && title.isNotBlank() && description.isNotBlank() && price.isNotBlank()
                            ) { Text(if (isLoading) (if (editAd != null) "Güncelleniyor..." else "Gönderiliyor...") else (if (editAd != null) "Değişiklikleri Kaydet" else "İlan Ver"), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                            
                            if (onCancel != null) {
                                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                                    Text("İptal", color = SeaMuted)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showCityDialog) {
        var localSearch by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showCityDialog = false }, title = { Text("İl Seçin", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = localSearch, onValueChange = { localSearch = it }, placeholder = { Text("İl ara...") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        val filtered = TurkeyLocations.cities.filter { it.contains(localSearch, ignoreCase = true) }
                        items(filtered) { city ->
                            Text(text = city, modifier = Modifier.fillMaxWidth().clickable { selectedCity = city; selectedDistrict = ""; showCityDialog = false }.padding(vertical = 10.dp, horizontal = 4.dp), color = if (city == selectedCity) OceanBlue60 else Color.White)
                            HorizontalDivider(color = NavyCard)
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { showCityDialog = false }) { Text("Kapat") } })
    }

    if (showDistrictDialog && selectedCity.isNotBlank()) {
        var localSearch by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showDistrictDialog = false }, title = { Text("İlçe Seçin", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(value = localSearch, onValueChange = { localSearch = it }, placeholder = { Text("İlçe ara...") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        val filtered = availableDistricts.filter { it.contains(localSearch, ignoreCase = true) }
                        items(filtered) { district ->
                            Text(text = district, modifier = Modifier.fillMaxWidth().clickable { selectedDistrict = district; showDistrictDialog = false }.padding(vertical = 10.dp, horizontal = 4.dp), color = if (district == selectedDistrict) Teal60 else Color.White)
                            HorizontalDivider(color = NavyCard)
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { showDistrictDialog = false }) { Text("Kapat") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdownField(label: String, options: List<CategoryDto>, selectedId: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.find { it.id == selectedId }?.name ?: "Seçin..."
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = selectedText, onValueChange = {}, readOnly = true, label = { Text(label, color = SeaMuted) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = NavyContainer, unfocusedContainerColor = NavyContainer, focusedBorderColor = OceanBlue60, unfocusedBorderColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(NavyCard)) {
            options.sortedBy { it.name }.forEach { option -> DropdownMenuItem(text = { Text(option.name, color = if (option.id == selectedId) OceanBlue60 else SeaWhite) }, onClick = { onSelect(option.id); expanded = false }) }
        }
    }
}

@Composable
private fun TypeCard(emoji: String, title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(90.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = if (isSelected) OceanBlue60.copy(alpha = 0.3f) else NavyCard), shape = RoundedCornerShape(14.dp), border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, OceanBlue60) else null) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp); Spacer(Modifier.height(4.dp))
            Text(title, color = if (isSelected) OceanBlue60 else SeaWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AvoriumTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, singleLine: Boolean = true, minLines: Int = 1, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label, color = SeaMuted) }, singleLine = singleLine, minLines = minLines, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType), modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = NavyContainer, unfocusedContainerColor = NavyContainer, focusedBorderColor = OceanBlue60, unfocusedBorderColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
}
