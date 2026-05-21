package com.example.projectotherversion.presentation.screens.contracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectotherversion.domain.model.Contract
import com.example.projectotherversion.domain.model.ContractStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractsScreen(
    viewModel: ContractsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("كل صفقاتي", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else if (state.contracts.isEmpty()) {
                Text(text = "لا توجد صفقات لعرضها حالياً", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.contracts) { contract ->
                        ContractItem(contract = contract)
                    }
                }
            }
        }
    }
}

@Composable
fun ContractItem(contract: Contract) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = when(contract.status) {
                ContractStatus.PENDING -> Color(0xFFFFF3E0)
                ContractStatus.ACCEPTED -> Color(0xFFE8F5E9)
                ContractStatus.CLOSED -> Color(0xFFF5F5F5)
                ContractStatus.REJECTED -> Color(0xFFFFEBEE)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (contract.status == ContractStatus.ACCEPTED) Icons.Default.Handshake else Icons.Default.Assignment,
                    contentDescription = null,
                    tint = when(contract.status) {
                        ContractStatus.ACCEPTED -> Color(0xFF4CAF50)
                        ContractStatus.PENDING -> Color(0xFFFF9800)
                        ContractStatus.REJECTED -> Color.Red
                        ContractStatus.CLOSED -> Color.Gray
                    }
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    val statusAr = when(contract.status) {
                        ContractStatus.PENDING -> "طلب معلق"
                        ContractStatus.ACCEPTED -> "صفقة جارية"
                        ContractStatus.REJECTED -> "مرفوضة"
                        ContractStatus.CLOSED -> "منتهية"
                    }
                    Text(text = statusAr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "مع: ${contract.artisanName} / ${contract.customerName}", fontSize = 12.sp, color = Color.Gray)
                }
                Text(text = "${contract.price} دج", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
            Spacer(Modifier.height(8.dp))
            Text(text = contract.details, fontSize = 14.sp)
        }
    }
}
