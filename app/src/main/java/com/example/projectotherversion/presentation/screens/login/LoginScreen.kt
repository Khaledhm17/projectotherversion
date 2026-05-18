package com.example.projectotherversion.presentation.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToVisitor: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            focusManager.clearFocus() // إغلاق التركيز قبل التنقل
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f)) // رفع المحتوى قليلاً للأعلى

        Text(
            text = "ARTISAN APP",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "تطبيق يربط بين الحرفيين والزبائن في مكان واحد ",
            fontSize = 16.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 15.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEvent(LoginEvent.EmailChanged(it)) },
            label = { Text("البريد الإلكتروني") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onEvent(LoginEvent.PasswordChanged(it)) },
            label = { Text("كلمة المرور") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                focusManager.clearFocus() // إغلاق لوحة المفاتيح عند النقر
                viewModel.onEvent(LoginEvent.LoginClicked) 
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !state.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White // جعل الخط باللون الأبيض ليتناسب ويوضح
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(
                    text = "تسجيل الدخول",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // تأكيد اللون الأبيض
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            focusManager.clearFocus()
            onNavigateToRegister()
        }) {
            Text("إنشاء حساب جديد", fontSize = 16.sp)
        }

        TextButton(onClick = {
            focusManager.clearFocus()
            onNavigateToVisitor()
        }) {
            Text("الدخول كزائر", fontSize = 16.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.weight(1f)) // موازنة المسافة في الأسفل
    }
}
