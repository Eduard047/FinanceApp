package com.example.financeapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.financeapp.data.database.FinanceDatabase
import com.example.financeapp.data.repository.CreditRepository
import com.example.financeapp.data.repository.FinanceRepository
import com.example.financeapp.navigation.FinanceNavGraph
import com.example.financeapp.reminder.CreditReminderScheduler
import com.example.financeapp.ui.theme.FinanceAppTheme

class MainActivity : ComponentActivity() {
    private val database: FinanceDatabase by lazy {
        FinanceDatabase.getDatabase(applicationContext)
    }

    private val financeRepository: FinanceRepository by lazy {
        FinanceRepository(
            categoryDao = database.categoryDao(),
            transactionDao = database.transactionDao()
        )
    }

    private val creditRepository: CreditRepository by lazy {
        CreditRepository(
            database = database,
            creditDao = database.creditDao(),
            creditPaymentDao = database.creditPaymentDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()
        CreditReminderScheduler.schedule(applicationContext)

        setContent {
            FinanceAppTheme {
                FinanceNavGraph(
                    financeRepository = financeRepository,
                    creditRepository = creditRepository
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST
            )
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001
    }
}
