package com.project.vaultly.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.project.vaultly.core.navigation.navigateBack
import com.project.vaultly.core.navigation.navigateTo

@Composable
fun RegisterScreen(authViewModel: AuthViewModel = viewModel()) {
    val backStack = LocalBackStack.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Buat Akun",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Daftar untuk mulai mengatur keuangan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = ""
                authViewModel.clearError()
            },
            label = { Text("Nama Lengkap") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = ""
                authViewModel.clearError()
            },
            label = { Text("Konfirmasi Password") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
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
                    name.isBlank() -> errorMessage = "Nama tidak boleh kosong"
                    email.isBlank() -> errorMessage = "Email tidak boleh kosong"
                    !email.contains("@") -> errorMessage = "Format email tidak valid"
                    password.length < 6 -> errorMessage = "Password minimal 6 karakter"
                    password != confirmPassword -> errorMessage = "Password tidak cocok"
                    else -> {
                        authViewModel.register(
                            fullName = name,
                            email = email,
                            password = password,
                            onSuccess = { showSuccessDialog = true }
                        )
                    }
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
                Text("Daftar", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { backStack.navigateTo(Routes.LoginRoute) }) {
            Text("Sudah punya akun? ")
            Text("Login", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Registrasi Berhasil") },
            text = { Text("Akun $email berhasil dibuat. Silakan login.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        backStack.navigateBack()
                    }
                ) {
                    Text("Login Sekarang")
                }
            }
        )
    }
}
