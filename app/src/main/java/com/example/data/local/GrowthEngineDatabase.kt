package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        InvoiceEntity::class,
        ProductEntity::class,
        PartyEntity::class,
        PaymentEntity::class,
        ExpenseEntity::class,
        ManufacturingOrderEntity::class,
        CopilotMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GrowthEngineDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun productDao(): ProductDao
    abstract fun partyDao(): PartyDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun manufacturingDao(): ManufacturingDao
    abstract fun copilotDao(): CopilotDao

    companion object {
        @Volatile
        private var INSTANCE: GrowthEngineDatabase? = null

        fun getDatabase(context: Context): GrowthEngineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrowthEngineDatabase::class.java,
                    "growthengine_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
