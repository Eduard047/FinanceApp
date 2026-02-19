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
- Refreshed modern visual style with Google Fonts (**Unbounded** + **Manrope**) and updated Material 3 color/shape tokens.

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

## Update Without Reinstall

If you already installed the app and have local data, update using the same package/signature:

```bash
adb install -r app-debug.apk
```

For convenience, use the helper script that builds a debug APK with an auto-incrementing `versionCode` and copies it to project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-update-apk.ps1
```

Then install/update:

```bash
adb install -r app-debug.apk
```

Notes:
- `-r` updates in place and preserves app data.
- If you install manually from phone file manager, higher `versionCode` is required (the script handles this).
- If you get `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, the APK is signed with a different key than the installed app.

## Release Signing

The project supports optional release signing via `keystore.properties`.

1. Copy template:
   - `keystore.properties.example` -> `keystore.properties`
2. Generate keystore (example):

```bash
keytool -genkeypair -v -keystore keystore/finance-release.jks -alias finance_release -keyalg RSA -keysize 2048 -validity 10000
```

3. Build release:

```bash
./gradlew :app:assembleRelease
```

If `keystore.properties` is absent, local release build falls back to debug signing for easier personal updates.

## Notes

- App data is stored locally in Room.
- Notification permission (`POST_NOTIFICATIONS`) is requested on Android 13+.
- If you switch language, UI labels update immediately via Compose state.
