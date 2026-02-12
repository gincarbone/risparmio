package com.youandmedia.risparmio.data.dao

import androidx.room.*
import com.youandmedia.risparmio.data.model.FixedExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedExpenseDao {
    @Insert
    suspend fun insert(fixedExpense: FixedExpense)

    @Update
    suspend fun update(fixedExpense: FixedExpense)

    @Delete
    suspend fun delete(fixedExpense: FixedExpense)

    @Query("SELECT * FROM fixed_expenses")
    fun getAll(): Flow<List<FixedExpense>>

    @Query("SELECT * FROM fixed_expenses")
    suspend fun getAllOnce(): List<FixedExpense>
}
