package com.youandmedia.risparmio.ui.daydetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.youandmedia.risparmio.RisparmioApp
import com.youandmedia.risparmio.data.model.Expense
import com.youandmedia.risparmio.data.model.Income
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class DayDetailUiState(
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList()
)

class DayDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as RisparmioApp).database

    private val _uiState = MutableStateFlow(DayDetailUiState())
    val uiState: StateFlow<DayDetailUiState> = _uiState

    fun loadDay(date: LocalDate) {
        viewModelScope.launch {
            val startMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val expenses = db.expenseDao().getByDateRangeOnce(startMillis, endMillis)
            val incomes = db.incomeDao().getByDateRangeOnce(startMillis, endMillis)

            _uiState.value = DayDetailUiState(expenses = expenses, incomes = incomes)
        }
    }

    fun saveExpense(amount: Double, category: String, description: String, date: LocalDate) {
        viewModelScope.launch {
            val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            db.expenseDao().insert(Expense(amount = amount, category = category, description = description, date = millis))
            loadDay(date)
        }
    }

    fun updateExpense(expense: Expense, amount: Double, category: String, description: String, date: LocalDate) {
        viewModelScope.launch {
            db.expenseDao().update(expense.copy(amount = amount, category = category, description = description))
            loadDay(date)
        }
    }

    fun deleteExpense(expense: Expense, date: LocalDate) {
        viewModelScope.launch {
            db.expenseDao().delete(expense)
            loadDay(date)
        }
    }

    fun saveIncome(amount: Double, category: String, description: String, date: LocalDate) {
        viewModelScope.launch {
            val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            db.incomeDao().insert(Income(amount = amount, category = category, description = description, date = millis))
            loadDay(date)
        }
    }

    fun updateIncome(income: Income, amount: Double, category: String, description: String, date: LocalDate) {
        viewModelScope.launch {
            db.incomeDao().update(income.copy(amount = amount, category = category, description = description))
            loadDay(date)
        }
    }

    fun deleteIncome(income: Income, date: LocalDate) {
        viewModelScope.launch {
            db.incomeDao().delete(income)
            loadDay(date)
        }
    }
}
