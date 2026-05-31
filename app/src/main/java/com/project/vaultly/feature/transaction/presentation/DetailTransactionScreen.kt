package com.project.vaultly.feature.transaction.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.vaultly.core.navigation.LocalBackStack
import com.project.vaultly.core.navigation.Routes
import com.project.vaultly.core.navigation.navigateBack
import com.project.vaultly.core.navigation.navigateTo
import com.project.vaultly.theme.VaultlyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTransactionScreen(
    transactionId: String,
    isFromDeficit: Boolean,
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val backStack = LocalBackStack.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val transaction = transactionViewModel.getTransactionById(transactionId)

    LaunchedEffect(transactionId) {
        transactionViewModel.clearError()
        transactionViewModel.loadTransaction(transactionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Transaksi") },
                navigationIcon = {
                    IconButton(onClick = { backStack.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            transactionViewModel.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            transaction == null -> {
            // Jika transaksi tidak ditemukan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = transactionViewModel.errorMessage.ifEmpty { "Transaksi tidak ditemukan" },
                        color = if (transactionViewModel.errorMessage.isNotEmpty()) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { backStack.navigateBack() }) {
                        Text("Kembali")
                    }
                }
            }
            }
            else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card Informasi Transaksi
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (transaction.isExpense) "Pengeluaran" else "Pemasukan",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (transaction.isExpense) MaterialTheme.colorScheme.error else VaultlyColors.Income
                        )
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatRupiah(transaction.amount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Divider()
                        Text("Kategori: ${transaction.category}", style = MaterialTheme.typography.bodyMedium)
                        Text("ID: ${transaction.id}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (isFromDeficit) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = "Peringatan: Transaksi ini berkontribusi pada defisit",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            backStack.navigateTo(Routes.CreateTransactionRoute(transaction.id))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Transaksi")
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hapus Transaksi")
                    }
                }
            }
            }
        }
    }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Transaksi") },
            text = { Text("Apakah Anda yakin ingin menghapus transaksi \"${transaction?.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionViewModel.deleteTransaction(
                            transactionId = transactionId,
                            onSuccess = {
                                showDeleteDialog = false
                                backStack.navigateBack()
                            }
                        )
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

private fun formatRupiah(amount: Double): String {
    return "Rp ${String.format("%,.0f", amount).replace(',', '.')}"
}
