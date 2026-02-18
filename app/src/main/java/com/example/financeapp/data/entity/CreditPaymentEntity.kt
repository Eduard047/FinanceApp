package com.example.financeapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_payments",
    foreignKeys = [
        ForeignKey(
            entity = CreditAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditAccountId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["creditAccountId"]),
        Index(value = ["paymentDate"])
    ]
)
data class CreditPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val creditAccountId: Long,
    val amount: Double,
    val paymentDate: Long
)
