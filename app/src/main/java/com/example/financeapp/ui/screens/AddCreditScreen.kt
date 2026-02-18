package com.example.financeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
    installmentPaymentPreview: Double?,
    onNameChange: (String) -> Unit,
    onCreditTypeChange: (CreditType) -> Unit,
    onTotalAmountChange: (String) -> Unit,
    onInstallmentCountChange: (String) -> Unit,
    onPaymentDueDateChange: (String) -> Unit,
    onMonthlyPaymentChange: (String) -> Unit,
    onInterestRateChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }

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

                    OutlinedTextField(
                        value = formState.totalAmount,
                        onValueChange = onTotalAmountChange,
                        label = { Text(text = tr("Загальна сума", "Total amount")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isInstallmentPlan) {
                        OutlinedTextField(
                            value = formState.installmentCount,
                            onValueChange = onInstallmentCountChange,
                            label = { Text(text = tr("Кількість платежів", "Number of payments")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = formState.paymentDueDateInput,
                            onValueChange = onPaymentDueDateChange,
                            label = { Text(text = tr("Дата платежу до (дд.мм.рррр)", "Payment due date (dd.MM.yyyy)")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

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
                    } else {
                        OutlinedTextField(
                            value = formState.monthlyPayment,
                            onValueChange = onMonthlyPaymentChange,
                            label = { Text(text = tr("Щомісячний платіж (необов'язково)", "Monthly payment (optional)")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = formState.paymentDueDateInput,
                            onValueChange = onPaymentDueDateChange,
                            label = { Text(text = tr("Дата наступного платежу (дд.мм.рррр)", "Next payment date (dd.MM.yyyy)")) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = formState.interestRate,
                        onValueChange = onInterestRateChange,
                        label = { Text(text = tr("Відсоткова ставка % (необов'язково)", "Interest rate % (optional)")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

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
        "Дата має бути у форматі дд.мм.рррр" -> tr("Дата має бути у форматі дд.мм.рррр", "Date must match dd.MM.yyyy")
        "Вкажіть дату наступного платежу" -> tr("Вкажіть дату наступного платежу", "Enter next payment date")
        "Щомісячний платіж має бути числом" -> tr("Щомісячний платіж має бути числом", "Monthly payment must be numeric")
        "Відсоткова ставка має бути числом" -> tr("Відсоткова ставка має бути числом", "Interest rate must be numeric")
        else -> message
    }
}
