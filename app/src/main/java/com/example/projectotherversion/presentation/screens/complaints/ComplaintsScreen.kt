package com.example.projectotherversion.presentation.screens.complaints

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectotherversion.domain.model.Complaint
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintsScreen(
    viewModel: ComplaintsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            focusManager.clearFocus()
            selectedTab = 0
            viewModel.onEvent(ComplaintsEvent.ClearForm)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الشكاوى") },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding() // ضمان ارتفاع المحتوى مع لوحة المفاتيح
        ) {
            if (state.currentUser?.role == "ADMIN") {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.complaints) { complaint ->
                        ComplaintCard(
                            complaint = complaint,
                            isAdmin = true,
                            onDelete = { viewModel.onEvent(ComplaintsEvent.DeleteComplaint(complaint.id)) }
                        )
                    }
                }
            } else {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { 
                            focusManager.clearFocus()
                            selectedTab = 0 
                        },
                        text = { Text("الشكاوى السابقة") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { 
                            focusManager.clearFocus()
                            selectedTab = 1 
                        },
                        text = { Text("تقديم شكوى جديدة") }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        val myComplaints = state.complaints.filter { it.senderId == state.currentUser?.id }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(myComplaints) { complaint ->
                                ComplaintCard(
                                    complaint = complaint,
                                    isAdmin = false,
                                    onDelete = {}
                                )
                            }
                        }
                    }
                    1 -> {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = state.subject,
                                onValueChange = { viewModel.onEvent(ComplaintsEvent.SubjectChanged(it)) },
                                label = { Text("الموضوع") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.message,
                                onValueChange = { viewModel.onEvent(ComplaintsEvent.MessageChanged(it)) },
                                label = { Text("تفاصيل الشكوى") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                            if (state.error != null) {
                                Text(
                                    text = state.error!!,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { 
                                    focusManager.clearFocus()
                                    viewModel.onEvent(ComplaintsEvent.SubmitClicked) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isLoading && state.subject.isNotBlank() && state.message.isNotBlank()
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("إرسال الشكوى")
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
fun ComplaintCard(
    complaint: Complaint,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "من: ${complaint.senderName}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(complaint.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الموضوع: ${complaint.subject}",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = complaint.message,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (complaint.adminReply != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "رد الإدارة:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = complaint.adminReply,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("تم الحل")
                    }
                }
            }
        }
    }
}
