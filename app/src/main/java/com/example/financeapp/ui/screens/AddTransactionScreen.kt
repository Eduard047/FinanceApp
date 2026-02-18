package com.example.financeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
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
import com.example.financeapp.data.entity.CategoryEntity
import com.example.financeapp.data.entity.TransactionType
import com.example.financeapp.ui.localization.tr
import com.example.financeapp.ui.viewmodel.AddTransactionFormState

@Composable
fun AddTransactionScreen(
    formState: AddTransactionFormState,
    categories: List<CategoryEntity>,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    categoryActionMessage: String?,
    isCategoryActionError: Boolean,
    onClearCategoryActionMessage: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var categoriesDialogExpanded by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    val selectedCategory = categories.firstOrNull { it.id == formState.selectedCategoryId }

    if (categoriesDialogExpanded) {
        AlertDialog(
            onDismissRequest = {
                categoriesDialogExpanded = false
                onClearCategoryActionMessage()
            },
            title = { Text(text = tr("Керування категоріями", "Manage categories")) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val typeLabel = when (formState.type) {
                        TransactionType.INCOME -> tr("Дохід", "Income")
                        TransactionType.EXPENSE -> tr("Витрата", "Expense")
                        TransactionType.CREDIT_PAYMENT -> tr("Кредит", "Credit")
                    }

                    Text(
                        text = "${tr("Поточний тип", "Current type")}: $typeLabel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text(text = tr("Нова категорія", "New category")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onAddCategory(newCategoryName)
                            newCategoryName = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = tr("Додати категорію", "Add category"))
                    }

                    categoryActionMessage?.let { message ->
                        Text(
                            text = localizedTransactionMessage(message),
                            color = if (isCategoryActionError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    if (categories.isEmpty()) {
                        Text(
                            text = tr("Категорій для цього типу ще немає", "No categories for this type yet"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        categories.forEach { category ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { onDeleteCategory(category.id) }) {
                                    Text(
                                        text = tr("Видалити", "Delete"),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    categoriesDialogExpanded = false
                    onClearCategoryActionMessage()
                }) {
                    Text(text = tr("Готово", "Done"))
                }
            }
        )
    }

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
                SoftBadge(text = tr("нова", "new"))
            }

            Text(text = tr("Додати операцію", "Add transaction"), style = MaterialTheme.typography.headlineMedium)

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
                        value = formState.amount,
                        onValueChange = onAmountChange,
                        label = { Text(text = tr("Сума", "Amount")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = formState.note,
                        onValueChange = onNoteChange,
                        label = { Text(text = tr("Нотатка (необов'язково)", "Note (optional)")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = tr("Тип", "Type"),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = formState.type == TransactionType.INCOME,
                            onClick = { onTypeChange(TransactionType.INCOME) },
                            label = { Text(text = tr("Дохід", "Income")) }
                        )
                        FilterChip(
                            selected = formState.type == TransactionType.EXPENSE,
                            onClick = { onTypeChange(TransactionType.EXPENSE) },
                            label = { Text(text = tr("Витрата", "Expense")) }
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { categoryMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = selectedCategory?.name ?: tr("Оберіть категорію", "Choose category"))
                        }

                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(text = category.name) },
                                    onClick = {
                                        onCategorySelected(category.id)
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            categoriesDialogExpanded = true
                            onClearCategoryActionMessage()
                        }
                    ) {
                        Text(text = tr("Керувати категоріями", "Manage categories"))
                    }

                    formState.errorMessage?.let { error ->
                        Text(text = localizedTransactionMessage(error), color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = tr("Зберегти операцію", "Save transaction"))
                    }
                }
            }
        }
    }
}

@Composable
private fun localizedTransactionMessage(message: String): String {
    return when (message) {
        "Введіть коректну суму" -> tr("Введіть коректну суму", "Enter a valid amount")
        "Оберіть категорію" -> tr("Оберіть категорію", "Choose a category")
        "Категорію додано" -> tr("Категорію додано", "Category added")
        "Така категорія вже існує" -> tr("Така категорія вже існує", "Category already exists")
        "Введіть назву категорії" -> tr("Введіть назву категорії", "Enter a category name")
        "Не вдалося додати категорію" -> tr("Не вдалося додати категорію", "Failed to add category")
        "Категорію видалено" -> tr("Категорію видалено", "Category deleted")
        "Категорія вже використовується в операціях" -> tr(
            "Категорія вже використовується в операціях",
            "Category is already used in transactions"
        )

        "Категорію не знайдено" -> tr("Категорію не знайдено", "Category not found")
        "Не вдалося видалити категорію" -> tr("Не вдалося видалити категорію", "Failed to delete category")
        else -> message
    }
}
