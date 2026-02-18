package com.example.financeapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financeapp.data.entity.CreditAccountEntity
import com.example.financeapp.domain.formatCurrency
import com.example.financeapp.ui.localization.tr

@Composable
fun CreditListScreen(
    credits: List<CreditAccountEntity>,
    onAddCredit: () -> Unit,
    onOpenCreditDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var reveal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        reveal = true
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
                    Text(text = tr("Кредити", "Credits"), style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = tr("Облік усіх боргових рахунків", "All debt accounts overview"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = onAddCredit) {
                    Text(text = tr("Додати", "Add"))
                }
            }

            if (credits.isEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = tr("Кредитних рахунків ще немає.", "No credit accounts yet."),
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(credits, key = { _, item -> item.id }) { index, credit ->
                        val paidRatio = if (credit.totalAmount <= 0) {
                            0f
                        } else {
                            ((credit.totalAmount - credit.remainingAmount) / credit.totalAmount)
                                .coerceIn(0.0, 1.0)
                                .toFloat()
                        }

                        AnimatedVisibility(
                            visible = reveal,
                            enter = fadeIn(tween(360, delayMillis = 100 + index * 45)) +
                                slideInVertically(tween(360, delayMillis = 100 + index * 45), initialOffsetY = { it / 8 })
                        ) {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenCreditDetails(credit.id) },
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = credit.name, style = MaterialTheme.typography.titleMedium)
                                        SoftBadge(text = creditTypeLabel(credit.creditType))
                                    }

                                    Text(
                                        text = "${tr("Залишок", "Remaining")}: ${formatCurrency(credit.remainingAmount)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    LinearProgressIndicator(
                                        progress = { paidRatio },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = tr("Сплачено ${(paidRatio * 100).toInt()}%", "Paid ${(paidRatio * 100).toInt()}%"),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
