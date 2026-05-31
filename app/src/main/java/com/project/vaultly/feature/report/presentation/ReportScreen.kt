package com.project.vaultly.feature.report.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.project.vaultly.core.navigation.navigateBack
import com.project.vaultly.feature.auth.data.UserRepository
import com.project.vaultly.theme.VaultlyColors
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    email: String = UserRepository.getCurrentUserEmail() ?: ""
) {
    val backStack = LocalBackStack.current
    val reportViewModel: ReportViewModel = viewModel()

    var selectedPeriod by remember { mutableStateOf("Bulan Ini") }
    val periods = listOf("Bulan Ini", "Bulan Lalu", "3 Bulan Terakhir", "Tahun Ini", "Semua")

    LaunchedEffect(email, selectedPeriod, backStack.size) {
        if (email.isNotEmpty()) {
            reportViewModel.load(selectedPeriod)
        }
    }

    val filteredTransactions = reportViewModel.transactions
    val budgetActualItems = reportViewModel.budgetActualItems

    val summary = reportViewModel.summary
    val totalIncome = summary?.totalIncome ?: 0.0
    val totalExpense = summary?.totalExpense ?: 0.0
    val balance = summary?.balance ?: (totalIncome - totalExpense)
    val totalBudget = budgetActualItems.sumOf { it.budgetAmount }
    val totalSpent = budgetActualItems.sumOf { it.actualExpense }
    val budgetRemaining = totalBudget - totalSpent

    // Statistik per kategori pengeluaran
    val expenseByCategory = filteredTransactions
        .filter { it.isExpense }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { transaksi -> transaksi.amount } }
        .toList()
        .sortedByDescending { it.second }

    // Statistik per kategori pemasukan
    val incomeByCategory = filteredTransactions
        .filter { !it.isExpense }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { transaksi -> transaksi.amount } }
        .toList()
        .sortedByDescending { it.second }

    // Transaksi terbaru
    val recentTransactions = filteredTransactions.take(10)
    val isDeficit = reportViewModel.deficit?.isDeficit == true

    // Nama user
    val userName = UserRepository.getUserName(email) ?: "User"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { backStack.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (reportViewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (reportViewModel.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = reportViewModel.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { reportViewModel.load(selectedPeriod) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }

            // Header dengan nama user
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = "User Avatar", modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Laporan untuk:", style = MaterialTheme.typography.bodySmall)
                        Text(userName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Pilih Periode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Periode Laporan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEach { period ->
                            FilterChip(
                                selected = selectedPeriod == period,
                                onClick = { selectedPeriod = period },
                                label = { Text(period, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Alert Defisit
            if (isDeficit) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Peringatan: Pengeluaran melebihi pemasukan dalam 2 bulan terakhir",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Ringkasan Keuangan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Ringkasan Keuangan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Pemasukan", tint = VaultlyColors.Income, modifier = Modifier.size(24.dp))
                            Text("Pemasukan", style = MaterialTheme.typography.labelSmall)
                            Text(
                                formatRupiah(totalIncome),
                                fontWeight = FontWeight.Bold,
                                color = VaultlyColors.Income,
                                fontSize = 16.sp
                            )
                        }
                        VerticalDivider(modifier = Modifier.height(60.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Pengeluaran", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                            Text("Pengeluaran", style = MaterialTheme.typography.labelSmall)
                            Text(
                                formatRupiah(totalExpense),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp
                            )
                        }
                        VerticalDivider(modifier = Modifier.height(60.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AccountBalance, contentDescription = "Saldo", modifier = Modifier.size(24.dp))
                            Text("Saldo", style = MaterialTheme.typography.labelSmall)
                            Text(
                                formatRupiah(balance),
                                fontWeight = FontWeight.Bold,
                                color = if (balance >= 0) VaultlyColors.Success else MaterialTheme.colorScheme.error,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Perbandingan Budget vs Realisasi
            if (budgetActualItems.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Anggaran vs Realisasi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Anggaran", style = MaterialTheme.typography.labelSmall)
                                Text(formatRupiah(totalBudget), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Realisasi", style = MaterialTheme.typography.labelSmall)
                                Text(formatRupiah(totalSpent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sisa", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    formatRupiah(budgetRemaining),
                                    fontWeight = FontWeight.Bold,
                                    color = if (budgetRemaining >= 0) VaultlyColors.Success else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val budgetProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
                        LinearProgressIndicator(
                            progress = budgetProgress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth(),
                            color = if (budgetProgress > 0.8f) MaterialTheme.colorScheme.error else VaultlyColors.Income
                        )
                        Text(
                            text = "${(budgetProgress * 100).toInt()}% anggaran terpakai",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            // Pengeluaran per Kategori
            if (expenseByCategory.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pengeluaran per Kategori", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        expenseByCategory.forEach { (category, amount) ->
                            val percentage = if (totalExpense > 0) (amount / totalExpense * 100).toInt() else 0
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(category, style = MaterialTheme.typography.bodyMedium)
                                    Text(formatRupiah(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = (percentage / 100f),
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text("$percentage%", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // Pemasukan per Kategori
            if (incomeByCategory.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pemasukan per Kategori", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        incomeByCategory.forEach { (category, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, style = MaterialTheme.typography.bodyMedium)
                                Text(formatRupiah(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = VaultlyColors.Income)
                            }
                        }
                    }
                }
            }

            // Detail Budget per Kategori
            if (budgetActualItems.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Detail Budget per Kategori", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        budgetActualItems.forEach { budget ->
                            val percentage = if (budget.budgetAmount > 0) ((budget.actualExpense / budget.budgetAmount) * 100).toInt() else 0
                            val isOverBudget = budget.isOverBudget

                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(budget.category, fontWeight = FontWeight.Medium)
                                    Text(
                                        if (isOverBudget) "Peringatan: Melebihi budget" else "Sisa ${formatRupiah(budget.remaining)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOverBudget) MaterialTheme.colorScheme.error else VaultlyColors.Success
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Target: ${formatRupiah(budget.budgetAmount)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Realisasi: ${formatRupiah(budget.actualExpense)}", style = MaterialTheme.typography.bodySmall)
                                }
                                LinearProgressIndicator(
                                    progress = (percentage / 100f).coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = when {
                                        percentage > 100 -> MaterialTheme.colorScheme.error
                                        percentage > 80 -> VaultlyColors.Warning
                                        else -> VaultlyColors.Income
                                    }
                                )
                                Text("$percentage% terpakai", style = MaterialTheme.typography.bodySmall)
                            }
                            if (budgetActualItems.last() != budget) HorizontalDivider()
                        }
                    }
                }
            }

            // Transaksi Terbaru
            if (recentTransactions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Transaksi Terbaru", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        recentTransactions.forEach { transaction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(transaction.title, fontWeight = FontWeight.Medium)
                                    Text(transaction.category, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        formatApiDate(transaction.transactionAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = if (transaction.isExpense) "- ${formatRupiah(transaction.amount)}" else "+ ${formatRupiah(transaction.amount)}",
                                    color = if (transaction.isExpense) MaterialTheme.colorScheme.error else VaultlyColors.Income,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (recentTransactions.last() != transaction) HorizontalDivider()
                        }
                    }
                }
            }

            // Footer
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Dibuat pada: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Filter transaksi berdasarkan periode
private fun filterTransactionsByPeriod(
    transactions: List<com.project.vaultly.feature.transaction.presentation.Transaction>,
    period: String
): List<com.project.vaultly.feature.transaction.presentation.Transaction> {
    val calendar = Calendar.getInstance()

    return when (period) {
        "Bulan Ini" -> {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            transactions.filter { transaction ->
                val transDate = parseTransactionDate(transaction)
                val transCalendar = Calendar.getInstance().apply { time = transDate }
                transCalendar.get(Calendar.YEAR) == year && transCalendar.get(Calendar.MONTH) == month
            }
        }
        "Bulan Lalu" -> {
            calendar.add(Calendar.MONTH, -1)
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            transactions.filter { transaction ->
                val transDate = parseTransactionDate(transaction)
                val transCalendar = Calendar.getInstance().apply { time = transDate }
                transCalendar.get(Calendar.YEAR) == year && transCalendar.get(Calendar.MONTH) == month
            }
        }
        "3 Bulan Terakhir" -> {
            val threeMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.time
            transactions.filter { transaction ->
                parseTransactionDate(transaction) >= threeMonthsAgo
            }
        }
        "Tahun Ini" -> {
            val year = calendar.get(Calendar.YEAR)
            transactions.filter { transaction ->
                val transDate = parseTransactionDate(transaction)
                val transCalendar = Calendar.getInstance().apply { time = transDate }
                transCalendar.get(Calendar.YEAR) == year
            }
        }
        else -> transactions
    }
}

// Cek defisit (pengeluaran > pemasukan dalam 2 bulan terakhir)
private fun checkDeficit(
    transactions: List<com.project.vaultly.feature.transaction.presentation.Transaction>
): Boolean {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    var thisMonthIncome = 0.0
    var thisMonthExpense = 0.0

    calendar.add(Calendar.MONTH, -1)
    val lastMonth = calendar.get(Calendar.MONTH)
    val lastYear = calendar.get(Calendar.YEAR)
    var lastMonthIncome = 0.0
    var lastMonthExpense = 0.0

    transactions.forEach { transaction ->
        val transDate = parseTransactionDate(transaction)
        val transCalendar = Calendar.getInstance().apply { time = transDate }
        val transMonth = transCalendar.get(Calendar.MONTH)
        val transYear = transCalendar.get(Calendar.YEAR)

        when {
            transYear == currentYear && transMonth == currentMonth -> {
                if (transaction.isExpense) thisMonthExpense += transaction.amount
                else thisMonthIncome += transaction.amount
            }
            transYear == lastYear && transMonth == lastMonth -> {
                if (transaction.isExpense) lastMonthExpense += transaction.amount
                else lastMonthIncome += transaction.amount
            }
        }
    }

    val thisMonthDeficit = thisMonthExpense > thisMonthIncome
    val lastMonthDeficit = lastMonthExpense > lastMonthIncome

    return thisMonthDeficit && lastMonthDeficit
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(timestamp))
}

private fun formatApiDate(value: String): String {
    if (value.isBlank()) return "-"
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    return runCatching { formatter.format(parseApiDate(value) ?: Date()) }.getOrDefault(value)
}

private fun parseTransactionDate(transaction: com.project.vaultly.feature.transaction.presentation.Transaction): Date {
    if (transaction.transactionAt.isNotBlank()) {
        parseApiDate(transaction.transactionAt)?.let { return it }
    }
    return transaction.id.toLongOrNull()?.let { Date(it) } ?: Date()
}

private fun parseApiDate(value: String): Date? {
    val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd HH:mm:ss")
    patterns.forEach { pattern ->
        val parser = SimpleDateFormat(pattern, Locale.US).apply {
            if (pattern.contains("'Z'")) {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        }
        runCatching { parser.parse(value) }.getOrNull()?.let { return it }
    }
    return null
}

private fun formatRupiah(amount: Double): String {
    return "Rp ${String.format("%,.0f", amount).replace(',', '.')}"
}
