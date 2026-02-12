package com.youandmedia.risparmio.data.dao

import androidx.room.*
import com.youandmedia.risparmio.data.model.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert
    suspend fun insert(income: Income)

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Query("SELECT * FROM incomes WHERE date >= :startMillis AND date <= :endMillis")
    fun getByDateRange(startMillis: Long, endMillis: Long): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE date >= :startMillis AND date <= :endMillis")
    suspend fun getByDateRangeOnce(startMillis: Long, endMillis: Long): List<Income>

    @Query("SELECT * FROM incomes")
    fun getAll(): Flow<List<Income>>

    @Query("SELECT * FROM incomes")
    suspend fun getAllOnce(): List<Income>
}
