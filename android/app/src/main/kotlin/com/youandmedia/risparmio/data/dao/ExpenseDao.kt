package com.youandmedia.risparmio.data.dao

import androidx.room.*
import com.youandmedia.risparmio.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE date >= :startMillis AND date <= :endMillis")
    fun getByDateRange(startMillis: Long, endMillis: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date >= :startMillis AND date <= :endMillis")
    suspend fun getByDateRangeOnce(startMillis: Long, endMillis: Long): List<Expense>

    @Query("SELECT * FROM expenses")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses")
    suspend fun getAllOnce(): List<Expense>
}
