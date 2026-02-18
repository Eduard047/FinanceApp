package com.example.financeapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.financeapp.data.dao.CategoryDao
import com.example.financeapp.data.dao.CreditDao
import com.example.financeapp.data.dao.CreditPaymentDao
import com.example.financeapp.data.dao.TransactionDao
import com.example.financeapp.data.entity.CategoryEntity
import com.example.financeapp.data.entity.CreditAccountEntity
import com.example.financeapp.data.entity.CreditPaymentEntity
import com.example.financeapp.data.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        CreditAccountEntity::class,
        CreditPaymentEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(FinanceTypeConverters::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun creditDao(): CreditDao
    abstract fun creditPaymentDao(): CreditPaymentDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE credit_accounts ADD COLUMN installmentCount INTEGER")
                db.execSQL("ALTER TABLE credit_accounts ADD COLUMN paidInstallments INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE credit_accounts ADD COLUMN paymentDueDate INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_accounts_paymentDueDate ON credit_accounts(paymentDueDate)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE credit_accounts ADD COLUMN note TEXT")
            }
        }

        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
