package com.example.projectotherversion.presentation.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectotherversion.domain.model.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    otherUserId: String,
    otherUserName: String,
    onNavigateBack: () -> Unit,
    onNavigateToRating: (String, String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var showFinishDealDialog by remember { mutableStateOf(false) }

    LaunchedEffect(otherUserId) {
        viewModel.initialize(otherUserId, otherUserName)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    if (showFinishDealDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDealDialog = false },
            title = { Text("إنهاء الصفقة", color = Color(0xFF1565C0)) },
            text = { Text("هل تم تقديم الخدمة بنجاح؟ يمكنك تقييم الحرفي الآن.") },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDealDialog = false
                        onNavigateToRating(otherUserId, otherUserName)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("تقييم الحرفي")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDealDialog = false }) {
                    Text("إلغاء", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(otherUserName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    // زر "انتهت الصفقة" يظهر فقط للزبون (إذا كان يتحدث مع حرفي)
                    // للتبسيط، سنظهره دائماً حالياً في صفحة المحادثة
                    TextButton(onClick = { showFinishDealDialog = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("انتهت الصفقة", color = Color(0xFF1565C0))
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.currentMessage,
                        onValueChange = { viewModel.onMessageChanged(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("اكتب رسالتك...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = { viewModel.sendMessage() },
                        containerColor = Color(0xFF1565C0),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(state.messages) { msg ->
                MessageItem(message = msg, isMine = msg.senderId == state.currentUserId)
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, isMine: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) Color(0xFFBBDEFB) else Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isMine) 12.dp else 0.dp,
                bottomEnd = if (isMine) 0.dp else 12.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}
