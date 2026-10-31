package com.example.avorium.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.avorium.data.AdDto
import com.example.avorium.data.CategoryDto
import com.example.avorium.data.TurkeyLocations
import com.example.avorium.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    ads: List<AdDto>,
    selectedCategory: String,
    selectedCategoryId: String? = null,
    selectedParentCategoryId: String? = null,
    selectedCity: String? = null,
    selectedDistrict: String? = null,
    categories: List<CategoryDto> = emptyList(),
    search: String,
    onCategorySelect: (String) -> Unit,
    onParentCategorySelect: ((String?) -> Unit)? = null,
    onCategoryIdSelect: ((String?) -> Unit)? = null,
    onCitySelect: ((String?) -> Unit)? = null,
    onDistrictSelect: ((String?) -> Unit)? = null,
    onSearchChange: (String) -> Unit,
    onAdClick: (AdDto) -> Unit
) {
    var showCityDialog by remember { mutableStateOf(false) }
    var showDistrictDialog by remember { mutableStateOf(false) }
    var citySearch by remember { mutableStateOf("") }
    var districtSearch by remember { mutableStateOf("") }
    val availableDistricts = if (selectedCity != null) TurkeyLocations.getDistricts(selectedCity) else emptyList()

    Column(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(NavyCard, DeepNavy)))
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "AVORIUM",
                    fontWeight = FontWeight.ExtraBold,
                    color = SeaWhite,
                    fontSize = 36.sp,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Denizin Kalbi, Balıkçının Dünyası",
                    color = OceanBlue60,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CategoryCard(
                        emoji = "\uD83C\uDFA3",
                        title = "Balık Av",
                        subtitle = "Malzemeleri",
                        isSelected = selectedCategory == "FISHING_GEAR",
                        gradientColors = listOf(Color(0xFF1B5E20), Color(0xFF004D40)),
                        onClick = { onCategorySelect("FISHING_GEAR") },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCard(
                        emoji = "\u26F5",
                        title = "Tekne",
                        subtitle = "Tekneler & Aksesuar",
                        isSelected = selectedCategory == "BOAT",
                        gradientColors = listOf(Color(0xFF0D47A1), Color(0xFF01579B)),
                        onClick = { onCategorySelect("BOAT") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val parentCats = categories.filter { it.type == selectedCategory && it.parentId == null }.sortedBy { it.name }
            val subCats = categories.filter { it.parentId == selectedParentCategoryId }.sortedBy { it.name }
            if (parentCats.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryDropdown(
                            label = "Ana Kategori",
                            options = parentCats,
                            selectedId = selectedParentCategoryId,
                            onSelect = { id -> onParentCategorySelect?.invoke(id); onCategoryIdSelect?.invoke(null) },
                            placeholder = "Tümü"
                        )
                        if (selectedParentCategoryId != null && subCats.isNotEmpty()) {
                            CategoryDropdown(
                                label = "Alt Kategori",
                                options = subCats,
                                selectedId = selectedCategoryId,
                                onSelect = { id -> onCategoryIdSelect?.invoke(id) },
                                placeholder = "Tümü"
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("İlan ara...", color = SeaMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SeaMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavyContainer, unfocusedContainerColor = NavyContainer,
                        focusedBorderColor = OceanBlue60, unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                    )
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (selectedCity != null) OceanBlue60.copy(alpha = 0.2f) else NavyCard)
                            .border(1.dp, if (selectedCity != null) OceanBlue60 else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { showCityDialog = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (selectedCity != null) OceanBlue60 else SeaMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(selectedCity ?: "İl Seç", color = if (selectedCity != null) OceanBlue60 else SeaMuted, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SeaMuted, modifier = Modifier.size(16.dp))
                    }
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(if (selectedDistrict != null) Teal60.copy(alpha = 0.2f) else NavyCard)
                            .border(1.dp, if (selectedDistrict != null) Teal60 else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable(enabled = selectedCity != null && availableDistricts.isNotEmpty()) { showDistrictDialog = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (selectedDistrict != null) Teal60 else SeaMuted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(selectedDistrict ?: "İlçe Seç", color = if (selectedDistrict != null) Teal60 else SeaMuted, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SeaMuted, modifier = Modifier.size(16.dp))
                    }
                }
                if (selectedCity != null || selectedDistrict != null) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { onCitySelect?.invoke(null); onDistrictSelect?.invoke(null) }, contentPadding = PaddingValues(horizontal = 0.dp)) {
                        Text("Konum filtresini temizle", color = SeaMuted, fontSize = 12.sp)
                    }
                }
            }

            item {
                Text(text = "${ads.size} ilan bulundu", color = SeaMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
            }

            if (ads.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(NavyCard), contentAlignment = Alignment.Center) {
                        Text("Bu kriterlerde ilan bulunamadı.", color = SeaMuted)
                    }
                }
            }

            items(ads, key = { it.id }) { ad -> AdCard(ad = ad, onClick = { onAdClick(ad) }) }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false; citySearch = "" },
            title = { Text("İl Seçin", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = citySearch, onValueChange = { citySearch = it },
                        placeholder = { Text("İl ara...") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    val filtered = TurkeyLocations.cities.filter { it.contains(citySearch, ignoreCase = true) }
                    LazyColumn(state = rememberLazyListState(), modifier = Modifier.heightIn(max = 320.dp)) {
                        items(filtered) { city ->
                            Text(
                                text = city,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { onCitySelect?.invoke(city); onDistrictSelect?.invoke(null); showCityDialog = false; citySearch = "" }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                color = if (city == selectedCity) OceanBlue60 else Color.White,
                                fontWeight = if (city == selectedCity) FontWeight.Bold else FontWeight.Normal
                            )
                            HorizontalDivider(color = NavyCard)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCityDialog = false; citySearch = "" }) { Text("Kapat") } }
        )
    }

    if (showDistrictDialog && selectedCity != null) {
        AlertDialog(
            onDismissRequest = { showDistrictDialog = false; districtSearch = "" },
            title = { Text("İlçe Seçin - $selectedCity", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = districtSearch, onValueChange = { districtSearch = it },
                        placeholder = { Text("İlçe ara...") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    val filtered = availableDistricts.filter { it.contains(districtSearch, ignoreCase = true) }
                    LazyColumn(state = rememberLazyListState(), modifier = Modifier.heightIn(max = 320.dp)) {
                        items(filtered) { district ->
                            Text(
                                text = district,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { onDistrictSelect?.invoke(district); showDistrictDialog = false; districtSearch = "" }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                color = if (district == selectedDistrict) Teal60 else Color.White,
                                fontWeight = if (district == selectedDistrict) FontWeight.Bold else FontWeight.Normal
                            )
                            HorizontalDivider(color = NavyCard)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDistrictDialog = false; districtSearch = "" }) { Text("Kapat") } }
        )
    }
}

@Composable
private fun CategoryCard(emoji: String, title: String, subtitle: String, isSelected: Boolean, gradientColors: List<Color>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor = if (isSelected) GoldenAmber else Color.Transparent
    Card(
        modifier = modifier.height(100.dp).clip(RoundedCornerShape(16.dp)).border(2.dp, borderColor, RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(if (isSelected) gradientColors else gradientColors.map { it.copy(alpha = 0.5f) })).padding(16.dp)) {
            Column {
                Text(emoji, fontSize = 28.sp)
                Spacer(Modifier.height(4.dp))
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AdCard(ad: AdDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = NavyElevated),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            val imageUrl = ad.images.firstOrNull()?.url
            if (imageUrl != null) {
                AsyncImage(model = imageUrl, contentDescription = ad.title, modifier = Modifier.width(120.dp).fillMaxHeight().clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)), contentScale = ContentScale.Crop)
            } else {
                Box(modifier = Modifier.width(120.dp).fillMaxHeight().background(NavyContainer, RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)), contentAlignment = Alignment.Center) {
                    Text(text = if (ad.category?.type == "BOAT") "\u26F5" else "\uD83C\uDFA3", fontSize = 32.sp)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                if (ad.isUrgent) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE53935).copy(alpha = 0.2f)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                        Text("ACİL", color = Color(0xFFE53935), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(text = ad.title, fontWeight = FontWeight.Bold, color = SeaWhite, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                if (ad.city != null || ad.district != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SeaMuted, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(text = listOfNotNull(ad.city, ad.district).joinToString(", "), color = SeaMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(2.dp))
                }
                Text(text = ad.description, color = SeaMuted, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = ad.category?.name ?: "", color = OceanBlue60, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(OceanBlue60.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp))
                    Text(text = ad.price.toInt().toString() + " TL", fontWeight = FontWeight.ExtraBold, color = GoldenAmber, fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(label: String, options: List<CategoryDto>, selectedId: String?, onSelect: (String?) -> Unit, placeholder: String = "Tümü") {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.find { it.id == selectedId }?.name ?: placeholder
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedText, onValueChange = {}, readOnly = true,
            label = { Text(label, color = SeaMuted) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavyContainer, unfocusedContainerColor = NavyContainer,
                focusedBorderColor = OceanBlue60, unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(NavyCard)) {
            DropdownMenuItem(text = { Text(placeholder, color = SeaMuted) }, onClick = { onSelect(null); expanded = false })
            options.sortedBy { it.name }.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name, color = if (option.id == selectedId) OceanBlue60 else SeaWhite) },
                    onClick = { onSelect(option.id); expanded = false }
                )
            }
        }
    }
}
