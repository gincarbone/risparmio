package com.youandmedia.risparmio.ui.daydetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.ArrowCircleUp
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youandmedia.risparmio.data.model.Expense
import com.youandmedia.risparmio.data.model.Income
import com.youandmedia.risparmio.ui.components.AddEditDialog
import com.youandmedia.risparmio.ui.components.ConfirmDeleteDialog
import com.youandmedia.risparmio.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    date: LocalDate,
    viewModel: DayDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ITALIAN)

    var showExpenseDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    var editingIncome by remember { mutableStateOf<Income?>(null) }
    var deletingExpense by remember { mutableStateOf<Expense?>(null) }
    var deletingIncome by remember { mutableStateOf<Income?>(null) }

    LaunchedEffect(date) {
        viewModel.loadDay(date)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(date.format(formatter)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        },
        containerColor = BgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Expenses list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(state.expenses) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onDelete = { deletingExpense = expense },
                        onEdit = {
                            editingExpense = expense
                            showExpenseDialog = true
                        }
                    )
                    HorizontalDivider()
                }
            }

            // Incomes list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(state.incomes) { income ->
                    IncomeItem(
                        income = income,
                        onDelete = { deletingIncome = income },
                        onEdit = {
                            editingIncome = income
                            showIncomeDialog = true
                        }
                    )
                    HorizontalDivider()
                }
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        editingIncome = null
                        showIncomeDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Outlined.ArrowCircleUp,
                        contentDescription = null,
                        tint = EntrateCol,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Entrata", color = Color.Black)
                }

                Spacer(modifier = Modifier.width(20.dp))

                Button(
                    onClick = {
                        editingExpense = null
                        showExpenseDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Outlined.ArrowCircleDown,
                        contentDescription = null,
                        tint = SpeseCol,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Spesa", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Expense dialog
    if (showExpenseDialog) {
        AddEditDialog(
            title = "Aggiungi Spesa",
            categories = constTipoSpese,
            initialCategory = editingExpense?.category,
            initialAmount = editingExpense?.amount?.toString() ?: "",
            initialNotes = editingExpense?.description ?: "",
            amountColor = SpeseCol,
            onDismiss = { showExpenseDialog = false; editingExpense = null },
            onConfirm = { category, amount, notes ->
                if (editingExpense != null) {
                    viewModel.updateExpense(editingExpense!!, amount, category, notes, date)
                } else {
                    viewModel.saveExpense(amount, category, notes, date)
                }
                showExpenseDialog = false
                editingExpense = null
            }
        )
    }

    // Income dialog
    if (showIncomeDialog) {
        AddEditDialog(
            title = "Aggiungi Entrata",
            categories = constTipoEntrate,
            initialCategory = editingIncome?.category,
            initialAmount = editingIncome?.amount?.toString() ?: "",
            initialNotes = editingIncome?.description ?: "",
            amountColor = EntrateCol,
            onDismiss = { showIncomeDialog = false; editingIncome = null },
            onConfirm = { category, amount, notes ->
                if (editingIncome != null) {
                    viewModel.updateIncome(editingIncome!!, amount, category, notes, date)
                } else {
                    viewModel.saveIncome(amount, category, notes, date)
                }
                showIncomeDialog = false
                editingIncome = null
            }
        )
    }

    // Delete expense confirmation
    deletingExpense?.let { expense ->
        ConfirmDeleteDialog(
            onDismiss = { deletingExpense = null },
            onConfirm = {
                viewModel.deleteExpense(expense, date)
                deletingExpense = null
            }
        )
    }

    // Delete income confirmation
    deletingIncome?.let { income ->
        ConfirmDeleteDialog(
            onDismiss = { deletingIncome = null },
            onConfirm = {
                viewModel.deleteIncome(income, date)
                deletingIncome = null
            }
        )
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit, onEdit: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                expense.category,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = Color(0xDD000000)
            )
        },
        supportingContent = {
            Text(
                expense.description,
                fontSize = 11.sp,
                color = Color.Black
            )
        },
        leadingContent = {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "Elimina",
                    tint = Color.Gray
                )
            }
        },
        trailingContent = {
            Text(
                String.format("\u20AC -%.2f", expense.amount),
                fontSize = 18.sp,
                color = SpeseCol
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    )
}

@Composable
fun IncomeItem(income: Income, onDelete: () -> Unit, onEdit: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                income.category,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = Color(0xDD000000)
            )
        },
        supportingContent = {
            Text(
                income.description,
                fontSize = 11.sp,
                color = Color.Black
            )
        },
        leadingContent = {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "Elimina",
                    tint = Color.Gray
                )
            }
        },
        trailingContent = {
            Text(
                String.format("\u20AC +%.2f", income.amount),
                fontSize = 18.sp,
                color = EntrateCol
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    )
}
