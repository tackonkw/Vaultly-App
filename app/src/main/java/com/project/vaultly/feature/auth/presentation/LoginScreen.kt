package com.project.vaultly.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.vaultly.core.navigation.LocalBackStack
import com.project.vaultly.core.navigation.Routes
import com.project.vaultly.core.navigation.navigateAndClear
import com.project.vaultly.core.navigation.navigateTo

@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel()) {
    val backStack = LocalBackStack.current

    LoginContent(
        authViewModel = authViewModel,
        onLoginSuccess = { email ->
            backStack.navigateAndClear(Routes.HomeRoute(email = email))
        },
        onNavigateToRegister = {
            backStack.navigateTo(Routes.RegisterRoute)
        }
    )
}

@Composable
fun LoginContent(
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vaultly",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Personal Finance Tracker",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                    email = it
                    errorMessage = ""
                    authViewModel.clearError()
                },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                    password = it
                    errorMessage = ""
                    authViewModel.clearError()
                },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        val apiErrorMessage = authViewModel.errorMessage
        if (errorMessage.isNotEmpty() || apiErrorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage.ifEmpty { apiErrorMessage },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    email.isBlank() -> errorMessage = "Email tidak boleh kosong"
                    password.isBlank() -> errorMessage = "Password tidak boleh kosong"
                    else -> authViewModel.login(email, password, onLoginSuccess)
                }
            },
            enabled = !authViewModel.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (authViewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Belum punya akun? ")
            Text("Daftar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
