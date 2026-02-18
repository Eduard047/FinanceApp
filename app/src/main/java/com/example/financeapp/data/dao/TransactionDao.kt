package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: Long)

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getByIdNow(transactionId: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countByCategoryId(categoryId: Long): Int

    @Query(
        """
        SELECT * FROM transactions
        WHERE type = 'CREDIT_PAYMENT'
          AND amount = :amount
          AND date = :paymentDate
          AND note = :note
        ORDER BY id DESC
        LIMIT 1
        """
    )
    suspend fun findCreditPaymentTransactionNow(
        amount: Double,
        paymentDate: Long,
        note: String
    ): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startTimestamp AND :endTimestamp ORDER BY date DESC")
    fun getByMonth(startTimestamp: Long, endTimestamp: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :startTimestamp AND :endTimestamp")
    fun getMonthlyIncomeSum(startTimestamp: Long, endTimestamp: Long): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE (type = 'EXPENSE' OR type = 'CREDIT_PAYMENT')
          AND date BETWEEN :startTimestamp AND :endTimestamp
        """
    )
    fun getMonthlyExpenseSum(startTimestamp: Long, endTimestamp: Long): Flow<Double>
}
