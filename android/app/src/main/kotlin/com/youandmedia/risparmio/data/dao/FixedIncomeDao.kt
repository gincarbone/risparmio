package com.youandmedia.risparmio.data.dao

import androidx.room.*
import com.youandmedia.risparmio.data.model.FixedIncome
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedIncomeDao {
    @Insert
    suspend fun insert(fixedIncome: FixedIncome)

    @Update
    suspend fun update(fixedIncome: FixedIncome)

    @Delete
    suspend fun delete(fixedIncome: FixedIncome)

    @Query("SELECT * FROM fixed_incomes")
    fun getAll(): Flow<List<FixedIncome>>

    @Query("SELECT * FROM fixed_incomes")
    suspend fun getAllOnce(): List<FixedIncome>
}
