package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.entity.CategoryEntity
import com.example.financeapp.data.entity.CategoryType
import com.example.financeapp.data.entity.TransactionEntity
import com.example.financeapp.data.entity.TransactionType
import com.example.financeapp.data.repository.CategoryMutationResult
import com.example.financeapp.data.repository.FinanceRepository
import com.example.financeapp.domain.currentMonthRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionListItem(
    val id: Long,
    val amount: Double,
    val date: Long,
    val categoryName: String,
    val type: TransactionType,
    val note: String?
)

data class AddTransactionFormState(
    val amount: String = "",
    val note: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategoryId: Long? = null,
    val errorMessage: String? = null
)

data class CategoryActionState(
    val message: String? = null,
    val isError: Boolean = false
)

class TransactionViewModel(
    private val financeRepository: FinanceRepository
) : ViewModel() {
    private val monthRange = currentMonthRange()
    private var lastDeletedTransaction: TransactionEntity? = null

    private val _formState = MutableStateFlow(AddTransactionFormState())
    val formState: StateFlow<AddTransactionFormState> = _formState.asStateFlow()

    private val _canUndoDelete = MutableStateFlow(false)
    val canUndoDelete: StateFlow<Boolean> = _canUndoDelete.asStateFlow()

    private val _categoryActionState = MutableStateFlow(CategoryActionState())
    val categoryActionState: StateFlow<CategoryActionState> = _categoryActionState.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = financeRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val filteredCategories: StateFlow<List<CategoryEntity>> = combine(
        categories,
        formState
    ) { categoryList, form ->
        val targetCategoryType = when (form.type) {
            TransactionType.INCOME -> CategoryType.INCOME
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.CREDIT_PAYMENT -> CategoryType.CREDIT
        }
        categoryList.filter { it.type == targetCategoryType }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<TransactionListItem>> = combine(
        financeRepository.getTransactionsByMonth(monthRange.start, monthRange.end),
        categories
    ) { transactionList, categoryList ->
        val categoryNames = categoryList.associate { it.id to it.name }

        transactionList.map { transaction ->
            TransactionListItem(
                id = transaction.id,
                amount = transaction.amount,
                date = transaction.date,
                categoryName = categoryNames[transaction.categoryId] ?: "—",
                type = transaction.type,
                note = transaction.note
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            financeRepository.ensureDefaultCategories()
        }
    }

    fun onAmountChanged(value: String) {
        _formState.update { it.copy(amount = value, errorMessage = null) }
    }

    fun onNoteChanged(value: String) {
        _formState.update { it.copy(note = value, errorMessage = null) }
    }

    fun onTypeChanged(value: TransactionType) {
        _formState.update {
            it.copy(
                type = value,
                selectedCategoryId = null,
                errorMessage = null
            )
        }
        clearCategoryActionMessage()
    }

    fun onCategorySelected(categoryId: Long) {
        _formState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    fun saveTransaction(onSaved: () -> Unit = {}) {
        val form = _formState.value
        val amount = form.amount.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _formState.update { it.copy(errorMessage = "Введіть коректну суму") }
            return
        }

        val categoryId = form.selectedCategoryId
        if (categoryId == null) {
            _formState.update { it.copy(errorMessage = "Оберіть категорію") }
            return
        }

        viewModelScope.launch {
            financeRepository.addTransaction(
                amount = amount,
                date = System.currentTimeMillis(),
                categoryId = categoryId,
                type = form.type,
                note = form.note.takeIf { it.isNotBlank() }
            )

            _formState.value = AddTransactionFormState(type = form.type)
            onSaved()
        }
    }

    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            val deleted = financeRepository.deleteTransactionWithUndo(transactionId)
            lastDeletedTransaction = deleted
            _canUndoDelete.value = deleted != null
        }
    }

    fun undoDeleteTransaction() {
        viewModelScope.launch {
            val transactionToRestore = lastDeletedTransaction ?: return@launch
            financeRepository.restoreTransaction(transactionToRestore)
            lastDeletedTransaction = null
            _canUndoDelete.value = false
        }
    }

    fun addCategory(name: String) {
        val targetCategoryType = formTypeToCategoryType(_formState.value.type)
        viewModelScope.launch {
            when (val result = financeRepository.addCategory(name, targetCategoryType)) {
                is CategoryMutationResult.Added -> {
                    _formState.update {
                        it.copy(
                            selectedCategoryId = result.categoryId,
                            errorMessage = null
                        )
                    }
                    _categoryActionState.value = CategoryActionState(
                        message = "Категорію додано",
                        isError = false
                    )
                }

                CategoryMutationResult.Duplicate -> {
                    _categoryActionState.value = CategoryActionState(
                        message = "Така категорія вже існує",
                        isError = true
                    )
                }

                CategoryMutationResult.InvalidName -> {
                    _categoryActionState.value = CategoryActionState(
                        message = "Введіть назву категорії",
                        isError = true
                    )
                }

                else -> {
                    _categoryActionState.value = CategoryActionState(
                        message = "Не вдалося додати категорію",
                        isError = true
                    )
                }
            }
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            when (financeRepository.deleteCategory(categoryId)) {
                CategoryMutationResult.Deleted -> {
                    if (_formState.value.selectedCategoryId == categoryId) {
                        _formState.update { it.copy(selectedCategoryId = null) }
                    }
                    _categoryActionState.value = CategoryActionState(
                        message = "Категорію видалено",
                        isError = false
                    )
                }

                CategoryMutationResult.InUse -> {
                    _categoryActionState.value = CategoryActionState(
                        message = "Категорія вже використовується в операціях",
                        isError = true
                    )
                }

                CategoryMutationResult.NotFound -> {
                    _categoryActionState.value = CategoryActionState(
                        message = "Категорію не знайдено",
                        isError = true
                    )
                }

                else -> {
                    _categoryActionState.value = CategoryActionState(
                        message = "Не вдалося видалити категорію",
                        isError = true
                    )
                }
            }
        }
    }

    fun clearCategoryActionMessage() {
        _categoryActionState.value = CategoryActionState()
    }

    private fun formTypeToCategoryType(type: TransactionType): CategoryType {
        return when (type) {
            TransactionType.INCOME -> CategoryType.INCOME
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.CREDIT_PAYMENT -> CategoryType.CREDIT
        }
    }
}

class TransactionViewModelFactory(
    private val financeRepository: FinanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(financeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
