package com.example.financeapp.navigation

import com.example.financeapp.ui.localization.AppLanguage

sealed class FinanceDestination(
    val route: String,
    val topLevel: Boolean = false
) {
    data object Dashboard : FinanceDestination("dashboard", true)
    data object Transactions : FinanceDestination("transactions", true)
    data object Credits : FinanceDestination("credits", true)

    data object AddTransaction : FinanceDestination("add_transaction")
    data object AddCredit : FinanceDestination("add_credit")

    data object CreditDetails : FinanceDestination("credit_details/{creditId}") {
        const val ARG_CREDIT_ID: String = "creditId"
        fun createRoute(creditId: Long): String = "credit_details/$creditId"
    }

    companion object {
        val bottomNavDestinations = listOf(Dashboard, Transactions, Credits)
    }
}

fun FinanceDestination.label(language: AppLanguage): String {
    return when (this) {
        FinanceDestination.Dashboard -> if (language == AppLanguage.UK) "Головна" else "Home"
        FinanceDestination.Transactions -> if (language == AppLanguage.UK) "Операції" else "Transactions"
        FinanceDestination.Credits -> if (language == AppLanguage.UK) "Кредити" else "Credits"
        FinanceDestination.AddTransaction -> if (language == AppLanguage.UK) "Додати операцію" else "Add transaction"
        FinanceDestination.AddCredit -> if (language == AppLanguage.UK) "Додати кредит" else "Add credit"
        FinanceDestination.CreditDetails -> if (language == AppLanguage.UK) "Деталі кредиту" else "Credit details"
    }
}
