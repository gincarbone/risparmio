package com.youandmedia.risparmio.ui.drawer

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.youandmedia.risparmio.RisparmioApp
import com.youandmedia.risparmio.data.model.FixedExpense
import com.youandmedia.risparmio.data.model.FixedIncome
import com.youandmedia.risparmio.ui.components.AddFixedEntryDialog
import com.youandmedia.risparmio.ui.components.ConfirmDeleteDialog
import com.youandmedia.risparmio.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.YearMonth

data class DrawerUiState(
    val fixedIncomes: List<FixedIncome> = emptyList(),
    val fixedExpenses: List<FixedExpense> = emptyList(),
    val totalFixedIncomes: Double = 0.0,
    val totalFixedExpenses: Double = 0.0,
    val dailyBudget: Double = 0.0
)

class DrawerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as RisparmioApp).database

    private val _uiState = MutableStateFlow(DrawerUiState())
    val uiState: StateFlow<DrawerUiState> = _uiState

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            val fixedIncomes = db.fixedIncomeDao().getAllOnce()
            val fixedExpenses = db.fixedExpenseDao().getAllOnce()
            val totalInc = fixedIncomes.sumOf { it.amount }
            val totalExp = fixedExpenses.sumOf { it.amount }
            val daysInMonth = YearMonth.now().lengthOfMonth()
            val dailyBudget = if (daysInMonth > 0) (totalInc - totalExp) / daysInMonth else 0.0

            _uiState.value = DrawerUiState(
                fixedIncomes = fixedIncomes,
                fixedExpenses = fixedExpenses,
                totalFixedIncomes = totalInc,
                totalFixedExpenses = totalExp,
                dailyBudget = dailyBudget
            )
        }
    }

    fun addFixedIncome(category: String, amount: Double) {
        viewModelScope.launch {
            db.fixedIncomeDao().insert(FixedIncome(category = category, amount = amount))
            loadData()
        }
    }

    fun addFixedExpense(category: String, amount: Double) {
        viewModelScope.launch {
            db.fixedExpenseDao().insert(FixedExpense(category = category, amount = amount))
            loadData()
        }
    }

    fun deleteFixedIncome(item: FixedIncome) {
        viewModelScope.launch {
            db.fixedIncomeDao().delete(item)
            loadData()
        }
    }

    fun deleteFixedExpense(item: FixedExpense) {
        viewModelScope.launch {
            db.fixedExpenseDao().delete(item)
            loadData()
        }
    }

    fun updateFixedIncome(item: FixedIncome, category: String, amount: Double) {
        viewModelScope.launch {
            db.fixedIncomeDao().update(item.copy(category = category, amount = amount))
            loadData()
        }
    }

    fun updateFixedExpense(item: FixedExpense, category: String, amount: Double) {
        viewModelScope.launch {
            db.fixedExpenseDao().update(item.copy(category = category, amount = amount))
            loadData()
        }
    }
}

@Composable
fun MainDrawer(
    viewModel: DrawerViewModel,
    onStatsClick: () -> Unit,
    onClose: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingFixedIncome by remember { mutableStateOf<FixedIncome?>(null) }
    var editingFixedExpense by remember { mutableStateOf<FixedExpense?>(null) }
    var deletingFixedIncome by remember { mutableStateOf<FixedIncome?>(null) }
    var deletingFixedExpense by remember { mutableStateOf<FixedExpense?>(null) }

    LaunchedEffect(Unit) { viewModel.loadData() }

    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxHeight()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Brush.linearGradient(colors = listOf(GradientBlue, GradientPurple))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Savings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp).padding(8.dp)
                    )
                    Text("Risparmio $APP_VERSION", color = Color.White, fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable content
            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                FixedEntrySection(
                    title = "Entrate Fisse",
                    icon = Icons.Default.ArrowCircleUp,
                    iconColor = Color(0xFF448AFF),
                    items = state.fixedIncomes.map { Triple(it.id, it.category, it.amount) },
                    itemColor = Color(0xFF448AFF),
                    onAdd = { showAddIncomeDialog = true },
                    onEdit = { id -> editingFixedIncome = state.fixedIncomes.find { it.id == id }; showAddIncomeDialog = true },
                    onDelete = { id -> deletingFixedIncome = state.fixedIncomes.find { it.id == id } }
                )

                FixedEntrySection(
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

                ListItem(
                    headlineContent = {
                        Column {
                            Text("Disponibilità: \u20AC${String.format("%.2f", state.totalFixedIncomes - state.totalFixedExpenses)}")
                            Text("Budget Giornaliero: \u20AC${String.format("%.2f", state.dailyBudget)}")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(onClick = onStatsClick) {
                    ListItem(
                        headlineContent = { Text("Statistiche", color = Color.White) },
                        leadingContent = { Icon(Icons.Default.PieChart, contentDescription = null, tint = Color.White) },
                        colors = ListItemDefaults.colors(containerColor = Color(0xFF9C27B0))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val context = LocalContext.current
                Surface(onClick = { shareApk(context) }) {
                    ListItem(
                        headlineContent = { Text("Condividi App", color = Color.White) },
                        leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.White) },
                        colors = ListItemDefaults.colors(containerColor = Color(0xFF2196F3))
                    )
                }
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("You&Media (2023)", color = Color.Gray, fontSize = 11.sp)
            }
        }
    }

    // Dialogs
    if (showAddIncomeDialog) {
        AddFixedEntryDialog(
            isExpense = false,
            categories = constEntrateFisse,
            initialCategory = editingFixedIncome?.category,
            initialAmount = editingFixedIncome?.amount?.toString() ?: "",
            onDismiss = { showAddIncomeDialog = false; editingFixedIncome = null },
            onSave = { category, amount ->
                if (editingFixedIncome != null) viewModel.updateFixedIncome(editingFixedIncome!!, category, amount)
                else viewModel.addFixedIncome(category, amount)
                showAddIncomeDialog = false; editingFixedIncome = null
            }
        )
    }

    if (showAddExpenseDialog) {
        AddFixedEntryDialog(
            isExpense = true,
            categories = constUsciteFisse,
            initialCategory = editingFixedExpense?.category,
            initialAmount = editingFixedExpense?.amount?.toString() ?: "",
            onDismiss = { showAddExpenseDialog = false; editingFixedExpense = null },
            onSave = { category, amount ->
                if (editingFixedExpense != null) viewModel.updateFixedExpense(editingFixedExpense!!, category, amount)
                else viewModel.addFixedExpense(category, amount)
                showAddExpenseDialog = false; editingFixedExpense = null
            }
        )
    }

    deletingFixedIncome?.let { item ->
        ConfirmDeleteDialog(
            onDismiss = { deletingFixedIncome = null },
            onConfirm = { viewModel.deleteFixedIncome(item); deletingFixedIncome = null }
        )
    }

    deletingFixedExpense?.let { item ->
        ConfirmDeleteDialog(
            onDismiss = { deletingFixedExpense = null },
            onConfirm = { viewModel.deleteFixedExpense(item); deletingFixedExpense = null }
        )
    }
}

private fun shareApk(context: Context) {
    val apkFile = File(context.applicationInfo.sourceDir)
    val cacheDir = File(context.cacheDir, "apk")
    cacheDir.mkdirs()
    val sharedApk = File(cacheDir, "Risparmio.apk")
    apkFile.copyTo(sharedApk, overwrite = true)

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        sharedApk
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.android.package-archive"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Condividi Risparmio"))
}

@Composable
fun FixedEntrySection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    items: List<Triple<Int, String, Double>>,
    itemColor: Color,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(onClick = { expanded = !expanded }, color = Color.Transparent) {
        ListItem(
            headlineContent = { Text(title) },
            leadingContent = { Icon(icon, contentDescription = null, tint = iconColor) },
            trailingContent = {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        )
    }

    if (expanded) {
        Surface(onClick = onAdd, color = Color.Transparent) {
            ListItem(
                headlineContent = { Text("Aggiungi") },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) }
            )
        }

        items.forEach { (id, category, amount) ->
            Surface(onClick = { onEdit(id) }) {
                ListItem(
                    headlineContent = {
                        Text("$category: \u20AC ${String.format("%.2f", amount)}", color = Color.White, fontSize = 12.sp)
                    },
                    leadingContent = {
                        IconButton(onClick = { onDelete(id) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Elimina", tint = Color.White)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = itemColor)
                )
            }
        }
    }
}
