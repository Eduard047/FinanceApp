package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.data.entity.CreditPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditPaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: CreditPaymentEntity): Long

    @Update
    suspend fun update(payment: CreditPaymentEntity)

    @Delete
    suspend fun delete(payment: CreditPaymentEntity)

    @Query("DELETE FROM credit_payments WHERE id = :paymentId")
    suspend fun deleteById(paymentId: Long)

    @Query("SELECT * FROM credit_payments ORDER BY paymentDate DESC")
    fun getAll(): Flow<List<CreditPaymentEntity>>

    @Query("SELECT * FROM credit_payments WHERE paymentDate BETWEEN :startTimestamp AND :endTimestamp ORDER BY paymentDate DESC")
    fun getByMonth(startTimestamp: Long, endTimestamp: Long): Flow<List<CreditPaymentEntity>>

    @Query("SELECT * FROM credit_payments WHERE creditAccountId = :creditId ORDER BY paymentDate DESC")
    fun getCreditPayments(creditId: Long): Flow<List<CreditPaymentEntity>>

    @Query(
        """
        SELECT * FROM credit_payments
        WHERE creditAccountId = :creditId
        ORDER BY paymentDate DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getLatestForCreditNow(creditId: Long): CreditPaymentEntity?
}
