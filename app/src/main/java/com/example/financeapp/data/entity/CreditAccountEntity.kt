package com.example.financeapp.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_accounts",
    indices = [
        Index(value = ["remainingAmount"]),
        Index(value = ["creditType"]),
        Index(value = ["paymentDueDate"])
    ]
)
data class CreditAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val creditType: CreditType,
    val totalAmount: Double,
    val remainingAmount: Double,
    val monthlyPayment: Double?,
    val interestRate: Double?,
    val startDate: Long,
    val endDate: Long?,
    val installmentCount: Int? = null,
    val paidInstallments: Int = 0,
    val paymentDueDate: Long? = null,
    val note: String? = null
)
