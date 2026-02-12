package com.youandmedia.risparmio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fixed_incomes")
data class FixedIncome(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val amount: Double
)
