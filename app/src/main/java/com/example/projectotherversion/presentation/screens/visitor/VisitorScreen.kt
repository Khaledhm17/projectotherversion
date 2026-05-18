package com.example.projectotherversion.presentation.screens.visitor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.domain.model.User
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
    
    var selectedUserForDetails by remember { mutableStateOf<User?>(null) }

    // نافذة عرض تفاصيل الحرفي
    selectedUserForDetails?.let { user ->
        UserDetailDialog(user = user, onDismiss = { selectedUserForDetails = null })
    }

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
                        val author = state.users.find { it.id == post.authorId }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE2DCE6)),
                            border = BorderStroke(1.dp, Color(0xFF757575))
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = post.authorName,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (post.type == "SERVICE" && author != null) {
                                                    Spacer(Modifier.width(8.dp))
                                                    RatingStars(author.totalRating, author.ratingCount)
                                                }
                                            }
                                            
                                            Text(
                                                text = post.description,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.Black
                                            )
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (post.type == "SERVICE") "خدمة حرفية" else "طلب عمل",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color(0xFF1565C0)
                                                )
                                                if (post.type == "SERVICE" && author != null) {
                                                    Spacer(Modifier.width(8.dp))
                                                    TextButton(
                                                        onClick = { selectedUserForDetails = author },
                                                        contentPadding = PaddingValues(0.dp),
                                                        modifier = Modifier.height(30.dp)
                                                    ) {
                                                        Text("التفاصيل", fontSize = 12.sp, color = Color(0xFF1565C0))
                                                    }
                                                }
                                            }
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

@Composable
fun RatingStars(total: Double, count: Int) {
    val avg = if (count > 0) total / count else 0.0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
        Text(String.format("%.1f", avg), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(" ($count)", fontSize = 10.sp, color = Color.DarkGray)
    }
}

@Composable
fun UserDetailDialog(user: User, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تفاصيل الحرفي", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (!user.profileImage.isNullOrBlank()) {
                    AsyncImage(
                        model = user.profileImage,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(80.dp), tint = Color.Gray)
                }
                Spacer(Modifier.height(16.dp))
                Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(user.profession ?: "حرفي", color = Color.DarkGray)
                Spacer(Modifier.height(8.dp))
                RatingStars(user.totalRating, user.ratingCount)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(user.city, color = Color.Black)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("إغلاق")
            }
        },
        containerColor = Color.White
    )
}
