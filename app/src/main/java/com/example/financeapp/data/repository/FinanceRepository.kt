package com.example.financeapp.data.repository

import com.example.financeapp.data.dao.CategoryDao
import com.example.financeapp.data.dao.TransactionDao
import com.example.financeapp.data.entity.CategoryEntity
import com.example.financeapp.data.entity.CategoryType
import com.example.financeapp.data.entity.TransactionEntity
import com.example.financeapp.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Locale

sealed class CategoryMutationResult {
    data class Added(val categoryId: Long) : CategoryMutationResult()
    object Deleted : CategoryMutationResult()
    object Duplicate : CategoryMutationResult()
    object InUse : CategoryMutationResult()
    object InvalidName : CategoryMutationResult()
    object NotFound : CategoryMutationResult()
}

class FinanceRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    private val defaultCategories = listOf(
        CategoryEntity(name = "Зарплата", type = CategoryType.INCOME),
        CategoryEntity(name = "Фріланс", type = CategoryType.INCOME),
        CategoryEntity(name = "Оренда", type = CategoryType.EXPENSE),
        CategoryEntity(name = "Продукти", type = CategoryType.EXPENSE),
        CategoryEntity(name = "Транспорт", type = CategoryType.EXPENSE),
        CategoryEntity(name = "Кредитна картка", type = CategoryType.CREDIT),
        CategoryEntity(name = "Позика", type = CategoryType.CREDIT)
    )

    private val legacyCategoryTranslations = mapOf(
        "salary" to ("Зарплата" to CategoryType.INCOME),
        "freelance" to ("Фріланс" to CategoryType.INCOME),
        "rent" to ("Оренда" to CategoryType.EXPENSE),
        "groceries" to ("Продукти" to CategoryType.EXPENSE),
        "transport" to ("Транспорт" to CategoryType.EXPENSE),
        "credit card" to ("Кредитна картка" to CategoryType.CREDIT),
        "loan" to ("Позика" to CategoryType.CREDIT)
    )

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    fun getCategoriesByType(type: CategoryType): Flow<List<CategoryEntity>> = categoryDao.getByType(type)

    fun getTransactionsByMonth(startTimestamp: Long, endTimestamp: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getByMonth(startTimestamp, endTimestamp)
    }

    fun getMonthlyIncomeSum(startTimestamp: Long, endTimestamp: Long): Flow<Double> {
        return transactionDao.getMonthlyIncomeSum(startTimestamp, endTimestamp)
    }

    fun getMonthlyExpenseSum(startTimestamp: Long, endTimestamp: Long): Flow<Double> {
        return transactionDao.getMonthlyExpenseSum(startTimestamp, endTimestamp)
    }

    suspend fun addTransaction(
        amount: Double,
        date: Long,
        categoryId: Long,
        type: TransactionType,
        note: String?
    ) {
        transactionDao.insert(
            TransactionEntity(
                amount = amount,
                date = date,
                categoryId = categoryId,
                type = type,
                note = note
            )
        )
    }

    suspend fun addCategory(name: String, type: CategoryType): CategoryMutationResult {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            return CategoryMutationResult.InvalidName
        }

        val existing = categoryDao.getByNameAndTypeNow(normalizedName, type)
        if (existing != null) {
            return CategoryMutationResult.Duplicate
        }

        val categoryId = categoryDao.insert(
            CategoryEntity(
                name = normalizedName,
                type = type
            )
        )

        return CategoryMutationResult.Added(categoryId)
    }

    suspend fun deleteCategory(categoryId: Long): CategoryMutationResult {
        val category = categoryDao.getByIdNow(categoryId) ?: return CategoryMutationResult.NotFound
        val usageCount = transactionDao.countByCategoryId(category.id)
        if (usageCount > 0) {
            return CategoryMutationResult.InUse
        }

        categoryDao.deleteById(categoryId)
        return CategoryMutationResult.Deleted
    }

    suspend fun deleteTransaction(transactionId: Long) {
        transactionDao.deleteById(transactionId)
    }

    suspend fun deleteTransactionWithUndo(transactionId: Long): TransactionEntity? {
        val transaction = transactionDao.getByIdNow(transactionId) ?: return null
        transactionDao.deleteById(transactionId)
        return transaction
    }

    suspend fun restoreTransaction(transaction: TransactionEntity) {
        transactionDao.insert(transaction.copy(id = 0L))
    }

    suspend fun ensureDefaultCategories() {
        normalizeLegacyCategoryNames()
        val existing = categoryDao.getAllNow()

        defaultCategories.forEach { defaultCategory ->
            val alreadyExists = existing.any {
                it.type == defaultCategory.type &&
                    normalizedKey(it.name) == normalizedKey(defaultCategory.name)
            }
            if (!alreadyExists) {
                categoryDao.insert(defaultCategory)
            }
        }
    }

    private suspend fun normalizeLegacyCategoryNames() {
        val categories = categoryDao.getAllNow()
        categories.forEach { category ->
            val translation = legacyCategoryTranslations[normalizedKey(category.name)] ?: return@forEach
            val translatedName = translation.first
            val translatedType = translation.second
            if (category.type != translatedType) {
                return@forEach
            }
            if (normalizedKey(category.name) == normalizedKey(translatedName)) {
                return@forEach
            }

            val localizedExisting = categoryDao.getByNameAndTypeNow(translatedName, translatedType)
            if (localizedExisting != null && localizedExisting.id != category.id) {
                val usageCount = transactionDao.countByCategoryId(category.id)
                if (usageCount == 0) {
                    categoryDao.deleteById(category.id)
                }
            } else {
                categoryDao.update(category.copy(name = translatedName))
            }
        }
    }

    private fun normalizedKey(value: String): String {
        return value.trim().lowercase(Locale.ROOT)
    }
}
