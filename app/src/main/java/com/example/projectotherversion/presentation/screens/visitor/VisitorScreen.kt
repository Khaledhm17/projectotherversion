package com.example.projectotherversion.presentation.screens.visitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.projectotherversion.domain.model.Post
import com.example.projectotherversion.presentation.components.CitySpinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorScreen(
    onNavigateBack: () -> Unit,
    onLoginRequest: () -> Unit = {}
) {
    var selectedCity by remember { mutableStateOf("الوادي") }
    // في التطبيق الحقيقي، يجب جلب المنشورات من ViewModel مخصص للزائر
    val posts = remember { emptyList<Post>() } // Placeholder

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تصفح كزائر") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            CitySpinner(
                selectedCity = selectedCity,
                onCitySelected = { selectedCity = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد منشورات في هذه المدينة حالياً")
                }
            } else {
                LazyColumn {
                    items(posts.filter { it.city == selectedCity && it.type == "SERVICE" }) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column {
                                post.imageUrl?.let { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(post.authorName, fontWeight = FontWeight.Bold)
                                        Text(post.description)
                                    }
                                    Button(onClick = onLoginRequest) {
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