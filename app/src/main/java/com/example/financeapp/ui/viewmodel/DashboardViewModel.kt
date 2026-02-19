package com.example.financeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.entity.CreditAccountEntity
import com.example.financeapp.data.repository.CreditRepository
import com.example.financeapp.data.repository.FinanceRepository
import com.example.financeapp.domain.MonthSelection
import com.example.financeapp.domain.currentMonthSelection
import com.example.financeapp.domain.monthRange
import com.example.financeapp.domain.shiftMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class DashboardUiState(
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val totalRemainingDebt: Double = 0.0,
    val activeCredits: List<CreditAccountEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    financeRepository: FinanceRepository,
    creditRepository: CreditRepository
) : ViewModel() {
    private val _selectedMonth = MutableStateFlow(currentMonthSelection())
    val selectedMonth: StateFlow<MonthSelection> = _selectedMonth.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = selectedMonth
        .flatMapLatest { selectedMonth ->
            val monthRange = monthRange(selectedMonth)
            combine(
                financeRepository.getMonthlyIncomeSum(monthRange.start, monthRange.end),
                financeRepository.getMonthlyExpenseSum(monthRange.start, monthRange.end),
                creditRepository.getCreditDebtSummary(),
                creditRepository.getActiveCredits()
            ) { income, expenses, debt, activeCredits ->
                DashboardUiState(
                    monthlyIncome = income,
                    monthlyExpenses = expenses,
                    totalRemainingDebt = debt,
                    activeCredits = activeCredits
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )

    fun selectPreviousMonth() {
        _selectedMonth.update { shiftMonth(it, -1) }
    }

    fun selectNextMonth() {
        _selectedMonth.update { shiftMonth(it, 1) }
    }

    fun resetToCurrentMonth() {
        _selectedMonth.value = currentMonthSelection()
    }

    fun isCurrentMonthSelected(): Boolean {
        val current = currentMonthSelection()
        val selected = _selectedMonth.value
        return current.year == selected.year && current.month == selected.month
    }
}

class DashboardViewModelFactory(
    private val financeRepository: FinanceRepository,
    private val creditRepository: CreditRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(financeRepository, creditRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
