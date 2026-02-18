package com.example.financeapp.reminder

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financeapp.data.database.FinanceDatabase
import com.example.financeapp.domain.formatDate
import java.util.concurrent.TimeUnit

class PaymentReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        createNotificationChannel(applicationContext)

        if (!hasNotificationPermission(applicationContext)) {
            return Result.success()
        }

        val now = System.currentTimeMillis()
        val dueUntil = now + TimeUnit.DAYS.toMillis(2)

        val database = FinanceDatabase.getDatabase(applicationContext)
        val dueCredits = database.creditDao().getDueCreditsUntil(dueUntil)

        if (dueCredits.isEmpty()) {
            return Result.success()
        }

        dueCredits.forEach { credit ->
            val dueDate = credit.paymentDueDate ?: return@forEach
            val title = "Платіж за кредитом: ${credit.name}"
            val message = if (dueDate < now) {
                "Платіж прострочено з ${formatDate(dueDate)}"
            } else {
                "Потрібно сплатити до ${formatDate(dueDate)}"
            }

            showNotification(
                context = applicationContext,
                notificationId = credit.id.toInt().coerceAtLeast(1),
                title = title,
                message = message
            )
        }

        return Result.success()
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Нагадування про платежі",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Нагадування про кредити з датою оплати"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        if (!hasNotificationPermission(context)) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notifySafely(context, notificationId, notification)
    }

    @SuppressLint("MissingPermission")
    private fun notifySafely(
        context: Context,
        notificationId: Int,
        notification: Notification
    ) {
        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "credit_payment_reminders"
    }
}
