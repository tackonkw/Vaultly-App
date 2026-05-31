package com.project.vaultly.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.vaultly.core.navigation.LocalBackStack
import com.project.vaultly.core.navigation.Routes
import com.project.vaultly.core.navigation.navigateAndClear
import com.project.vaultly.core.navigation.navigateTo
import com.project.vaultly.feature.transaction.presentation.TransactionViewModel
import com.project.vaultly.feature.auth.data.UserRepository
import com.project.vaultly.theme.VaultlyColors
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    email: String = UserRepository.getCurrentUserEmail() ?: ""
) {
    val backStack = LocalBackStack.current
    var showExitDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeficitPopup by remember { mutableStateOf(false) }
    var deficitAmount by remember { mutableStateOf(0.0) }

    val transactionViewModel: TransactionViewModel = viewModel()

    // Load transaksi untuk user ini
    LaunchedEffect(email, backStack.size) {
        if (email.isNotEmpty()) {
            transactionViewModel.loadTransactionsForUser(email)
        }
    }

    val transactions = transactionViewModel.transactions
    val balance = transactionViewModel.getBalance()
    val totalIncome = transactionViewModel.getTotalIncome()
    val totalExpense = transactionViewModel.getTotalExpense()

    val recentTransactions = if (transactions.size > 5) transactions.take(5) else transactions

    // Cek defisit bulan ini
    LaunchedEffect(transactions.size) {
        val (isDeficit, amount) = checkMonthlyDeficit(transactions)
        if (isDeficit && !showDeficitPopup) {
            deficitAmount = amount
            showDeficitPopup = true
        }
    }

    // Navigasi otomatis ke halaman DeficitAlert saat showDeficitPopup true
    LaunchedEffect(showDeficitPopup) {
        if (showDeficitPopup) {
            backStack.navigateTo(Routes.DeficitAlertRoute(deficitAmount = deficitAmount))
            showDeficitPopup = false
        }
    }

    BackHandler { showExitDialog = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vaultly") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { backStack.navigateTo(Routes.CreateTransactionRoute()) }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header dengan nama user
            if (transactionViewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (transactionViewModel.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = transactionViewModel.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { transactionViewModel.loadTransactionsForUser(email) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }

            // Header dengan nama user
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Selamat datang,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = UserRepository.getUserName(email) ?: "Vaultly User",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Saldo card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Saldo",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatRupiah(balance),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pemasukan", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                            Text(formatRupiah(totalIncome), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        VerticalDivider(modifier = Modifier.height(32.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pengeluaran", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                            Text(formatRupiah(totalExpense), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "Transaksi",
                    onClick = { backStack.navigateTo(Routes.ListTransactionRoute) }
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Budget",
                    onClick = { backStack.navigateTo(Routes.BudgetPlanningRoute) }
                )
                MenuCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BarChart,
                    label = "Laporan",
                    onClick = { backStack.navigateTo(Routes.ReportRoute) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Transaksi terakhir
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Transaksi Terakhir", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { backStack.navigateTo(Routes.ListTransactionRoute) }) {
                    Text("Lihat semua")
                }
            }

            if (recentTransactions.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada transaksi\nKlik + untuk menambah",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column {
                        recentTransactions.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        backStack.navigateTo(Routes.DetailTransactionRoute(item.id, false))
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (item.isExpense) MaterialTheme.colorScheme.error else VaultlyColors.Income
                                    )
                                    Column {
                                        Text(
                                            item.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            item.category,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = if (item.isExpense) "- ${formatRupiah(item.amount)}" else "+ ${formatRupiah(item.amount)}",
                                    color = if (item.isExpense) MaterialTheme.colorScheme.error else VaultlyColors.Income,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (index < recentTransactions.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Dialog Exit
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Keluar Aplikasi") },
            text = { Text("Apakah Anda yakin ingin keluar?") },
            confirmButton = {
                TextButton(onClick = { android.os.Process.killProcess(android.os.Process.myPid()) }) {
                    Text("Ya", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tidak")
                }
            }
        )
    }

    // Dialog Logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Apakah Anda yakin ingin logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        UserRepository.logout()
                        showLogoutDialog = false
                        backStack.navigateAndClear(Routes.LoginRoute)
                    }
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun MenuCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

// Fungsi untuk cek defisit bulan ini
private fun checkMonthlyDeficit(transactions: List<com.project.vaultly.feature.transaction.presentation.Transaction>): Pair<Boolean, Double> {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    var monthlyIncome = 0.0
    var monthlyExpense = 0.0

    transactions.forEach { transaction ->
        val transDate = parseTransactionDate(transaction)
        val transCalendar = Calendar.getInstance().apply { time = transDate }
        val transMonth = transCalendar.get(Calendar.MONTH)
        val transYear = transCalendar.get(Calendar.YEAR)

        if (transYear == currentYear && transMonth == currentMonth) {
            if (transaction.isExpense) {
                monthlyExpense += transaction.amount
            } else {
                monthlyIncome += transaction.amount
            }
        }
    }

    val deficit = monthlyExpense - monthlyIncome
    return Pair(deficit > 0, deficit)
}

private fun formatRupiah(amount: Double): String {
    return "Rp ${String.format("%,.0f", amount).replace(',', '.')}"
}

private fun parseTransactionDate(transaction: com.project.vaultly.feature.transaction.presentation.Transaction): Date {
    if (transaction.transactionAt.isNotBlank()) {
        val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd HH:mm:ss")
        patterns.forEach { pattern ->
            val parser = java.text.SimpleDateFormat(pattern, Locale.US).apply {
                if (pattern.contains("'Z'")) {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
            }
            runCatching { parser.parse(transaction.transactionAt) }.getOrNull()?.let { return it }
        }
    }
    return transaction.id.toLongOrNull()?.let { Date(it) } ?: Date()
}
