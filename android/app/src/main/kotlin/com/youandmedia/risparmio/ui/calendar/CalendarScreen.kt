package com.youandmedia.risparmio.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youandmedia.risparmio.ui.components.GaugeWidget
import com.youandmedia.risparmio.util.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onDaySelected: (LocalDate) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Quick-add bottom sheet state
    var showQuickAdd by remember { mutableStateOf(false) }
    var quickAddIsExpense by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.selectedMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.ITALIAN)
                            .replaceFirstChar { it.uppercase() }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = GradientPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi veloce")
            }
        },
        containerColor = BgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Calendar
            CalendarGrid(
                yearMonth = state.selectedMonth,
                datesWithExpenses = state.datesWithExpenses,
                datesWithIncomes = state.datesWithIncomes,
                dailyExpenses = state.dailyExpenses,
                dailyBudget = state.dailyBudget,
                onDaySelected = onDaySelected,
                onMonthChanged = { viewModel.onMonthChanged(it) }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Gauge
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                val gaugeValue = if (state.totalFixedIncomes > 0 &&
                    (state.totalFixedIncomes - state.totalFixedExpenses - state.totalExpensesOfMonth) > 0
                ) {
                    (state.totalFixedIncomes - state.totalFixedExpenses - state.totalExpensesOfMonth) / state.totalFixedIncomes
                } else 0.0

                GaugeWidget(
                    value = gaugeValue,
                    entrateFisse = state.totalFixedIncomes,
                    entrateMese = state.totalIncomesOfMonth,
                    usciteFisse = state.totalFixedExpenses,
                    usciteMese = state.totalExpensesOfMonth
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Forecast card (current month) or past month card
            if (viewModel.isCurrentMonth()) {
                ForecastCard(state)
            } else {
                PastMonthCard(state)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Quick-add bottom sheet
    if (showQuickAdd) {
        QuickAddBottomSheet(
            isExpense = quickAddIsExpense,
            onToggleType = { quickAddIsExpense = !quickAddIsExpense },
            onDismiss = { showQuickAdd = false },
            onConfirm = { amount, category, description ->
                if (quickAddIsExpense) {
                    viewModel.quickAddExpense(amount, category, description, LocalDate.now())
                } else {
                    viewModel.quickAddIncome(amount, category, description, LocalDate.now())
                }
                showQuickAdd = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    isExpense: Boolean,
    onToggleType: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    val categories = if (isExpense) constTipoSpese else constTipoEntrate
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Reset category when type changes
    LaunchedEffect(isExpense) {
        selectedCategory = (if (isExpense) constTipoSpese else constTipoEntrate).first()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Aggiungi Veloce",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Type toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isExpense,
                    onClick = { if (!isExpense) onToggleType() },
                    label = { Text("Spesa") },
                    leadingIcon = if (isExpense) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpeseCol,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !isExpense,
                    onClick = { if (isExpense) onToggleType() },
                    label = { Text("Entrata") },
                    leadingIcon = if (!isExpense) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EntrateCol,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.replace(',', '.') },
                label = { Text("Importo") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) SpeseCol else EntrateCol,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = { if (it.length <= 24) notesText = it },
                label = { Text("Note (opzionale)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                supportingText = { Text("${notesText.length}/24") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount, selectedCategory, notesText)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) SpeseCol else EntrateCol
                )
            ) {
                Text(
                    if (isExpense) "Aggiungi Spesa" else "Aggiungi Entrata",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    datesWithExpenses: Set<LocalDate>,
    datesWithIncomes: Set<LocalDate>,
    dailyExpenses: Map<LocalDate, Double>,
    dailyBudget: Double,
    onDaySelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    val today = LocalDate.now()
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) // Monday = 0

    val dayNames = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(30.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(yearMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mese precedente")
                }
                Text(
                    "${yearMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.ITALIAN).replaceFirstChar { it.uppercase() }} ${yearMonth.year}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = { onMonthChanged(yearMonth.plusMonths(1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Mese successivo")
                }
            }

            // Day of week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                dayNames.forEachIndexed { index, name ->
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (index >= 5) 12.sp else 15.sp,
                        color = if (index >= 5) Color(0xFFFF5252) else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar days grid
            var dayCounter = 1
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        if (cellIndex >= firstDayOfWeek && dayCounter <= daysInMonth) {
                            val date = yearMonth.atDay(dayCounter)
                            val isToday = date == today
                            val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
                            val hasExpense = datesWithExpenses.contains(date)
                            val hasIncome = datesWithIncomes.contains(date)

                            // Budget traffic light color
                            val daySpent = dailyExpenses[date] ?: 0.0
                            val budgetColor = if (hasExpense && dailyBudget > 0) {
                                val ratio = daySpent / dailyBudget
                                when {
                                    ratio <= 0.7 -> Color(0xFF4CAF50).copy(alpha = 0.15f) // green
                                    ratio <= 1.0 -> Color(0xFFFFC107).copy(alpha = 0.15f) // yellow
                                    else -> Color(0xFFF44336).copy(alpha = 0.15f) // red
                                }
                            } else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(budgetColor)
                                    .clickable { onDaySelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dayCounter",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isToday -> Color.Blue
                                        isWeekend -> Color(0xFFFF5252)
                                        else -> Color.Black
                                    }
                                )

                                // Expense indicator (red dot)
                                if (hasExpense) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 12.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF5252))
                                    )
                                }

                                // Income indicator (blue dot)
                                if (hasIncome) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 12.dp, end = 5.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF448AFF))
                                    )
                                }
                            }
                            dayCounter++
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastCard(state: CalendarUiState) {
    val forecastSavings = state.totalFixedIncomes - state.totalFixedExpenses - state.forecastExpensesOfMonth
    val today = LocalDate.now()
    val avgDaily = if (today.dayOfMonth > 0) state.totalExpensesOfMonth / today.dayOfMonth else 0.0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(300.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(colors = listOf(GradientBlue, GradientPurple))
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Previsioni Risparmio",
                    color = Color.White,
                    fontSize = 18.sp
                )

                Text(
                    "Le previsioni di risparmio e spese sono calcolate da un algoritmo sulla base delle tue abitudini di consumo rilevate durante il mese in corso.",
                    color = Color.White,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    String.format("\u20AC %.2f", forecastSavings),
                    color = if (forecastSavings < 0) Color(0xFFFF5252) else Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ForecastColumn(
                        icon = Icons.Default.Money,
                        label = "Previsione spesa a fine mese*",
                        value = String.format("\u20AC %.2f", state.forecastExpensesOfMonth)
                    )
                    ForecastColumn(
                        icon = Icons.Default.GraphicEq,
                        label = "Quanto spendi in media al giorno?",
                        value = String.format("\u20AC %.2f", avgDaily)
                    )
                    ForecastColumn(
                        icon = Icons.Default.Lightbulb,
                        label = "Quanto puoi spendere al giorno?",
                        value = String.format("\u20AC %.2f", state.dailyBudget)
                    )
                }
            }
        }
    }
}

@Composable
fun ForecastColumn(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 9.sp, textAlign = TextAlign.Center)
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun PastMonthCard(state: CalendarUiState) {
    val savings = state.totalFixedIncomes - state.totalFixedExpenses - state.totalExpensesOfMonth + state.totalIncomesOfMonth
    val isPositive = savings > 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .height(250.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Risparmio ${state.selectedMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.ITALIAN)}",
                color = Color.Gray,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                String.format("\u20AC%.2f", savings),
                color = Color(0xDD000000),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (isPositive) EntrateCol else SpeseCol,
                modifier = Modifier.size(80.dp)
            )

            Text(
                if (isPositive) "Mese chiuso. Ottimo lavoro!" else "Stai spendendo troppo. Stai più attento!",
                color = Color.Black,
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}
