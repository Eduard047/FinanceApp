package com.example.financeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financeapp.data.entity.CreditType
import com.example.financeapp.domain.formatCurrency
import com.example.financeapp.ui.localization.tr
import com.example.financeapp.ui.viewmodel.AddCreditFormState

@Composable
fun AddCreditScreen(
    formState: AddCreditFormState,
    isInstallmentPlan: Boolean,
    isCreditLimit: Boolean,
    installmentPaymentPreview: Double?,
    onNameChange: (String) -> Unit,
    onCreditTypeChange: (CreditType) -> Unit,
    onTotalAmountChange: (String) -> Unit,
    onInstallmentCountChange: (String) -> Unit,
    onPaymentDueDayChange: (Int) -> Unit,
    onMonthlyPaymentChange: (String) -> Unit,
    onInterestRateChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAlreadyPaidAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var dueDayMenuExpanded by remember { mutableStateOf(false) }

    FinanceScreenBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text(text = tr("Назад", "Back"))
                }
                SoftBadge(text = tr("кредит", "credit"))
            }

            Text(text = tr("Додати кредит", "Add credit"), style = MaterialTheme.typography.headlineMedium)

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.name,
                        onValueChange = onNameChange,
                        label = { Text(text = tr("Назва кредиту", "Credit name")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { typeMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "${tr("Тип", "Type")}: ${creditTypeLabel(formState.creditType)}")
                        }

                        DropdownMenu(
                            expanded = typeMenuExpanded,
                            onDismissRequest = { typeMenuExpanded = false }
                        ) {
                            CreditType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(text = creditTypeLabel(type)) },
                                    onClick = {
                                        onCreditTypeChange(type)
                                        typeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formState.totalAmount,
                        onValueChange = onTotalAmountChange,
                        label = {
                            Text(
                                text = if (isCreditLimit) {
                                    tr("Сума кредитного ліміту", "Credit limit amount")
                                } else {
                                    tr("Загальна сума", "Total amount")
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isCreditLimit) {
                        OutlinedTextField(
                            value = formState.alreadyPaidAmount,
                            onValueChange = onAlreadyPaidAmountChange,
                            label = { Text(text = tr("Вже внесено", "Already paid")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = formState.note,
                            onValueChange = onNoteChange,
                            label = { Text(text = tr("Нотатка (необов'язково)", "Note (optional)")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        if (isInstallmentPlan) {
                            OutlinedTextField(
                                value = formState.installmentCount,
                                onValueChange = onInstallmentCountChange,
                                label = { Text(text = tr("Кількість платежів", "Number of payments")) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = formState.monthlyPayment,
                                onValueChange = onMonthlyPaymentChange,
                                label = { Text(text = tr("Щомісячний платіж (необов'язково)", "Monthly payment (optional)")) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { dueDayMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val selectedDay = formState.paymentDueDay
                                Text(
                                    text = if (selectedDay == null) {
                                        tr("Оберіть день платежу", "Choose payment day")
                                    } else {
                                        tr("До $selectedDay числа", "Due on day $selectedDay")
                                    }
                                )
                            }

                            DropdownMenu(
                                expanded = dueDayMenuExpanded,
                                onDismissRequest = { dueDayMenuExpanded = false }
                            ) {
                                (1..31).forEach { day ->
                                    DropdownMenuItem(
                                        text = { Text(text = day.toString()) },
                                        onClick = {
                                            onPaymentDueDayChange(day)
                                            dueDayMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (isInstallmentPlan) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = tr("Сума одного платежу", "Amount per payment"),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = installmentPaymentPreview?.let { formatCurrency(it) } ?: "-",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = formState.interestRate,
                            onValueChange = onInterestRateChange,
                            label = { Text(text = tr("Відсоткова ставка % (необов'язково)", "Interest rate % (optional)")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = formState.note,
                            onValueChange = onNoteChange,
                            label = { Text(text = tr("Нотатка (необов'язково)", "Note (optional)")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    formState.errorMessage?.let { error ->
                        Text(text = localizedCreditMessage(error), color = MaterialTheme.colorScheme.error)
                    }

                    Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                        Text(text = tr("Зберегти кредит", "Save credit"))
                    }
                }
            }
        }
    }
}

@Composable
private fun localizedCreditMessage(message: String): String {
    return when (message) {
        "Введіть назву кредиту" -> tr("Введіть назву кредиту", "Enter credit name")
        "Введіть коректну загальну суму" -> tr("Введіть коректну загальну суму", "Enter a valid total amount")
        "Вкажіть кількість платежів" -> tr("Вкажіть кількість платежів", "Enter number of payments")
        "Вкажіть день платежу" -> tr("Вкажіть день платежу", "Choose payment day")
        "Щомісячний платіж має бути числом" -> tr("Щомісячний платіж має бути числом", "Monthly payment must be numeric")
        "Відсоткова ставка має бути числом" -> tr("Відсоткова ставка має бути числом", "Interest rate must be numeric")
        "Вже внесено має бути числом" -> tr("Вже внесено має бути числом", "Already paid must be numeric")
        "Вже внесено не може бути від'ємним" -> tr("Вже внесено не може бути від'ємним", "Already paid cannot be negative")
        "Вже внесено не може перевищувати загальну суму" -> tr(
            "Вже внесено не може перевищувати загальну суму",
            "Already paid cannot exceed total amount"
        )

        else -> message
    }
}
