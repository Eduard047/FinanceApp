package com.example.financeapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financeapp.data.entity.TransactionType
import com.example.financeapp.domain.formatCurrency
import com.example.financeapp.domain.formatDate
import com.example.financeapp.ui.localization.tr
import com.example.financeapp.ui.viewmodel.TransactionListItem

@Composable
fun TransactionsScreen(
    transactions: List<TransactionListItem>,
    onAddTransaction: () -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    canUndoDelete: Boolean,
    onUndoDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reveal by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<TransactionListItem?>(null) }

    val incomeTotal = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }
    val expenseTotal = transactions
        .filter { it.type == TransactionType.EXPENSE || it.type == TransactionType.CREDIT_PAYMENT }
        .sumOf { it.amount }

    LaunchedEffect(Unit) {
        reveal = true
    }

    transactionToDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text(text = tr("Видалити операцію?", "Delete transaction?")) },
            text = {
                Text(
                    text = tr(
                        "Цю дію не можна скасувати. ${formatCurrency(transaction.amount)} від ${formatDate(transaction.date)}",
                        "This action cannot be undone. ${formatCurrency(transaction.amount)} on ${formatDate(transaction.date)}"
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(transaction.id)
                        transactionToDelete = null
                    }
                ) {
                    Text(text = tr("Видалити", "Delete"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(text = tr("Скасувати", "Cancel"))
                }
            }
        )
    }

    FinanceScreenBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = tr("Операції", "Transactions"), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = tr("Поточний місяць", "Current month"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = onAddTransaction) {
                    Text(text = tr("Додати", "Add"))
                }
            }

            AnimatedVisibility(
                visible = reveal,
                enter = fadeIn(tween(450)) + slideInVertically(tween(450), initialOffsetY = { it / 6 })
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = tr("Дохід", "Income"), color = MaterialTheme.colorScheme.primary)
                            Text(text = formatCurrency(incomeTotal), color = MaterialTheme.colorScheme.primary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = tr("Витрати", "Expenses"), color = MaterialTheme.colorScheme.error)
                            Text(text = formatCurrency(expenseTotal), color = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider()
                        Text(
                            text = tr("${transactions.size} записів", "${transactions.size} records"),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (canUndoDelete) {
                TextButton(onClick = onUndoDelete) {
                    Text(text = tr("Скасувати видалення", "Undo delete"))
                }
            }

            if (transactions.isEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = tr("За цей місяць операцій ще немає.", "No transactions for this month yet."),
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(transactions, key = { _, item -> item.id }) { index, transaction ->
                        val accentColor = when (transaction.type) {
                            TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                            TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                            TransactionType.CREDIT_PAYMENT -> MaterialTheme.colorScheme.tertiary
                        }

                        AnimatedVisibility(
                            visible = reveal,
                            enter = fadeIn(tween(360, delayMillis = 120 + index * 40)) +
                                slideInVertically(tween(360, delayMillis = 120 + index * 40), initialOffsetY = { it / 8 })
                        ) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        SoftBadge(text = transactionTypeLabel(transaction.type))
                                        Text(
                                            text = formatCurrency(transaction.amount),
                                            color = accentColor,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    Text(
                                        text = transaction.categoryName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = formatDate(transaction.date),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (!transaction.note.isNullOrBlank()) {
                                        Text(
                                            text = transaction.note,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { transactionToDelete = transaction }) {
                                            Text(text = tr("Видалити", "Delete"), color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
