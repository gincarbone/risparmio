package com.youandmedia.risparmio.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.youandmedia.risparmio.data.dao.*
import com.youandmedia.risparmio.data.model.*

@Database(
    entities = [Expense::class, Income::class, FixedIncome::class, FixedExpense::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun fixedIncomeDao(): FixedIncomeDao
    abstract fun fixedExpenseDao(): FixedExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "risparmio_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
