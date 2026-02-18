package com.example.financeapp.data.database

import androidx.room.TypeConverter
import com.example.financeapp.data.entity.CategoryType
import com.example.financeapp.data.entity.CreditType
import com.example.financeapp.data.entity.TransactionType

class FinanceTypeConverters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType = CategoryType.valueOf(value)

    @TypeConverter
    fun fromCreditType(value: CreditType): String = value.name

    @TypeConverter
    fun toCreditType(value: String): CreditType = CreditType.valueOf(value)
}
