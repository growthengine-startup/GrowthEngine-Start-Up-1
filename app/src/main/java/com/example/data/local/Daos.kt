package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY dateEpoch DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE paymentStatus = 'OVERDUE' OR paymentStatus = 'UNPAID' ORDER BY dueDateEpoch ASC")
    fun getPendingInvoices(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getCount(): Int
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE currentStock <= minReorderLevel ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Query("UPDATE products SET currentStock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties ORDER BY name ASC")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE type = 'CUSTOMER' ORDER BY outstandingBalance DESC")
    fun getCustomers(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE type = 'SUPPLIER' ORDER BY name ASC")
    fun getSuppliers(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id LIMIT 1")
    suspend fun getPartyById(id: Long): PartyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Update
    suspend fun updateParty(party: PartyEntity)

    @Query("UPDATE parties SET outstandingBalance = outstandingBalance - :amount WHERE id = :id")
    suspend fun reduceOutstanding(id: Long, amount: Double)

    @Delete
    suspend fun deleteParty(party: PartyEntity)

    @Query("SELECT COUNT(*) FROM parties")
    suspend fun getCount(): Int
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY dateEpoch DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Query("SELECT COUNT(*) FROM payments")
    suspend fun getCount(): Int
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateEpoch DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getCount(): Int
}

@Dao
interface ManufacturingDao {
    @Query("SELECT * FROM manufacturing_orders ORDER BY startDateEpoch DESC")
    fun getAllBatches(): Flow<List<ManufacturingOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ManufacturingOrderEntity): Long

    @Query("UPDATE manufacturing_orders SET status = :status WHERE id = :id")
    suspend fun updateBatchStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM manufacturing_orders")
    suspend fun getCount(): Int
}

@Dao
interface CopilotDao {
    @Query("SELECT * FROM copilot_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<CopilotMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CopilotMessageEntity): Long

    @Query("DELETE FROM copilot_messages")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM copilot_messages")
    suspend fun getCount(): Int
}
