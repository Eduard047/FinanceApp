package com.example.financeapp.data.repository

import androidx.room.withTransaction
import com.example.financeapp.data.dao.CategoryDao
import com.example.financeapp.data.dao.CreditDao
import com.example.financeapp.data.dao.CreditPaymentDao
import com.example.financeapp.data.dao.TransactionDao
import com.example.financeapp.data.database.FinanceDatabase
import com.example.financeapp.data.entity.CategoryEntity
import com.example.financeapp.data.entity.CategoryType
import com.example.financeapp.data.entity.CreditAccountEntity
import com.example.financeapp.data.entity.CreditPaymentEntity
import com.example.financeapp.data.entity.CreditType
import com.example.financeapp.data.entity.TransactionEntity
import com.example.financeapp.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class CreditRepository(
    private val database: FinanceDatabase,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
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
        note: String?,
        initialPaidAmount: Double?,
        startDate: Long,
        endDate: Long?
    ) {
        val isInstallment = creditType == CreditType.INSTALLMENT || creditType == CreditType.PAY_IN_PARTS
        val isCreditLimit = creditType == CreditType.CREDIT_LIMIT
        val normalizedInstallmentCount = if (isInstallment) installmentCount else null
        val normalizedInitialPaidAmount = if (isCreditLimit) {
            (initialPaidAmount ?: 0.0).coerceIn(0.0, totalAmount)
        } else {
            0.0
        }
        val normalizedRemainingAmount = (totalAmount - normalizedInitialPaidAmount).coerceAtLeast(0.0)

        val normalizedMonthlyPayment = when {
            isCreditLimit -> null
            isInstallment && normalizedInstallmentCount != null && normalizedInstallmentCount > 0 -> {
                totalAmount / normalizedInstallmentCount
            }

            else -> monthlyPayment
        }

        val insertedCreditId = creditDao.insert(
            CreditAccountEntity(
                name = name,
                creditType = creditType,
                totalAmount = totalAmount,
                remainingAmount = normalizedRemainingAmount,
                monthlyPayment = normalizedMonthlyPayment,
                interestRate = if (isCreditLimit) null else interestRate,
                startDate = startDate,
                endDate = endDate,
                installmentCount = normalizedInstallmentCount,
                paidInstallments = 0,
                paymentDueDate = if (isCreditLimit) null else paymentDueDate,
                note = note
            )
        )

        if (isCreditLimit && normalizedInitialPaidAmount > 0.0) {
            creditPaymentDao.insert(
                CreditPaymentEntity(
                    creditAccountId = insertedCreditId,
                    amount = normalizedInitialPaidAmount,
                    paymentDate = startDate
                )
            )
        }
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

            if (credit.creditType != CreditType.CREDIT_LIMIT) {
                addExpenseTransactionForCreditPayment(
                    credit = credit,
                    paymentAmount = amount,
                    paymentDate = paymentDate
                )
            }
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

            addExpenseTransactionForCreditPayment(
                credit = credit,
                paymentAmount = paymentAmount,
                paymentDate = paymentDate
            )
        }
    }

    suspend fun undoLastPayment(creditId: Long): Boolean {
        var reverted = false

        database.withTransaction {
            val credit = creditDao.getByIdNow(creditId) ?: return@withTransaction
            val lastPayment = creditPaymentDao.getLatestForCreditNow(creditId) ?: return@withTransaction

            if (credit.creditType != CreditType.CREDIT_LIMIT) {
                removeExpenseTransactionForCreditPayment(
                    credit = credit,
                    paymentAmount = lastPayment.amount,
                    paymentDate = lastPayment.paymentDate
                )
            }

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

    private suspend fun ensureCreditPaymentCategoryId(): Long {
        val existing = categoryDao.getAllNow().firstOrNull { it.type == CategoryType.CREDIT }
        if (existing != null) {
            return existing.id
        }

        return categoryDao.insert(
            CategoryEntity(
                name = "Кредитні платежі",
                type = CategoryType.CREDIT
            )
        )
    }

    private suspend fun addExpenseTransactionForCreditPayment(
        credit: CreditAccountEntity,
        paymentAmount: Double,
        paymentDate: Long
    ) {
        val categoryId = ensureCreditPaymentCategoryId()
        transactionDao.insert(
            TransactionEntity(
                amount = paymentAmount,
                date = paymentDate,
                categoryId = categoryId,
                type = TransactionType.CREDIT_PAYMENT,
                note = paymentNoteForCredit(credit.name)
            )
        )
    }

    private suspend fun removeExpenseTransactionForCreditPayment(
        credit: CreditAccountEntity,
        paymentAmount: Double,
        paymentDate: Long
    ) {
        val transaction = transactionDao.findCreditPaymentTransactionNow(
            amount = paymentAmount,
            paymentDate = paymentDate,
            note = paymentNoteForCredit(credit.name)
        ) ?: return

        transactionDao.deleteById(transaction.id)
    }

    private fun paymentNoteForCredit(creditName: String): String {
        return "Платіж за кредитом: $creditName"
    }
}
