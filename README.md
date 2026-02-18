# Finance App (Jetpack Compose)

A modern Android finance tracker built with **Kotlin**, **Jetpack Compose**, **MVVM**, **Room**, **Coroutines + Flow**, **Repository pattern**, **Material 3**, and **Navigation Compose**.

## Features

- Dashboard with monthly income, expenses, net balance, and active credit debt.
- Transactions by current month.
- Add / delete transactions.
- Undo last deleted transaction.
- Category management from the transaction form:
  - create category
  - delete category (blocked if category is used in transactions)
- Credits:
  - add credit account
  - support installment/pay-in-parts plans
  - automatic per-installment amount calculation
  - payment due date support
  - payment history
  - mark next installment as paid
  - undo last payment
- Payment reminders using WorkManager notifications.
- Currency: **UAH (hryvnia)**.
- In-app language switch (**Ukrainian / English**) from the **top-right** menu (selection is persisted).

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM
- Room (with KAPT)
- Coroutines + Flow
- Navigation Compose
- WorkManager

## Database

Room entities:

- `CategoryEntity`
- `TransactionEntity`
- `CreditAccountEntity`
- `CreditPaymentEntity`

Includes enum converters and DB migration for credit-installment fields.

## Build & Run

### Requirements

- Android Studio (recent stable)
- JDK 11+
- Android SDK (minSdk 24, targetSdk 36)

### Commands

```bash
./gradlew :app:assembleDebug
```

On Windows:

```bash
gradlew.bat :app:assembleDebug
```

## Notes

- App data is stored locally in Room.
- Notification permission (`POST_NOTIFICATIONS`) is requested on Android 13+.
- If you switch language, UI labels update immediately via Compose state.
