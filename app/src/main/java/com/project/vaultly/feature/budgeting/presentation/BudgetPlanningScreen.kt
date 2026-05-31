package com.project.vaultly.feature.budgeting.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.project.vaultly.feature.transaction.presentation.TransactionViewModel
import com.project.vaultly.theme.VaultlyColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlanningScreen(
    email: String = UserRepository.getCurrentUserEmail() ?: ""
) {
    val backStack = LocalBackStack.current
    val budgetViewModel: BudgetViewModel = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    var newAmount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Load budgets untuk user ini
    LaunchedEffect(email, backStack.size) {
        if (email.isNotEmpty()) {
            budgetViewModel.loadBudgetsForUser(email)
            transactionViewModel.loadTransactionsForUser(email)
        }
    }

    // Sinkronisasi spent dengan transaksi setiap kali transaksi berubah
    LaunchedEffect(transactionViewModel.transactions.size) {
        budgetViewModel.recalculateSpent(transactionViewModel.transactions)
    }

    val budgets = budgetViewModel.budgets
    val totalBudget = budgetViewModel.getTotalBudget()
    val totalSpent = budgetViewModel.getTotalSpent()
    val remainingBudget = budgetViewModel.getRemainingBudget()

    val categories = listOf(
        "Makanan", "Transportasi", "Belanja", "Hiburan",
        "Kesehatan", "Pendidikan", "Tagihan", "Lainnya"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perencanaan Anggaran", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { backStack.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Budget")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                budgetViewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                budgetViewModel.errorMessage.isNotEmpty() && budgets.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = budgetViewModel.errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { budgetViewModel.loadBudgetsForUser(email) }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                budgets.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Belum ada anggaran",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tambah anggaran per kategori untuk mulai merencanakan keuangan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { showAddDialog = true }) {
                            Text("Tambah Anggaran")
                        }
                    }
                }
                }
                else -> {
                if (budgetViewModel.errorMessage.isNotEmpty()) {
                    Text(
                        text = budgetViewModel.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                // Ringkasan Anggaran
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
                        Text("Ringkasan Anggaran", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Anggaran", style = MaterialTheme.typography.labelSmall)
                                Text(formatRupiah(totalBudget), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Terpakai", style = MaterialTheme.typography.labelSmall)
                                Text(formatRupiah(totalSpent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            VerticalDivider(modifier = Modifier.height(40.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sisa Anggaran", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    formatRupiah(remainingBudget),
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingBudget >= 0) VaultlyColors.Success else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Progress Bar Sisa Anggaran
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Penggunaan Anggaran", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        val progress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
                        LinearProgressIndicator(
                            progress = progress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth(),
                            color = if (progress > 0.8f) MaterialTheme.colorScheme.error else VaultlyColors.Income
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(progress * 100).toInt()}% terpakai",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Anggaran per Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(budgets) { budget ->
                        BudgetCard(
                            budget = budget,
                            onEdit = {
                                selectedBudget = budget
                                newAmount = budget.amount.toString()
                                showEditDialog = true
                            },
                            onDelete = { budgetViewModel.deleteBudget(it) }
                        )
                    }
                }
            }
            }
        }
    }

    // Dialog Tambah Budget
    if (showAddDialog) {
        var selectedCategory by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Anggaran") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            placeholder = { Text("Pilih kategori") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        expanded = false
                                        errorMessage = ""
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newAmount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                newAmount = it
                                errorMessage = ""
                            }
                        },
                        label = { Text("Nominal Anggaran") },
                        placeholder = { Text("Contoh: 1000000") },
                        leadingIcon = { Text("Rp ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            selectedCategory.isEmpty() -> errorMessage = "Pilih kategori terlebih dahulu"
                            newAmount.isEmpty() -> errorMessage = "Masukkan nominal anggaran"
                            else -> {
                                val amount = newAmount.replace(",", ".").toDoubleOrNull()
                                if (amount == null || amount <= 0) {
                                    errorMessage = "Nominal tidak valid"
                                } else {
                                    budgetViewModel.addBudget(selectedCategory, amount)
                                    showAddDialog = false
                                    newAmount = ""
                                }
                            }
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Edit Budget
    if (showEditDialog && selectedBudget != null) {
        var editAmount by remember { mutableStateOf(newAmount) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Anggaran - ${selectedBudget?.category}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                editAmount = it
                            }
                        },
                        label = { Text("Nominal Anggaran") },
                        leadingIcon = { Text("Rp ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = editAmount.replace(",", ".").toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            budgetViewModel.updateBudget(selectedBudget!!.id, amount)
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun BudgetCard(
    budget: Budget,
    onEdit: () -> Unit,
    onDelete: (String) -> Unit
) {
    val progress = if (budget.amount > 0) (budget.spent / budget.amount).toFloat() else 0f
    val remaining = budget.amount - budget.spent
    val isOverBudget = remaining < 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    budget.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onDelete(budget.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Target:", style = MaterialTheme.typography.bodySmall)
                Text(formatRupiah(budget.amount), fontWeight = FontWeight.SemiBold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Terpakai:", style = MaterialTheme.typography.bodySmall)
                Text(
                    formatRupiah(budget.spent),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sisa:", style = MaterialTheme.typography.bodySmall)
                Text(
                    formatRupiah(remaining),
                    color = if (!isOverBudget) VaultlyColors.Success else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    progress > 0.9f -> MaterialTheme.colorScheme.error
                    progress > 0.7f -> VaultlyColors.Warning
                    else -> VaultlyColors.Income
                }
            )

            Text(
                text = "${(progress * 100).toInt()}% terpakai",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )

            if (isOverBudget) {
                Text(
                    text = "Peringatan: Melebihi anggaran sebesar ${formatRupiah(-remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatRupiah(amount: Double): String {
    return "Rp ${String.format("%,.0f", amount).replace(',', '.')}"
}
