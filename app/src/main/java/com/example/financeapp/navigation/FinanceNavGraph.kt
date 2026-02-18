package com.example.financeapp.navigation

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.financeapp.data.repository.CreditRepository
import com.example.financeapp.data.repository.FinanceRepository
import com.example.financeapp.ui.localization.AppLanguage
import com.example.financeapp.ui.localization.LocalAppLanguage
import com.example.financeapp.ui.localization.tr
import com.example.financeapp.ui.screens.AddCreditScreen
import com.example.financeapp.ui.screens.AddTransactionScreen
import com.example.financeapp.ui.screens.CreditDetailsScreen
import com.example.financeapp.ui.screens.CreditListScreen
import com.example.financeapp.ui.screens.DashboardScreen
import com.example.financeapp.ui.screens.TransactionsScreen
import com.example.financeapp.ui.viewmodel.CreditViewModel
import com.example.financeapp.ui.viewmodel.CreditViewModelFactory
import com.example.financeapp.ui.viewmodel.DashboardViewModel
import com.example.financeapp.ui.viewmodel.DashboardViewModelFactory
import com.example.financeapp.ui.viewmodel.TransactionViewModel
import com.example.financeapp.ui.viewmodel.TransactionViewModelFactory

@Composable
fun FinanceNavGraph(
    financeRepository: FinanceRepository,
    creditRepository: CreditRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("finance_ui_settings", Context.MODE_PRIVATE)
    }
    var appLanguage by rememberSaveable {
        mutableStateOf(
            when (preferences.getString("app_language", AppLanguage.UK.name)) {
                AppLanguage.EN.name -> AppLanguage.EN
                else -> AppLanguage.UK
            }
        )
    }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(appLanguage) {
        preferences.edit().putString("app_language", appLanguage.name).apply()
    }

    CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
        Scaffold(
            modifier = modifier,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { languageMenuExpanded = true }) {
                        Text(text = if (appLanguage == AppLanguage.UK) "UKR" else "ENG")
                    }

                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = tr("Українська", "Ukrainian")) },
                            onClick = {
                                appLanguage = AppLanguage.UK
                                languageMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = tr("Англійська", "English")) },
                            onClick = {
                                appLanguage = AppLanguage.EN
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            },
            bottomBar = {
                if (FinanceDestination.bottomNavDestinations.any { it.route == currentRoute }) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                        tonalElevation = 10.dp,
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent
                        ) {
                            FinanceDestination.bottomNavDestinations.forEach { destination ->
                                val selected = currentRoute == destination.route
                                val label = destination.label(appLanguage)

                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                                    ),
                                    icon = {
                                        Text(
                                            text = label.take(1),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    label = { Text(text = label) }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = FinanceDestination.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(FinanceDestination.Dashboard.route) {
                    val dashboardViewModel: DashboardViewModel = viewModel(
                        factory = DashboardViewModelFactory(financeRepository, creditRepository)
                    )
                    val uiState by dashboardViewModel.uiState.collectAsState()

                    DashboardScreen(
                        uiState = uiState,
                        onAddTransaction = { navController.navigate(FinanceDestination.AddTransaction.route) },
                        onAddCredit = { navController.navigate(FinanceDestination.AddCredit.route) },
                        onCreditClick = { creditId ->
                            navController.navigate(FinanceDestination.CreditDetails.createRoute(creditId))
                        }
                    )
                }

                composable(FinanceDestination.Transactions.route) {
                    val transactionViewModel: TransactionViewModel = viewModel(
                        factory = TransactionViewModelFactory(financeRepository)
                    )
                    val transactions by transactionViewModel.transactions.collectAsState()
                    val canUndoDelete by transactionViewModel.canUndoDelete.collectAsState()

                    TransactionsScreen(
                        transactions = transactions,
                        onAddTransaction = { navController.navigate(FinanceDestination.AddTransaction.route) },
                        onDeleteTransaction = transactionViewModel::deleteTransaction,
                        canUndoDelete = canUndoDelete,
                        onUndoDelete = transactionViewModel::undoDeleteTransaction
                    )
                }

                composable(FinanceDestination.AddTransaction.route) {
                    val transactionViewModel: TransactionViewModel = viewModel(
                        factory = TransactionViewModelFactory(financeRepository)
                    )
                    val formState by transactionViewModel.formState.collectAsState()
                    val categories by transactionViewModel.filteredCategories.collectAsState()
                    val categoryActionState by transactionViewModel.categoryActionState.collectAsState()

                    AddTransactionScreen(
                        formState = formState,
                        categories = categories,
                        onAmountChange = transactionViewModel::onAmountChanged,
                        onNoteChange = transactionViewModel::onNoteChanged,
                        onTypeChange = transactionViewModel::onTypeChanged,
                        onCategorySelected = transactionViewModel::onCategorySelected,
                        onAddCategory = transactionViewModel::addCategory,
                        onDeleteCategory = transactionViewModel::deleteCategory,
                        categoryActionMessage = categoryActionState.message,
                        isCategoryActionError = categoryActionState.isError,
                        onClearCategoryActionMessage = transactionViewModel::clearCategoryActionMessage,
                        onSave = {
                            transactionViewModel.saveTransaction {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(FinanceDestination.Credits.route) {
                    val creditViewModel: CreditViewModel = viewModel(
                        factory = CreditViewModelFactory(creditRepository)
                    )
                    val credits by creditViewModel.creditAccounts.collectAsState()

                    CreditListScreen(
                        credits = credits,
                        onAddCredit = { navController.navigate(FinanceDestination.AddCredit.route) },
                        onOpenCreditDetails = { creditId ->
                            navController.navigate(FinanceDestination.CreditDetails.createRoute(creditId))
                        }
                    )
                }

                composable(FinanceDestination.AddCredit.route) {
                    val creditViewModel: CreditViewModel = viewModel(
                        factory = CreditViewModelFactory(creditRepository)
                    )
                    val formState by creditViewModel.addCreditFormState.collectAsState()
                    val isInstallmentPlan = creditViewModel.isInstallmentPlan(formState.creditType)
                    val isCreditLimit = creditViewModel.isCreditLimit(formState.creditType)
                    val installmentPaymentPreview = creditViewModel.installmentPaymentPreview()

                    AddCreditScreen(
                        formState = formState,
                        isInstallmentPlan = isInstallmentPlan,
                        isCreditLimit = isCreditLimit,
                        installmentPaymentPreview = installmentPaymentPreview,
                        onNameChange = creditViewModel::onNameChanged,
                        onCreditTypeChange = creditViewModel::onCreditTypeChanged,
                        onTotalAmountChange = creditViewModel::onTotalAmountChanged,
                        onInstallmentCountChange = creditViewModel::onInstallmentCountChanged,
                        onPaymentDueDayChange = creditViewModel::onPaymentDueDayChanged,
                        onMonthlyPaymentChange = creditViewModel::onMonthlyPaymentChanged,
                        onInterestRateChange = creditViewModel::onInterestRateChanged,
                        onNoteChange = creditViewModel::onNoteChanged,
                        onAlreadyPaidAmountChange = creditViewModel::onAlreadyPaidAmountChanged,
                        onSave = {
                            creditViewModel.saveCredit {
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = FinanceDestination.CreditDetails.route,
                    arguments = listOf(
                        navArgument(FinanceDestination.CreditDetails.ARG_CREDIT_ID) {
                            type = NavType.LongType
                        }
                    )
                ) { entry ->
                    val creditId = entry.arguments?.getLong(FinanceDestination.CreditDetails.ARG_CREDIT_ID)
                        ?: return@composable

                    val creditViewModel: CreditViewModel = viewModel(
                        factory = CreditViewModelFactory(creditRepository)
                    )

                    val creditFlow = creditViewModel.creditById(creditId)
                    val paymentsFlow = creditViewModel.getCreditPayments(creditId)

                    val credit by creditFlow.collectAsState(initial = null)
                    val payments by paymentsFlow.collectAsState(initial = emptyList())

                    CreditDetailsScreen(
                        credit = credit,
                        payments = payments,
                        onAddPayment = { amountInput ->
                            creditViewModel.addPayment(creditId, amountInput)
                        },
                        onMarkInstallmentPaid = {
                            creditViewModel.markInstallmentPaid(creditId)
                        },
                        onUndoLastPayment = {
                            creditViewModel.undoLastPayment(creditId)
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
