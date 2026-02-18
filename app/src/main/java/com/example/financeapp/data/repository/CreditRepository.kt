package com.example.financeapp.data.repository

import androidx.room.withTransaction
import com.example.financeapp.data.dao.CreditDao
import com.example.financeapp.data.dao.CreditPaymentDao
import com.example.financeapp.data.database.FinanceDatabase
import com.example.financeapp.data.entity.CreditAccountEntity
import com.example.financeapp.data.entity.CreditPaymentEntity
import com.example.financeapp.data.entity.CreditType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class CreditRepository(
    private val database: FinanceDatabase,
    private val creditDao: CreditDao,
    private val creditPaymentDao: CreditPaymentDao
) {
    fun getAllCredits(): Flow<List<CreditAccountEntity>> = creditDao.getAll()

    fun getActiveCredits(): Flow<List<CreditAccountEntity>> = creditDao.getActiveCredits()

    fun getCreditDebtSummary(): Flow<Double> = creditDao.getCreditDebtSummary()

    fun getCreditById(creditId: Long): Flow<CreditAccountEntity?> = creditDao.getById(creditId)

    fun getCreditPayments(creditId: Long): Flow<List<CreditPaymentEntity>> {
        return creditPaymentDao.getCreditPayments(creditId)
    }

    suspend fun getDueCreditsUntil(untilTimestamp: Long): List<CreditAccountEntity> {
        return creditDao.getDueCreditsUntil(untilTimestamp)
    }

    suspend fun addCreditAccount(
        name: String,
        creditType: CreditType,
        totalAmount: Double,
        monthlyPayment: Double?,
        installmentCount: Int?,
        paymentDueDate: Long?,
        interestRate: Double?,
        startDate: Long,
        endDate: Long?
    ) {
        val isInstallment = creditType == CreditType.INSTALLMENT || creditType == CreditType.PAY_IN_PARTS
        val normalizedInstallmentCount = if (isInstallment) installmentCount else null

        val normalizedMonthlyPayment = when {
            isInstallment && normalizedInstallmentCount != null && normalizedInstallmentCount > 0 -> {
                totalAmount / normalizedInstallmentCount
            }

            else -> monthlyPayment
        }

        creditDao.insert(
            CreditAccountEntity(
                name = name,
                creditType = creditType,
                totalAmount = totalAmount,
                remainingAmount = totalAmount,
                monthlyPayment = normalizedMonthlyPayment,
                interestRate = interestRate,
                startDate = startDate,
                endDate = endDate,
                installmentCount = normalizedInstallmentCount,
                paidInstallments = 0,
                paymentDueDate = paymentDueDate
            )
        )
    }

    suspend fun addPayment(
        creditId: Long,
        amount: Double,
        paymentDate: Long
    ) {
        if (amount <= 0) {
            return
        }

        database.withTransaction {
            val credit = creditDao.getByIdNow(creditId) ?: return@withTransaction

            creditPaymentDao.insert(
                CreditPaymentEntity(
                    creditAccountId = creditId,
                    amount = amount,
                    paymentDate = paymentDate
                )
            )

            val updatedRemaining = (credit.remainingAmount - amount).coerceAtLeast(0.0)
            creditDao.update(
                credit.copy(
                    remainingAmount = updatedRemaining,
                    paymentDueDate = if (updatedRemaining <= 0.0) null else credit.paymentDueDate
                )
            )
        }
    }

    suspend fun markNextInstallmentAsPaid(creditId: Long, paymentDate: Long) {
        database.withTransaction {
            val credit = creditDao.getByIdNow(creditId) ?: return@withTransaction
            val isInstallment = credit.creditType == CreditType.INSTALLMENT || credit.creditType == CreditType.PAY_IN_PARTS
            if (!isInstallment) {
                return@withTransaction
            }

            val totalInstallments = credit.installmentCount ?: return@withTransaction
            if (credit.paidInstallments >= totalInstallments || credit.remainingAmount <= 0.0) {
                return@withTransaction
            }

            val defaultInstallmentAmount = if (totalInstallments > 0) {
                credit.totalAmount / totalInstallments
            } else {
                credit.remainingAmount
            }

            val paymentAmount = (credit.monthlyPayment ?: defaultInstallmentAmount)
                .coerceAtMost(credit.remainingAmount)

            creditPaymentDao.insert(
                CreditPaymentEntity(
                    creditAccountId = creditId,
                    amount = paymentAmount,
                    paymentDate = paymentDate
                )
            )

            val updatedRemaining = (credit.remainingAmount - paymentAmount).coerceAtLeast(0.0)
            val updatedPaidInstallments = (credit.paidInstallments + 1).coerceAtMost(totalInstallments)

            val updatedDueDate = if (updatedRemaining <= 0.0 || updatedPaidInstallments >= totalInstallments) {
                null
            } else {
                plusOneMonth(credit.paymentDueDate ?: paymentDate)
            }

            creditDao.update(
                credit.copy(
                    remainingAmount = updatedRemaining,
                    paidInstallments = updatedPaidInstallments,
                    paymentDueDate = updatedDueDate
                )
            )
        }
    }

    suspend fun undoLastPayment(creditId: Long): Boolean {
        var reverted = false

        database.withTransaction {
            val credit = creditDao.getByIdNow(creditId) ?: return@withTransaction
            val lastPayment = creditPaymentDao.getLatestForCreditNow(creditId) ?: return@withTransaction

            creditPaymentDao.deleteById(lastPayment.id)

            val updatedRemaining = (credit.remainingAmount + lastPayment.amount).coerceAtMost(credit.totalAmount)
            val isInstallment = credit.creditType == CreditType.INSTALLMENT || credit.creditType == CreditType.PAY_IN_PARTS
            val totalInstallments = credit.installmentCount ?: 0

            val updatedPaidInstallments = if (isInstallment) {
                (credit.paidInstallments - 1).coerceAtLeast(0)
            } else {
                credit.paidInstallments
            }

            val updatedDueDate = when {
                updatedRemaining <= 0.0 -> null
                isInstallment -> {
                    if (credit.paymentDueDate != null) {
                        minusOneMonth(credit.paymentDueDate)
                    } else {
                        lastPayment.paymentDate
                    }
                }

                else -> {
                    credit.paymentDueDate ?: lastPayment.paymentDate
                }
            }

            creditDao.update(
                credit.copy(
                    remainingAmount = updatedRemaining,
                    paidInstallments = if (isInstallment) {
                        updatedPaidInstallments.coerceAtMost(totalInstallments)
                    } else {
                        updatedPaidInstallments
                    },
                    paymentDueDate = updatedDueDate
                )
            )

            reverted = true
        }

        return reverted
    }

    private fun plusOneMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.add(Calendar.MONTH, 1)
        return calendar.timeInMillis
    }

    private fun minusOneMonth(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.add(Calendar.MONTH, -1)
        return calendar.timeInMillis
    }
}
