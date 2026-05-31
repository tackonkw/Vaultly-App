package com.project.vaultly.feature.transaction.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.vaultly.core.navigation.LocalBackStack
import com.project.vaultly.core.navigation.navigateBack
import com.project.vaultly.theme.VaultlyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionScreen(
    transactionId: String? = null,
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val backStack = LocalBackStack.current
    val existingTransaction = transactionId?.let(transactionViewModel::getTransactionById)
    val isEditMode = transactionId != null

    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var isExpense by rememberSaveable { mutableStateOf(true) }
    var transactionAt by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) } // Untuk dropdown

    val categories = listOf("Makanan", "Transportasi", "Belanja", "Hiburan", "Kesehatan", "Pendapatan", "Lainnya")

    LaunchedEffect(transactionId) {
        transactionViewModel.clearError()
        if (transactionId != null) {
            transactionViewModel.loadTransaction(transactionId)
        }
    }

    LaunchedEffect(existingTransaction?.id) {
        existingTransaction?.let { transaction ->
            title = transaction.title
            amount = transaction.amount
                .takeIf { it > 0 }
                ?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                .orEmpty()
            category = transaction.category
            isExpense = transaction.isExpense
            transactionAt = transaction.transactionAt
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Transaksi" else "Tambah Transaksi") },
                navigationIcon = {
                    IconButton(onClick = { backStack.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isEditMode && transactionViewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (isEditMode && existingTransaction == null && transactionViewModel.errorMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(transactionViewModel.errorMessage, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { backStack.navigateBack() }) {
                        Text("Kembali")
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tipe transaksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = !isExpense,
                    onClick = { isExpense = false },
                    label = { Text("Pemasukan") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VaultlyColors.AccentSoft,
                        selectedLabelColor = VaultlyColors.Income
                    )
                )
                FilterChip(
                    selected = isExpense,
                    onClick = { isExpense = true },
                    label = { Text("Pengeluaran") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.error
                    )
                )
            }

            // Judul transaksi
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = ""
                },
                label = { Text("Judul Transaksi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Nominal
            OutlinedTextField(
                value = amount,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = it
                        errorMessage = ""
                    }
                },
                label = { Text("Nominal") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = { Text("Rp ") }
            )

            // Kategori dengan Dropdown yang berfungsi
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    placeholder = { Text("Pilih kategori") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expanded = false
                                errorMessage = ""
                            }
                        )
                    }
                }
            }

            // Error message
            val apiErrorMessage = transactionViewModel.errorMessage
            if (errorMessage.isNotEmpty() || apiErrorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage.ifEmpty { apiErrorMessage },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol simpan
            Button(
                onClick = {
                    when {
                        title.isBlank() -> errorMessage = "Judul transaksi harus diisi"
                        amount.isBlank() -> errorMessage = "Nominal harus diisi"
                        category.isBlank() -> errorMessage = "Kategori harus dipilih"
                        else -> {
                            val amountValue = amount.replace(",", ".").toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                errorMessage = "Nominal tidak valid"
                            } else {
                                if (isEditMode) {
                                    transactionViewModel.updateTransaction(
                                        transactionId = transactionId.orEmpty(),
                                        title = title,
                                        amount = amountValue,
                                        category = category,
                                        isExpense = isExpense,
                                        transactionAt = transactionAt,
                                        onSuccess = { backStack.navigateBack() }
                                    )
                                } else {
                                    transactionViewModel.addTransaction(
                                        title = title,
                                        amount = amountValue,
                                        category = category,
                                        isExpense = isExpense,
                                        onSuccess = { backStack.navigateBack() }
                                    )
                                }
                            }
                        }
                    }
                },
                enabled = !transactionViewModel.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (transactionViewModel.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Simpan Perubahan" else "Simpan Transaksi")
                }
            }
        }
    }
}
