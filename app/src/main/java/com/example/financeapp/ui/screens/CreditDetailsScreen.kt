package com.example.financeapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.financeapp.data.entity.CreditAccountEntity
import com.example.financeapp.data.entity.CreditPaymentEntity
import com.example.financeapp.data.entity.CreditType
import com.example.financeapp.domain.formatCurrency
import com.example.financeapp.domain.formatDate
import com.example.financeapp.ui.localization.tr

@Composable
fun CreditDetailsScreen(
    credit: CreditAccountEntity?,
    payments: List<CreditPaymentEntity>,
    onAddPayment: (String) -> Boolean,
    onMarkInstallmentPaid: () -> Unit,
    onUndoLastPayment: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var paymentAmount by remember { mutableStateOf("") }
    var paymentError by remember { mutableStateOf<String?>(null) }
    var reveal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        reveal = true
    }

    FinanceScreenBackground(modifier = modifier.fillMaxSize()) {
        if (credit == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onBack) {
                    Text(text = tr("Назад", "Back"))
                }
                Text(text = tr("Кредитний рахунок не знайдено", "Credit account not found"))
            }
            return@FinanceScreenBackground
        }

        val isInstallmentPlan = credit.creditType == CreditType.INSTALLMENT || credit.creditType == CreditType.PAY_IN_PARTS
        val isCreditLimit = credit.creditType == CreditType.CREDIT_LIMIT
        val totalInstallments = credit.installmentCount ?: 0
        val paidAmount = (credit.totalAmount - credit.remainingAmount).coerceAtLeast(0.0)
        val canMarkNextInstallment =
            (totalInstallments == 0 || credit.paidInstallments < totalInstallments) && credit.remainingAmount > 0.0

        val paidRatio = if (credit.totalAmount <= 0.0) {
            0f
        } else {
            ((credit.totalAmount - credit.remainingAmount) / credit.totalAmount)
                .coerceIn(0.0, 1.0)
                .toFloat()
        }

        val noPaymentsToUndoMessage = tr("Немає платежів для скасування", "No payments to undo")
        val invalidAmountMessage = tr("Введіть коректну суму", "Enter a valid amount")

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onBack) {
                        Text(text = tr("Назад", "Back"))
                    }
                    SoftBadge(text = tr("деталі", "details"))
                }
            }

            item {
                Text(text = credit.name, style = MaterialTheme.typography.headlineMedium)
            }

            item {
                AnimatedVisibility(
                    visible = reveal,
                    enter = fadeIn(tween(420)) + slideInVertically(tween(420), initialOffsetY = { it / 6 })
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SoftBadge(text = creditTypeLabel(credit.creditType))
                                Text(
                                    text = "${tr("Залишок", "Remaining")} ${formatCurrency(credit.remainingAmount)}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Text(text = "${tr("Загалом", "Total")}: ${formatCurrency(credit.totalAmount)}")
                            if (isCreditLimit) {
                                Text(text = "${tr("Внесено", "Paid manually")}: ${formatCurrency(paidAmount)}")
                            }
                            credit.monthlyPayment?.let {
                                Text(text = "${tr("Платіж", "Payment")}: ${formatCurrency(it)}")
                            }
                            credit.note?.takeIf { it.isNotBlank() }?.let {
                                Text(text = "${tr("Нотатка", "Note")}: $it")
                            }

                            if (isInstallmentPlan && totalInstallments > 0) {
                                Text(
                                    text = tr(
                                        "Сплачено платежів: ${credit.paidInstallments} з $totalInstallments",
                                        "Paid installments: ${credit.paidInstallments} of $totalInstallments"
                                    )
                                )
                                repeat(totalInstallments) { index ->
                                    val paid = index < credit.paidInstallments
                                    Text(
                                        text = tr(
                                            "Платіж ${index + 1}: ${if (paid) "сплачено" else "очікує"}",
                                            "Payment ${index + 1}: ${if (paid) "paid" else "pending"}"
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (paid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            credit.paymentDueDate?.let {
                                Text(text = "${tr("Сплатити до", "Pay by")}: ${formatDate(it)}")
                            }

                            credit.interestRate?.let {
                                Text(text = "${tr("Відсоткова ставка", "Interest rate")}: $it%")
                            }
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

            item {
                if (isInstallmentPlan) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onMarkInstallmentPaid()
                                paymentError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canMarkNextInstallment
                        ) {
                            Text(text = tr("Позначити наступний платіж сплаченим", "Mark next installment as paid"))
                        }

                        OutlinedButton(
                            onClick = {
                                if (payments.isEmpty()) {
                                    paymentError = noPaymentsToUndoMessage
                                } else {
                                    onUndoLastPayment()
                                    paymentError = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = payments.isNotEmpty()
                        ) {
                            Text(text = tr("Скасувати останній платіж", "Undo last payment"))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = paymentAmount,
                                onValueChange = {
                                    paymentAmount = it
                                    paymentError = null
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text(text = tr("Сума платежу", "Payment amount")) },
                                singleLine = true
                            )

                            Button(onClick = {
                                val saved = onAddPayment(paymentAmount)
                                if (saved) {
                                    paymentAmount = ""
                                    paymentError = null
                                } else {
                                    paymentError = invalidAmountMessage
                                }
                            }) {
                                Text(text = tr("Додати", "Add"))
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (payments.isEmpty()) {
                                    paymentError = noPaymentsToUndoMessage
                                } else {
                                    onUndoLastPayment()
                                    paymentError = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = payments.isNotEmpty()
                        ) {
                            Text(text = tr("Скасувати останній платіж", "Undo last payment"))
                        }
                    }
                }
            }

            paymentError?.let { error ->
                item {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                Text(text = tr("Історія платежів", "Payment history"), style = MaterialTheme.typography.titleMedium)
            }

            if (payments.isEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = tr("Платежів ще немає", "No payments yet"),
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                itemsIndexed(payments, key = { _, item -> item.id }) { index, payment ->
                    AnimatedVisibility(
                        visible = reveal,
                        enter = fadeIn(tween(360, delayMillis = 100 + index * 40)) +
                            slideInVertically(tween(360, delayMillis = 100 + index * 40), initialOffsetY = { it / 8 })
                    ) {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = formatCurrency(payment.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatDate(payment.paymentDate),
                                    style = MaterialTheme.typography.bodyMedium,
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
