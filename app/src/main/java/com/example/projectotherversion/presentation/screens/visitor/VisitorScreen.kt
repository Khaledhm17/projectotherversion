package com.example.projectotherversion.presentation.screens.visitor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.presentation.components.CitySpinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorScreen(
    viewModel: VisitorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onLoginRequest: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var selectedCity by remember { mutableStateOf("الوادي") }
    var selectedType by remember { mutableStateOf("ALL") } // ALL, SERVICE, REQUEST
    var showLoginDialog by remember { mutableStateOf(false) }

    // نافذة التنبيه عند محاولة التواصل
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("تسجيل الدخول مطلوب", color = Color(0xFF1565C0)) },
            text = { Text("عذراً، يجب عليك تسجيل الدخول لتتمكن من التواصل مع أصحاب المنشورات.") },
            confirmButton = {
                TextButton(onClick = {
                    showLoginDialog = false
                    onLoginRequest()
                }) {
                    Text("تسجيل الدخول", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("إلغاء", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تصفح المنشورات", color = Color(0xFF1565C0)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // فلتر المدينة
            CitySpinner(
                selectedCity = selectedCity,
                onCitySelected = { selectedCity = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // أزرار فلترة نوع المنشور (حرفي / زبون)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == "ALL",
                    onClick = { selectedType = "ALL" },
                    label = { Text("الكل") }
                )
                FilterChip(
                    selected = selectedType == "SERVICE",
                    onClick = { selectedType = "SERVICE" },
                    label = { Text("خدمات حرفيين") }
                )
                FilterChip(
                    selected = selectedType == "REQUEST",
                    onClick = { selectedType = "REQUEST" },
                    label = { Text("طلبات زبائن") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredPosts = state.posts.filter {
                it.city == selectedCity && (selectedType == "ALL" || it.type == selectedType)
            }

            if (filteredPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("لا توجد منشورات متوفرة حالياً", color = Color.Black)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredPosts) { post ->
                        // استخدام نفس لون صناديق الشكاوى الذي اتفقنا عليه [photo_2026-05-05_01-53-57.jpg]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE2DCE6)),
                            border = BorderStroke(1.dp, Color(0xFF757575)) // إطار رمادي واضح
                        ) {
                            Column {
                                post.imageUrl?.let { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(160.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = post.authorName,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = post.description,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.Black
                                            )
                                            // عرض نوع المنشور (حرفة أو طلب)
                                            Text(
                                                text = if (post.type == "SERVICE") "خدمة حرفية" else "طلب عمل",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFF1565C0)
                                            )
                                        }

                                        Button(
                                            onClick = { showLoginDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                                        ) {
                                            Text("تواصل")
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
