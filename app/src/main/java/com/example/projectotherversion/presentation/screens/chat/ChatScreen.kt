package com.example.projectotherversion.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
    
    var showCreateContractDialog by remember { mutableStateOf(false) }
    var showPostCloseRatingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(otherUserId) {
        viewModel.initialize(otherUserId, otherUserName)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size)
        }
    }

    // نافذة إنشاء صفقة (للحرفي فقط)
    if (showCreateContractDialog) {
        CreateContractDialog(
            customerName = otherUserName,
            onDismiss = { showCreateContractDialog = false },
            onConfirm = { details, price ->
                viewModel.createContract(details, price)
                showCreateContractDialog = false
            }
        )
    }

    // نافذة التقييم بعد إغلاق الصفقة (للزبون)
    if (showPostCloseRatingDialog) {
        AlertDialog(
            onDismissRequest = { showPostCloseRatingDialog = false },
            title = { Text("تم إنهاء العمل") },
            text = { Text("لقد تم إغلاق الصفقة بنجاح. هل ترغب في تقييم الحرفي الآن؟") },
            confirmButton = {
                Button(onClick = { 
                    showPostCloseRatingDialog = false
                    onNavigateToRating(otherUserId, otherUserName)
                }) { Text("تقييم الآن") }
            },
            dismissButton = {
                TextButton(onClick = { showPostCloseRatingDialog = false }) { Text("لاحقاً") }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(otherUserName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        val activeContract = state.contracts.find { it.status == ContractStatus.ACCEPTED }
                        if (activeContract != null) {
                            Text("صفقة نشطة: ${activeContract.price} دج", fontSize = 11.sp, color = Color(0xFF4CAF50))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (state.currentUserRole == "CRAFTSMAN") {
                        IconButton(onClick = { showCreateContractDialog = true }) {
                            Icon(Icons.Default.Handshake, contentDescription = "إنشاء صفقة", tint = Color(0xFF1565C0))
                        }
                    }
                }
            )
        },
        bottomBar = {
            ChatBottomBar(
                message = state.currentMessage,
                onMessageChange = viewModel::onMessageChanged,
                onSend = viewModel::sendMessage
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // عرض الصفقة المعلقة أو النشطة في أعلى الدردشة بشكل بارز جداً للطرفين
            state.contracts.filter { it.status == ContractStatus.PENDING || it.status == ContractStatus.ACCEPTED }.forEach { contract ->
                ContractInteractiveCard(
                    contract = contract,
                    isCustomer = state.currentUserRole == "CLIENT",
                    onAccept = { viewModel.updateContractStatus(contract.id, ContractStatus.ACCEPTED) },
                    onReject = { viewModel.updateContractStatus(contract.id, ContractStatus.REJECTED) },
                    onClose = { 
                        viewModel.updateContractStatus(contract.id, ContractStatus.CLOSED)
                        if (state.currentUserRole == "CLIENT") showPostCloseRatingDialog = true
                    }
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages) { msg ->
                    MessageItem(message = msg, isMine = msg.senderId == state.currentUserId)
                }
            }
        }
    }
}

@Composable
fun ContractInteractiveCard(
    contract: Contract,
    isCustomer: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contract.status == ContractStatus.PENDING) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (contract.status == ContractStatus.ACCEPTED) Icons.Default.Handshake else Icons.Default.Assignment,
                    contentDescription = null,
                    tint = if (contract.status == ContractStatus.ACCEPTED) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (contract.status == ContractStatus.PENDING) "عرض صفقة عمل" else "صفقة قيد التنفيذ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(Modifier.height(8.dp))
            Text(text = contract.details, fontSize = 14.sp)
            Text(text = "المبلغ المتفق عليه: ${contract.price} دج", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1565C0), modifier = Modifier.padding(top = 4.dp))
            
            Spacer(Modifier.height(16.dp))

            if (contract.status == ContractStatus.PENDING) {
                if (isCustomer) {
                    // الخيارات المتاحة للزبون (وافق أو أرفض)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("موافقة")
                        }
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("رفض")
                        }
                    }
                } else {
                    Text("بانتظار رد الزبون...", fontSize = 12.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            } else if (contract.status == ContractStatus.ACCEPTED) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Text("إنهاء وإغلاق الصفقة (تم العمل)")
                }
            }
        }
    }
}

@Composable
fun CreateContractDialog(customerName: String, onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var details by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء عرض عمل جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إلى الزبون: $customerName", fontSize = 14.sp, color = Color.Gray)
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("ما هو العمل المطلوب؟") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.all { c -> c.isDigit() }) price = it },
                    label = { Text("المبلغ الإجمالي (دج)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(details, price.toDoubleOrNull() ?: 0.0) },
                enabled = details.isNotBlank() && price.isNotBlank()
            ) { Text("إرسال العرض") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun ChatBottomBar(message: String, onMessageChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("اكتب رسالتك...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(
                onClick = onSend,
                containerColor = Color(0xFF1565C0),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, isMine: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) Color(0xFFBBDEFB) else Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isMine) 12.dp else 2.dp,
                bottomEnd = if (isMine) 2.dp else 12.dp
            )
        ) {
            Text(text = message.content, modifier = Modifier.padding(10.dp), fontSize = 15.sp)
        }
    }
}
