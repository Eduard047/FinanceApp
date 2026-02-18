package com.example.financeapp.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object CreditReminderScheduler {
    private const val WORK_NAME = "credit_payment_reminder_work"
    private const val IMMEDIATE_WORK_NAME = "credit_payment_reminder_once"

    fun schedule(context: Context) {
        val immediateRequest = OneTimeWorkRequestBuilder<PaymentReminderWorker>().build()
        val request = PeriodicWorkRequestBuilder<PaymentReminderWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }
}
