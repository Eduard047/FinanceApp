package com.example.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.financeapp.data.entity.CreditType
import com.example.financeapp.data.entity.TransactionType
import com.example.financeapp.ui.localization.tr

@Composable
fun FinanceScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val brush = Brush.verticalGradient(
        colors = listOf(
            colors.primaryContainer.copy(alpha = 0.35f),
            colors.background,
            colors.tertiaryContainer.copy(alpha = 0.22f),
            colors.secondaryContainer.copy(alpha = 0.3f)
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .background(brush),
        content = content
    )
}

@Composable
fun SoftBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
        shape = CircleShape
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MonthSwitcher(
    monthLabel: String,
    isCurrentMonth: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetToCurrentMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPreviousMonth
            ) {
                Text(text = "‹")
            }

            FilledTonalButton(
                onClick = onResetToCurrentMonth,
                modifier = Modifier.weight(1f),
                enabled = !isCurrentMonth
            ) {
                Text(text = monthLabel)
            }

            OutlinedButton(
                onClick = onNextMonth
            ) {
                Text(text = "›")
            }
        }

        if (isCurrentMonth) {
            SoftBadge(text = tr("Поточний місяць", "Current month"))
        } else {
            Text(
                text = tr("Натисніть на місяць, щоб повернутись до поточного", "Tap month to return to current"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun transactionTypeLabel(type: TransactionType): String {
    return when (type) {
        TransactionType.INCOME -> tr("Дохід", "Income")
        TransactionType.EXPENSE -> tr("Витрата", "Expense")
        TransactionType.CREDIT_PAYMENT -> tr("Платіж за кредитом", "Credit payment")
    }
}

@Composable
fun creditTypeLabel(type: CreditType): String {
    return when (type) {
        CreditType.INSTALLMENT -> tr("Розстрочка", "Installment")
        CreditType.PAY_IN_PARTS -> tr("Оплата частинами", "Pay in parts")
        CreditType.CREDIT_LIMIT -> tr("Кредитний ліміт", "Credit limit")
        CreditType.LOAN -> tr("Позика", "Loan")
    }
}
