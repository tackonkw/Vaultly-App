package com.project.vaultly.feature.transaction.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.vaultly.core.navigation.LocalBackStack
import com.project.vaultly.core.navigation.Routes
import com.project.vaultly.core.navigation.navigateTo
import com.project.vaultly.feature.auth.data.UserRepository
import com.project.vaultly.theme.VaultlyColors
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTransactionScreen(
    transactionViewModel: TransactionViewModel = viewModel()
) {
    val backStack = LocalBackStack.current
    val transactions = transactionViewModel.transactions
    val email = UserRepository.getCurrentUserEmail().orEmpty()

    LaunchedEffect(email, backStack.size) {
        if (email.isNotEmpty()) {
            transactionViewModel.loadTransactionsForUser(email)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daftar Transaksi") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { backStack.navigateTo(Routes.CreateTransactionRoute()) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
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
            transactionViewModel.errorMessage.isNotEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = transactionViewModel.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { transactionViewModel.loadTransactionsForUser(email) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
            transactions.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada transaksi\nKlik + untuk menambah",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }
            else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(transactions) { transaction ->
                    ListItem(
                        headlineContent = {
                            Text(
                                transaction.title,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Text(transaction.category)
                        },
                        trailingContent = {
                            Text(
                                text = if (transaction.isExpense)
                                    "- ${formatRupiah(transaction.amount)}"
                                else
                                    "+ ${formatRupiah(transaction.amount)}",
                                color = if (transaction.isExpense)
                                    MaterialTheme.colorScheme.error
                                else
                                    VaultlyColors.Income,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.clickable {
                            backStack.navigateTo(Routes.DetailTransactionRoute(transaction.id, false))
                        }
                    )
                    HorizontalDivider()
                }
            }
            }
        }
    }
}

private fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(amount)
}
