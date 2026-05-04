package com.example.projectotherversion.presentation.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectotherversion.presentation.components.CitySpinner
import com.example.projectotherversion.presentation.components.ProfessionSpinner

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            focusManager.clearFocus()
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // التعامل مع لوحة المفاتيح
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "إنشاء حساب جديد",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { viewModel.onEvent(RegisterEvent.NameChanged(it)) },
            label = { Text("الاسم الكامل") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEvent(RegisterEvent.EmailChanged(it)) },
            label = { Text("البريد الإلكتروني") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onEvent(RegisterEvent.PasswordChanged(it)) },
            label = { Text("كلمة المرور") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        CitySpinner(
            selectedCity = state.city,
            onCitySelected = { viewModel.onEvent(RegisterEvent.CityChanged(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = state.role == "CLIENT",
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.onEvent(RegisterEvent.RoleChanged("CLIENT")) 
                }
            )
            Text("زبون", modifier = Modifier.padding(end = 16.dp))

            RadioButton(
                selected = state.role == "CRAFTSMAN",
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.onEvent(RegisterEvent.RoleChanged("CRAFTSMAN")) 
                }
            )
            Text("حرفي")
        }

        if (state.role == "CRAFTSMAN") {
            Spacer(modifier = Modifier.height(8.dp))
            ProfessionSpinner(
                selectedProfession = state.profession,
                onProfessionSelected = { viewModel.onEvent(RegisterEvent.ProfessionChanged(it)) }
            )
        }

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
                viewModel.onEvent(RegisterEvent.RegisterClicked) 
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("تسجيل")
            }
        }

        TextButton(onClick = {
            focusManager.clearFocus()
            onNavigateBack()
        }) {
            Text("لديك حساب بالفعل؟ تسجيل الدخول")
        }
    }
}
