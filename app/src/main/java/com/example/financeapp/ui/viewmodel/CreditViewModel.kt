package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.entity.CreditAccountEntity
import com.example.financeapp.data.entity.CreditPaymentEntity
import com.example.financeapp.data.entity.CreditType
import com.example.financeapp.data.repository.CreditRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.min

data class AddCreditFormState(
    val name: String = "",
    val creditType: CreditType = CreditType.INSTALLMENT,
    val totalAmount: String = "",
    val installmentCount: String = "",
    val paymentDueDay: Int? = null,
    val monthlyPayment: String = "",
    val interestRate: String = "",
    val note: String = "",
    val alreadyPaidAmount: String = "",
    val errorMessage: String? = null
)

class CreditViewModel(
    private val creditRepository: CreditRepository
) : ViewModel() {
    private val _addCreditFormState = MutableStateFlow(AddCreditFormState())
    val addCreditFormState: StateFlow<AddCreditFormState> = _addCreditFormState.asStateFlow()

    val creditAccounts: StateFlow<List<CreditAccountEntity>> = creditRepository.getAllCredits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun creditById(creditId: Long): Flow<CreditAccountEntity?> = creditRepository.getCreditById(creditId)

    fun getCreditPayments(creditId: Long): Flow<List<CreditPaymentEntity>> {
        return creditRepository.getCreditPayments(creditId)
    }

    fun isInstallmentPlan(type: CreditType): Boolean {
        return type == CreditType.INSTALLMENT || type == CreditType.PAY_IN_PARTS
    }

    fun isCreditLimit(type: CreditType): Boolean {
        return type == CreditType.CREDIT_LIMIT
    }

    fun installmentPaymentPreview(): Double? {
        val form = _addCreditFormState.value
        val totalAmount = form.totalAmount.toDoubleOrNull()
        val installmentCount = form.installmentCount.toIntOrNull()

        if (totalAmount == null || totalAmount <= 0.0 || installmentCount == null || installmentCount <= 0) {
            return null
        }

        return totalAmount / installmentCount
    }

    fun onNameChanged(value: String) {
        _addCreditFormState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onCreditTypeChanged(value: CreditType) {
        _addCreditFormState.update {
            it.copy(
                creditType = value,
                paymentDueDay = if (value == CreditType.CREDIT_LIMIT) null else it.paymentDueDay,
                installmentCount = if (value == CreditType.CREDIT_LIMIT) "" else it.installmentCount,
                monthlyPayment = if (value == CreditType.CREDIT_LIMIT) "" else it.monthlyPayment,
                interestRate = if (value == CreditType.CREDIT_LIMIT) "" else it.interestRate,
                errorMessage = null
            )
        }
    }

    fun onTotalAmountChanged(value: String) {
        _addCreditFormState.update { it.copy(totalAmount = value, errorMessage = null) }
    }

    fun onInstallmentCountChanged(value: String) {
        _addCreditFormState.update { it.copy(installmentCount = value, errorMessage = null) }
    }

    fun onPaymentDueDayChanged(day: Int) {
        _addCreditFormState.update { it.copy(paymentDueDay = day, errorMessage = null) }
    }

    fun onMonthlyPaymentChanged(value: String) {
        _addCreditFormState.update { it.copy(monthlyPayment = value, errorMessage = null) }
    }

    fun onInterestRateChanged(value: String) {
        _addCreditFormState.update { it.copy(interestRate = value, errorMessage = null) }
    }

    fun onNoteChanged(value: String) {
        _addCreditFormState.update { it.copy(note = value, errorMessage = null) }
    }

    fun onAlreadyPaidAmountChanged(value: String) {
        _addCreditFormState.update { it.copy(alreadyPaidAmount = value, errorMessage = null) }
    }

    fun saveCredit(onSaved: () -> Unit = {}) {
        val form = _addCreditFormState.value
        val totalAmount = form.totalAmount.toDoubleOrNull()
        val installmentPlan = isInstallmentPlan(form.creditType)
        val creditLimit = isCreditLimit(form.creditType)

        if (form.name.isBlank()) {
            _addCreditFormState.update { it.copy(errorMessage = "Введіть назву кредиту") }
            return
        }

        if (totalAmount == null || totalAmount <= 0.0) {
            _addCreditFormState.update { it.copy(errorMessage = "Введіть коректну загальну суму") }
            return
        }

        val installmentCount = form.installmentCount.toIntOrNull()
        if (installmentPlan && (installmentCount == null || installmentCount <= 0)) {
            _addCreditFormState.update { it.copy(errorMessage = "Вкажіть кількість платежів") }
            return
        }

        val paymentDueDate = if (creditLimit) {
            null
        } else {
            val dueDay = form.paymentDueDay
            if (dueDay == null || dueDay !in 1..31) {
                _addCreditFormState.update { it.copy(errorMessage = "Вкажіть день платежу") }
                return
            }
            nextDueDateFromDay(dueDay)
        }

        val monthlyPayment = when {
            creditLimit -> null
            installmentPlan -> installmentPaymentPreview()
            form.monthlyPayment.isBlank() -> null
            else -> form.monthlyPayment.toDoubleOrNull()
        }

        if (!creditLimit && !installmentPlan && form.monthlyPayment.isNotBlank() && monthlyPayment == null) {
            _addCreditFormState.update { it.copy(errorMessage = "Щомісячний платіж має бути числом") }
            return
        }

        val interestRate = if (creditLimit || form.interestRate.isBlank()) {
            null
        } else {
            form.interestRate.toDoubleOrNull()
        }

        if (!creditLimit && form.interestRate.isNotBlank() && interestRate == null) {
            _addCreditFormState.update { it.copy(errorMessage = "Відсоткова ставка має бути числом") }
            return
        }

        val initialPaidAmount = if (creditLimit) {
            if (form.alreadyPaidAmount.isBlank()) {
                0.0
            } else {
                val parsed = form.alreadyPaidAmount.toDoubleOrNull()
                if (parsed == null) {
                    _addCreditFormState.update { it.copy(errorMessage = "Вже внесено має бути числом") }
                    return
                }
                if (parsed < 0.0) {
                    _addCreditFormState.update { it.copy(errorMessage = "Вже внесено не може бути від'ємним") }
                    return
                }
                if (parsed > totalAmount) {
                    _addCreditFormState.update { it.copy(errorMessage = "Вже внесено не може перевищувати загальну суму") }
                    return
                }
                parsed
            }
        } else {
            null
        }

        viewModelScope.launch {
            creditRepository.addCreditAccount(
                name = form.name.trim(),
                creditType = form.creditType,
                totalAmount = totalAmount,
                monthlyPayment = monthlyPayment,
                installmentCount = if (installmentPlan) installmentCount else null,
                paymentDueDate = paymentDueDate,
                interestRate = interestRate,
                note = form.note.trim().ifBlank { null },
                initialPaidAmount = initialPaidAmount,
                startDate = System.currentTimeMillis(),
                endDate = null
            )

            _addCreditFormState.value = AddCreditFormState()
            onSaved()
        }
    }

    fun addPayment(creditId: Long, amountInput: String): Boolean {
        val amount = amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            return false
        }

        viewModelScope.launch {
            creditRepository.addPayment(
                creditId = creditId,
                amount = amount,
                paymentDate = System.currentTimeMillis()
            )
        }

        return true
    }

    fun markInstallmentPaid(creditId: Long) {
        viewModelScope.launch {
            creditRepository.markNextInstallmentAsPaid(
                creditId = creditId,
                paymentDate = System.currentTimeMillis()
            )
        }
    }

    fun undoLastPayment(creditId: Long) {
        viewModelScope.launch {
            creditRepository.undoLastPayment(creditId)
        }
    }

    private fun nextDueDateFromDay(dayOfMonth: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
        if (todayDay > dayOfMonth) {
            calendar.add(Calendar.MONTH, 1)
        }

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, min(dayOfMonth, maxDay))
        return calendar.timeInMillis
    }
}

class CreditViewModelFactory(
    private val creditRepository: CreditRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreditViewModel(creditRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
