package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.entity.CreditAccountEntity
import com.example.financeapp.data.entity.CreditPaymentEntity
import com.example.financeapp.data.entity.CreditType
import com.example.financeapp.data.repository.CreditRepository
import com.example.financeapp.domain.parseDateInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddCreditFormState(
    val name: String = "",
    val creditType: CreditType = CreditType.INSTALLMENT,
    val totalAmount: String = "",
    val installmentCount: String = "",
    val paymentDueDateInput: String = "",
    val monthlyPayment: String = "",
    val interestRate: String = "",
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

    fun onPaymentDueDateChanged(value: String) {
        _addCreditFormState.update { it.copy(paymentDueDateInput = value, errorMessage = null) }
    }

    fun onMonthlyPaymentChanged(value: String) {
        _addCreditFormState.update { it.copy(monthlyPayment = value, errorMessage = null) }
    }

    fun onInterestRateChanged(value: String) {
        _addCreditFormState.update { it.copy(interestRate = value, errorMessage = null) }
    }

    fun saveCredit(onSaved: () -> Unit = {}) {
        val form = _addCreditFormState.value
        val totalAmount = form.totalAmount.toDoubleOrNull()
        val installmentPlan = isInstallmentPlan(form.creditType)

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

        val paymentDueDate = parseDateInput(form.paymentDueDateInput)
        if (form.paymentDueDateInput.isNotBlank() && paymentDueDate == null) {
            _addCreditFormState.update { it.copy(errorMessage = "Дата має бути у форматі дд.мм.рррр") }
            return
        }

        if (installmentPlan && paymentDueDate == null) {
            _addCreditFormState.update { it.copy(errorMessage = "Вкажіть дату наступного платежу") }
            return
        }

        val monthlyPayment = if (installmentPlan) {
            installmentPaymentPreview()
        } else {
            if (form.monthlyPayment.isBlank()) {
                null
            } else {
                form.monthlyPayment.toDoubleOrNull()
            }
        }

        if (!installmentPlan && form.monthlyPayment.isNotBlank() && monthlyPayment == null) {
            _addCreditFormState.update { it.copy(errorMessage = "Щомісячний платіж має бути числом") }
            return
        }

        val interestRate = if (form.interestRate.isBlank()) {
            null
        } else {
            form.interestRate.toDoubleOrNull()
        }

        if (form.interestRate.isNotBlank() && interestRate == null) {
            _addCreditFormState.update { it.copy(errorMessage = "Відсоткова ставка має бути числом") }
            return
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
