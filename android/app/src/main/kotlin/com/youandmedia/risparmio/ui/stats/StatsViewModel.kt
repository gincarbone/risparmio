package com.youandmedia.risparmio.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.youandmedia.risparmio.RisparmioApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CategoryStat(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class MonthTrend(
    val month: YearMonth,
    val totalExpenses: Double,
    val totalIncomes: Double
)

data class StatsUiState(
    val categoryStats: List<CategoryStat> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalIncomes: Double = 0.0,
    val totalFixedIncomes: Double = 0.0,
    val totalFixedExpenses: Double = 0.0,
    val selectedMonth: YearMonth = YearMonth.now(),
    val isAllTime: Boolean = true,
    val topCategory: String = "",
    val avgDailyExpense: Double = 0.0,
    val transactionCount: Int = 0,
    val monthTrends: List<MonthTrend> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as RisparmioApp).database

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        loadMonthData(YearMonth.now())
    }

    fun loadMonthData(month: YearMonth) {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val startMillis = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val endMillis = month.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()

            val expenses = db.expenseDao().getByDateRangeOnce(startMillis, endMillis)
            val incomes = db.incomeDao().getByDateRangeOnce(startMillis, endMillis)
            val fixedIncomes = db.fixedIncomeDao().getAllOnce()
            val fixedExpenses = db.fixedExpenseDao().getAllOnce()

            val categoryMap = mutableMapOf<String, Pair<Double, Int>>()
            var total = 0.0

            for (expense in expenses) {
                total += expense.amount
                val current = categoryMap[expense.category] ?: Pair(0.0, 0)
                categoryMap[expense.category] = Pair(current.first + expense.amount, current.second + 1)
            }

            val stats = categoryMap.entries
                .map { (cat, pair) ->
                    CategoryStat(
                        category = cat,
                        amount = pair.first,
                        percentage = if (total > 0) (pair.first / total * 100).toFloat() else 0f,
                        transactionCount = pair.second
                    )
                }
                .sortedByDescending { it.amount }

            val daysInMonth = month.lengthOfMonth()
            val daysPassed = if (month == YearMonth.now()) LocalDate.now().dayOfMonth else daysInMonth

            // Load 6-month trend data
            val trends = mutableListOf<MonthTrend>()
            for (i in 5 downTo 0) {
                val trendMonth = month.minusMonths(i.toLong())
                val tStart = trendMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val tEnd = trendMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
                val tExpenses = db.expenseDao().getByDateRangeOnce(tStart, tEnd)
                val tIncomes = db.incomeDao().getByDateRangeOnce(tStart, tEnd)
                trends.add(MonthTrend(
                    month = trendMonth,
                    totalExpenses = tExpenses.sumOf { it.amount },
                    totalIncomes = tIncomes.sumOf { it.amount }
                ))
            }

            _uiState.value = StatsUiState(
                categoryStats = stats,
                totalSpent = total,
                totalIncomes = incomes.sumOf { it.amount },
                totalFixedIncomes = fixedIncomes.sumOf { it.amount },
                totalFixedExpenses = fixedExpenses.sumOf { it.amount },
                selectedMonth = month,
                isAllTime = false,
                topCategory = stats.firstOrNull()?.category ?: "-",
                avgDailyExpense = if (daysPassed > 0) total / daysPassed else 0.0,
                transactionCount = expenses.size,
                monthTrends = trends
            )
        }
    }

    fun nextMonth() {
        loadMonthData(_uiState.value.selectedMonth.plusMonths(1))
    }

    fun prevMonth() {
        loadMonthData(_uiState.value.selectedMonth.minusMonths(1))
    }
}
