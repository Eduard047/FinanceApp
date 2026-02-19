package com.example.financeapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.financeapp.domain.formatCurrency
import com.example.financeapp.ui.localization.tr
import com.example.financeapp.ui.viewmodel.DashboardUiState

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    monthLabel: String,
    isCurrentMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetToCurrentMonth: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddCredit: () -> Unit,
    onCreditClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var reveal by remember { mutableStateOf(false) }
    val netBalance = uiState.monthlyIncome - uiState.monthlyExpenses

    LaunchedEffect(Unit) {
        reveal = true
    }

    FinanceScreenBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = tr("Фінансовий потік", "Cash flow"),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = tr("Огляд за обраний місяць і стан боргу", "Selected month overview and debt status"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MonthSwitcher(
                monthLabel = monthLabel,
                isCurrentMonth = isCurrentMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onResetToCurrentMonth = onResetToCurrentMonth
            )

            AnimatedVisibility(
                visible = reveal,
                enter = fadeIn(tween(480)) + slideInVertically(tween(480), initialOffsetY = { it / 5 })
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = tr("Чистий баланс", "Net balance"),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = formatCurrency(netBalance),
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (netBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = tr("Дохід", "Income"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(uiState.monthlyIncome),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tr("Витрати", "Expenses"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(uiState.monthlyExpenses),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = tr("Борг", "Debt"),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency(uiState.totalRemainingDebt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onAddTransaction,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = tr("Додати операцію", "Add transaction"))
                }

                OutlinedButton(
                    onClick = onAddCredit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = tr("Додати кредит", "Add credit"))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tr("Активні кредити", "Active credits"),
                    style = MaterialTheme.typography.titleMedium
                )
                SoftBadge(text = if (uiState.activeCredits.size == 1) {
                    tr("1 активний", "1 active")
                } else {
                    if (uiState.activeCredits.isEmpty()) {
                        tr("0 активних", "0 active")
                    } else {
                        tr("${uiState.activeCredits.size} активних", "${uiState.activeCredits.size} active")
                    }
                })
            }

            if (uiState.activeCredits.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
                    )
                ) {
                    Text(
                        text = tr(
                            "Немає активних кредитів. Додайте перший для відстеження боргу.",
                            "No active credits yet. Add your first one to track debt."
                        ),
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                uiState.activeCredits.forEachIndexed { index, credit ->
                    AnimatedVisibility(
                        visible = reveal,
                        enter = fadeIn(tween(380, delayMillis = 120 + index * 55)) +
                            slideInVertically(tween(380, delayMillis = 120 + index * 55), initialOffsetY = { it / 8 })
                    ) {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCreditClick(credit.id) },
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = credit.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    SoftBadge(text = creditTypeLabel(credit.creditType))
                                }

                                Text(
                                    text = "${tr("Залишок", "Remaining")}: ${formatCurrency(credit.remainingAmount)}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
