package com.example.projectotherversion.presentation.screens.rating

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    viewModel: RatingViewModel,
    artisanId: String,
    artisanName: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقييم الحرفي") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "كيف كانت تجربتك مع",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Text(
                text = artisanName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // نجوم التقييم
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= state.rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { viewModel.onRatingChanged(i) },
                        tint = if (i <= state.rating) Color(0xFFFFC107) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { viewModel.submitRating(artisanId) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = state.rating > 0 && !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("إرسال التقييم", fontSize = 18.sp)
                }
            }
        }
    }
}
