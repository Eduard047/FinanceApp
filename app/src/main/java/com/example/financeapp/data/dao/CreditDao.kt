package com.example.financeapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.data.entity.CreditAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(creditAccount: CreditAccountEntity): Long

    @Update
    suspend fun update(creditAccount: CreditAccountEntity)

    @Delete
    suspend fun delete(creditAccount: CreditAccountEntity)

    @Query("SELECT * FROM credit_accounts ORDER BY startDate DESC")
    fun getAll(): Flow<List<CreditAccountEntity>>

    @Query("SELECT * FROM credit_accounts WHERE startDate BETWEEN :startTimestamp AND :endTimestamp ORDER BY startDate DESC")
    fun getByMonth(startTimestamp: Long, endTimestamp: Long): Flow<List<CreditAccountEntity>>

    @Query("SELECT * FROM credit_accounts WHERE remainingAmount > 0 ORDER BY startDate DESC")
    fun getActiveCredits(): Flow<List<CreditAccountEntity>>

    @Query("SELECT COALESCE(SUM(remainingAmount), 0) FROM credit_accounts WHERE remainingAmount > 0")
    fun getCreditDebtSummary(): Flow<Double>

    @Query("SELECT * FROM credit_accounts WHERE id = :creditId LIMIT 1")
    fun getById(creditId: Long): Flow<CreditAccountEntity?>

    @Query("SELECT * FROM credit_accounts WHERE id = :creditId LIMIT 1")
    suspend fun getByIdNow(creditId: Long): CreditAccountEntity?

    @Query(
        """
        SELECT * FROM credit_accounts
        WHERE remainingAmount > 0
          AND paymentDueDate IS NOT NULL
          AND paymentDueDate <= :untilTimestamp
        ORDER BY paymentDueDate ASC
        """
    )
    suspend fun getDueCreditsUntil(untilTimestamp: Long): List<CreditAccountEntity>
}
