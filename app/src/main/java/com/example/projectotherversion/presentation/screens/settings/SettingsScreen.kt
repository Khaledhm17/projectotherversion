package com.example.projectotherversion.presentation.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.projectotherversion.presentation.components.CitySpinner
import com.example.projectotherversion.presentation.components.ProfessionSpinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(SettingsEvent.ProfileImageChanged(uri))
    }

    LaunchedEffect(state.isAccountDeleted) {
        if (state.isAccountDeleted) {
            focusManager.clearFocus()
            onAccountDeleted()
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            focusManager.clearFocus()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات الحساب") },
                navigationIcon = {
                    IconButton(onClick = {
                        focusManager.clearFocus()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding() // التعامل مع لوحة المفاتيح
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (state.profileImageUrl != null || state.profileImageUri != null) {
                    val painter = if (state.profileImageUri != null) {
                        rememberAsyncImagePainter(state.profileImageUri)
                    } else {
                        rememberAsyncImagePainter(state.profileImageUrl)
                    }
                    Image(
                        painter = painter,
                        contentDescription = "صورة الملف الشخصي",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "صورة افتراضية",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        tint = Color.Gray
                    )
                }

                FloatingActionButton(
                    onClick = { 
                        focusManager.clearFocus()
                        imagePicker.launch("image/*") 
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تغيير الصورة",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(SettingsEvent.NameChanged(it)) },
                label = { Text("الاسم") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "الولاية:",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Medium
            )
            CitySpinner(
                selectedCity = state.city,
                onCitySelected = { city -> 
                    focusManager.clearFocus()
                    viewModel.onEvent(SettingsEvent.CityChanged(city)) 
                }
            )

            if (state.user?.role == "CRAFTSMAN") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "الحرفة:",
                    modifier = Modifier.align(Alignment.Start),
                    fontWeight = FontWeight.Medium
                )
                ProfessionSpinner(
                    selectedProfession = state.profession,
                    onProfessionSelected = { profession ->
                        focusManager.clearFocus()
                        viewModel.onEvent(SettingsEvent.ProfessionChanged(profession))
                    }
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.onEvent(SettingsEvent.SaveClicked) 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("حفظ التغييرات")
                }
            }

            if (state.user?.role != "ADMIN") {
                TextButton(
                    onClick = { 
                        focusManager.clearFocus()
                        showDeleteDialog = true 
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("حذف الحساب", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("تأكيد الحذف", color = MaterialTheme.colorScheme.error) },
            text = { Text("هل أنت متأكد من رغبتك في حذف حسابك نهائياً؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onEvent(SettingsEvent.DeleteAccountClicked)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، احذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
