package com.youandmedia.risparmio.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.youandmedia.risparmio.RisparmioApp
import com.youandmedia.risparmio.data.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CalendarUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val datesWithExpenses: Set<LocalDate> = emptySet(),
    val datesWithIncomes: Set<LocalDate> = emptySet(),
    val totalFixedIncomes: Double = 0.0,
    val totalFixedExpenses: Double = 0.0,
    val totalExpensesOfMonth: Double = 0.0,
    val totalIncomesOfMonth: Double = 0.0,
    val forecastExpensesOfMonth: Double = 0.0,
    val dailyBudget: Double = 0.0,
    val dailyExpenses: Map<LocalDate, Double> = emptyMap()
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as RisparmioApp).database

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    init {
        loadData()
    }

    fun onMonthChanged(yearMonth: YearMonth) {
        _uiState.value = _uiState.value.copy(selectedMonth = yearMonth)
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val month = _uiState.value.selectedMonth
            val startOfMonth = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonth = month.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val expenses = db.expenseDao().getByDateRangeOnce(startOfMonth, endOfMonth)
            val incomes = db.incomeDao().getByDateRangeOnce(startOfMonth, endOfMonth)
            val fixedIncomes = db.fixedIncomeDao().getAllOnce()
            val fixedExpenses = db.fixedExpenseDao().getAllOnce()

            val datesWithExp = expenses.map { epochToLocalDate(it.date) }.toSet()
            val datesWithInc = incomes.map { epochToLocalDate(it.date) }.toSet()

            val totalFixedInc = fixedIncomes.sumOf { it.amount }
            val totalFixedExp = fixedExpenses.sumOf { it.amount }
            val totalExpMonth = expenses.sumOf { it.amount }
            val totalIncMonth = incomes.sumOf { it.amount }

            val forecast = calculateForecast(expenses, month)

            val selectedDaysInMonth = month.lengthOfMonth()
            val dailyBudget = if (selectedDaysInMonth > 0) (totalFixedInc - totalFixedExp) / selectedDaysInMonth else 0.0

            // Per-day expense totals for budget coloring
            val dailyExp = expenses.groupBy { epochToLocalDate(it.date) }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            _uiState.value = _uiState.value.copy(
                datesWithExpenses = datesWithExp,
                datesWithIncomes = datesWithInc,
                totalFixedIncomes = totalFixedInc,
                totalFixedExpenses = totalFixedExp,
                totalExpensesOfMonth = totalExpMonth,
                totalIncomesOfMonth = totalIncMonth,
                forecastExpensesOfMonth = forecast,
                dailyBudget = dailyBudget,
                dailyExpenses = dailyExp
            )
        }
    }

    // Quick add expense directly from calendar
    fun quickAddExpense(amount: Double, category: String, description: String, date: LocalDate) {
        viewModelScope.launch {
            val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            db.expenseDao().insert(
                com.youandmedia.risparmio.data.model.Expense(
                    amount = amount, category = category, description = description, date = millis
                )
            )
            loadData()
        }
    }

    fun quickAddIncome(amount: Double, category: String, description: String, date: LocalDate) {
        viewModelScope.launch {
            val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            db.incomeDao().insert(
                com.youandmedia.risparmio.data.model.Income(
                    amount = amount, category = category, description = description, date = millis
                )
            )
            loadData()
        }
    }

    private fun calculateForecast(expenses: List<Expense>, month: YearMonth): Double {
        val now = LocalDate.now()
        if (month != YearMonth.now() || now.dayOfMonth == 0) return 0.0

        val todayEnd = now.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val startOfMonth = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val expensesUntilToday = expenses.filter { it.date <= todayEnd && it.date >= startOfMonth }
        val totalUntilToday = expensesUntilToday.sumOf { it.amount }

        val daysPassed = now.dayOfMonth
        val totalDays = month.lengthOfMonth()

        return if (daysPassed > 0) totalUntilToday / daysPassed * totalDays else 0.0
    }

    private fun epochToLocalDate(epochMillis: Long): LocalDate {
        return java.time.Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    fun isCurrentMonth(): Boolean {
        return _uiState.value.selectedMonth == YearMonth.now()
    }
}
