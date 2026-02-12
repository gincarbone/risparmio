package com.youandmedia.risparmio.ui.dashboard

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.youandmedia.risparmio.RisparmioApp
import com.youandmedia.risparmio.data.model.Expense
import com.youandmedia.risparmio.data.model.FixedExpense
import com.youandmedia.risparmio.data.model.FixedIncome
import com.youandmedia.risparmio.data.model.Income
import com.youandmedia.risparmio.ui.components.AddFixedEntryDialog
import com.youandmedia.risparmio.ui.components.ConfirmDeleteDialog
import com.youandmedia.risparmio.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class RecentTransaction(
    val id: Int,
    val category: String,
    val amount: Double,
    val description: String,
    val date: LocalDate,
    val isExpense: Boolean
)

data class DashboardUiState(
    val totalFixedIncomes: Double = 0.0,
    val totalFixedExpenses: Double = 0.0,
    val monthExpenses: Double = 0.0,
    val monthIncomes: Double = 0.0,
    val dailyBudget: Double = 0.0,
    val todaySpent: Double = 0.0,
    val fixedIncomes: List<FixedIncome> = emptyList(),
    val fixedExpenses: List<FixedExpense> = emptyList(),
    val recentTransactions: List<RecentTransaction> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as RisparmioApp).database

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            val month = YearMonth.now()
            val zone = ZoneId.systemDefault()
            val startOfMonth = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfMonth = month.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfDay = today.atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

            val monthExpenses = db.expenseDao().getByDateRangeOnce(startOfMonth, endOfMonth)
            val monthIncomes = db.incomeDao().getByDateRangeOnce(startOfMonth, endOfMonth)
            val fixedIncomes = db.fixedIncomeDao().getAllOnce()
            val fixedExpenses = db.fixedExpenseDao().getAllOnce()

            val totalFixedInc = fixedIncomes.sumOf { it.amount }
            val totalFixedExp = fixedExpenses.sumOf { it.amount }
            val daysInMonth = month.lengthOfMonth()
            val dailyBudget = if (daysInMonth > 0) (totalFixedInc - totalFixedExp) / daysInMonth else 0.0

            val todayExpenses = db.expenseDao().getByDateRangeOnce(startOfDay, endOfDay)
            val todaySpent = todayExpenses.sumOf { it.amount }

            // Build recent transactions (last 10)
            val recentExp = monthExpenses.map {
                RecentTransaction(it.id, it.category, it.amount, it.description,
                    Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate(), true)
            }
            val recentInc = monthIncomes.map {
                RecentTransaction(it.id, it.category, it.amount, it.description,
                    Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate(), false)
            }
            val recent = (recentExp + recentInc).sortedByDescending { it.date }.take(10)

            _uiState.value = DashboardUiState(
                totalFixedIncomes = totalFixedInc,
                totalFixedExpenses = totalFixedExp,
                monthExpenses = monthExpenses.sumOf { it.amount },
                monthIncomes = monthIncomes.sumOf { it.amount },
                dailyBudget = dailyBudget,
                todaySpent = todaySpent,
                fixedIncomes = fixedIncomes,
                fixedExpenses = fixedExpenses,
                recentTransactions = recent
            )
        }
    }

    fun addFixedIncome(category: String, amount: Double) {
        viewModelScope.launch { db.fixedIncomeDao().insert(FixedIncome(category = category, amount = amount)); loadData() }
    }
    fun addFixedExpense(category: String, amount: Double) {
        viewModelScope.launch { db.fixedExpenseDao().insert(FixedExpense(category = category, amount = amount)); loadData() }
    }
    fun deleteFixedIncome(item: FixedIncome) {
        viewModelScope.launch { db.fixedIncomeDao().delete(item); loadData() }
    }
    fun deleteFixedExpense(item: FixedExpense) {
        viewModelScope.launch { db.fixedExpenseDao().delete(item); loadData() }
    }
    fun updateFixedIncome(item: FixedIncome, category: String, amount: Double) {
        viewModelScope.launch { db.fixedIncomeDao().update(item.copy(category = category, amount = amount)); loadData() }
    }
    fun updateFixedExpense(item: FixedExpense, category: String, amount: Double) {
        viewModelScope.launch { db.fixedExpenseDao().update(item.copy(category = category, amount = amount)); loadData() }
    }
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsState()
    val balance = state.totalFixedIncomes - state.totalFixedExpenses - state.monthExpenses + state.monthIncomes
    val todayRemaining = state.dailyBudget - state.todaySpent

    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingFixedIncome by remember { mutableStateOf<FixedIncome?>(null) }
    var editingFixedExpense by remember { mutableStateOf<FixedExpense?>(null) }
    var deletingFixedIncome by remember { mutableStateOf<FixedIncome?>(null) }
    var deletingFixedExpense by remember { mutableStateOf<FixedExpense?>(null) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
        Spacer(modifier = Modifier.height(16.dp))

        // Balance card
        Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 8.dp) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(GradientBlue, GradientPurple)))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Bilancio del Mese", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Text(
                        String.format("\u20AC %.2f", balance),
                        color = if (balance >= 0) Color.White else Color(0xFFFF6B6B),
                        fontSize = 34.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MiniStat("Entrate Fisse", String.format("\u20AC%.0f", state.totalFixedIncomes), Icons.Default.ArrowCircleUp, Color(0xFF4ECDC4))
                        MiniStat("Uscite Fisse", String.format("\u20AC%.0f", state.totalFixedExpenses), Icons.Default.ArrowCircleDown, Color(0xFFFF6B6B))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Budget cards row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BudgetCard("Budget Giornaliero", String.format("\u20AC%.2f", state.dailyBudget),
                Icons.Default.CalendarToday, Color(0xFF45B7D1), Modifier.weight(1f))
            BudgetCard("Speso Oggi", String.format("\u20AC%.2f", state.todaySpent),
                Icons.Default.ShoppingCart, if (state.todaySpent > state.dailyBudget) Color(0xFFFF6B6B) else Color(0xFF4ECDC4), Modifier.weight(1f))
            BudgetCard("Rimane Oggi", String.format("\u20AC%.2f", todayRemaining),
                Icons.Default.Savings, if (todayRemaining < 0) Color(0xFFFF6B6B) else Color(0xFF2ECC71), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fixed Incomes
        FixedSection(
            title = "Entrate Fisse",
            icon = Icons.Default.ArrowCircleUp,
            iconColor = Color(0xFF448AFF),
            items = state.fixedIncomes.map { Triple(it.id, it.category, it.amount) },
            itemColor = Color(0xFF448AFF),
            onAdd = { showAddIncomeDialog = true },
            onEdit = { id -> editingFixedIncome = state.fixedIncomes.find { it.id == id }; showAddIncomeDialog = true },
            onDelete = { id -> deletingFixedIncome = state.fixedIncomes.find { it.id == id } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fixed Expenses
        FixedSection(
            title = "Uscite Fisse",
            icon = Icons.Default.ArrowCircleDown,
            iconColor = Color(0xFFFF5252),
            items = state.fixedExpenses.map { Triple(it.id, it.category, it.amount) },
            itemColor = Color(0xFFFF5252),
            onAdd = { showAddExpenseDialog = true },
            onEdit = { id -> editingFixedExpense = state.fixedExpenses.find { it.id == id }; showAddExpenseDialog = true },
            onDelete = { id -> deletingFixedExpense = state.fixedExpenses.find { it.id == id } }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Recent transactions
        Text("Ultimi Movimenti", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
        Spacer(modifier = Modifier.height(8.dp))

        if (state.recentTransactions.isEmpty()) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Text("Nessun movimento questo mese", color = Color.Gray, fontSize = 14.sp,
                    modifier = Modifier.padding(24.dp))
            }
        } else {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column {
                    state.recentTransactions.forEachIndexed { index, tx ->
                        RecentTransactionRow(tx)
                        if (index < state.recentTransactions.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogs
    if (showAddIncomeDialog) {
        AddFixedEntryDialog(
            isExpense = false, categories = constEntrateFisse,
            initialCategory = editingFixedIncome?.category, initialAmount = editingFixedIncome?.amount?.toString() ?: "",
            onDismiss = { showAddIncomeDialog = false; editingFixedIncome = null },
            onSave = { cat, amt ->
                if (editingFixedIncome != null) viewModel.updateFixedIncome(editingFixedIncome!!, cat, amt)
                else viewModel.addFixedIncome(cat, amt)
                showAddIncomeDialog = false; editingFixedIncome = null
            }
        )
    }
    if (showAddExpenseDialog) {
        AddFixedEntryDialog(
            isExpense = true, categories = constUsciteFisse,
            initialCategory = editingFixedExpense?.category, initialAmount = editingFixedExpense?.amount?.toString() ?: "",
            onDismiss = { showAddExpenseDialog = false; editingFixedExpense = null },
            onSave = { cat, amt ->
                if (editingFixedExpense != null) viewModel.updateFixedExpense(editingFixedExpense!!, cat, amt)
                else viewModel.addFixedExpense(cat, amt)
                showAddExpenseDialog = false; editingFixedExpense = null
            }
        )
    }
    deletingFixedIncome?.let { item ->
        ConfirmDeleteDialog(onDismiss = { deletingFixedIncome = null },
            onConfirm = { viewModel.deleteFixedIncome(item); deletingFixedIncome = null })
    }
    deletingFixedExpense?.let { item ->
        ConfirmDeleteDialog(onDismiss = { deletingFixedExpense = null },
            onConfirm = { viewModel.deleteFixedExpense(item); deletingFixedExpense = null })
    }
}

@Composable
fun MiniStat(label: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BudgetCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C3E50))
            Text(title, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun RecentTransactionRow(tx: RecentTransaction) {
    val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(if (tx.isExpense) SpeseCol.copy(alpha = 0.15f) else EntrateCol.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center) {
            Icon(if (tx.isExpense) Icons.Default.ArrowCircleDown else Icons.Default.ArrowCircleUp, null,
                tint = if (tx.isExpense) SpeseCol else EntrateCol, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(tx.category, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C3E50))
            Text(tx.date.format(fmt), fontSize = 11.sp, color = Color.Gray)
        }
        Text(
            String.format("%s\u20AC%.2f", if (tx.isExpense) "-" else "+", tx.amount),
            fontSize = 15.sp, fontWeight = FontWeight.Bold,
            color = if (tx.isExpense) SpeseCol else EntrateCol
        )
    }
}

@Composable
fun FixedSection(
    title: String, icon: ImageVector, iconColor: Color,
    items: List<Triple<Int, String, Double>>, itemColor: Color,
    onAdd: () -> Unit, onEdit: (Int) -> Unit, onDelete: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
        Column {
            Surface(onClick = { expanded = !expanded }, color = Color.Transparent) {
                ListItem(
                    headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
                    leadingContent = { Icon(icon, null, tint = iconColor) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(String.format("\u20AC%.2f", items.sumOf { it.third }), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = iconColor)
                            Spacer(Modifier.width(4.dp))
                            Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                        }
                    }
                )
            }
            if (expanded) {
                Surface(onClick = onAdd, color = Color.Transparent) {
                    ListItem(headlineContent = { Text("Aggiungi") }, leadingContent = { Icon(Icons.Default.Add, null) })
                }
                items.forEach { (id, cat, amt) ->
                    Surface(onClick = { onEdit(id) }) {
                        ListItem(
                            headlineContent = { Text("$cat: \u20AC${String.format("%.2f", amt)}", color = Color.White, fontSize = 12.sp) },
                            leadingContent = { IconButton(onClick = { onDelete(id) }) { Icon(Icons.Outlined.DeleteOutline, "Elimina", tint = Color.White) } },
                            colors = ListItemDefaults.colors(containerColor = itemColor)
                        )
                    }
                }
            }
        }
    }
}
