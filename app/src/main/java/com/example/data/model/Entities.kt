package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val invoiceType: String, // "TAX_INVOICE", "PROFORMA", "QUOTATION", "DELIVERY_CHALLAN"
    val partyId: Long,
    val partyName: String,
    val partyGstin: String,
    val partyPhone: String,
    val partyState: String,
    val isInterState: Boolean, // false = CGST + SGST (Intra-state), true = IGST (Inter-state)
    val dateEpoch: Long,
    val dueDateEpoch: Long,
    val itemsSummary: String,
    val itemsCount: Int,
    val subtotal: Double,
    val discount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalAmount: Double,
    val amountPaid: Double,
    val balanceDue: Double,
    val paymentStatus: String, // "PAID", "PARTIAL", "UNPAID", "OVERDUE"
    val paymentMode: String, // "UPI", "NEFT_RTGS", "CASH", "CHEQUE", "CREDIT"
    val eWayBillNumber: String? = null,
    val notes: String? = null
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sku: String,
    val hsnCode: String,
    val category: String,
    val unit: String, // "Pcs", "Box", "Kg", "Mtr", "Bags", "Ltr"
    val purchasePrice: Double,
    val wholesalePrice: Double,
    val mrp: Double,
    val gstRatePercent: Double, // 0.0, 5.0, 12.0, 18.0, 28.0
    val currentStock: Double,
    val minReorderLevel: Double,
    val preferredSupplier: String
)

@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val tradeName: String,
    val type: String, // "CUSTOMER", "SUPPLIER"
    val gstin: String,
    val panNumber: String,
    val phone: String,
    val email: String,
    val address: String,
    val stateName: String,
    val stateCode: String, // e.g. "27" for Maharashtra
    val creditLimit: Double,
    val outstandingBalance: Double, // Positive = Receivable (Debtor), Negative = Payable (Creditor)
    val paymentTermsDays: Int,
    val overdueDays: Int = 0
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partyId: Long,
    val partyName: String,
    val invoiceNumber: String?,
    val amount: Double,
    val paymentMode: String, // "UPI", "NEFT_RTGS", "CASH", "CHEQUE"
    val referenceNumber: String,
    val dateEpoch: Long,
    val notes: String?
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Factory Rent", "Freight & Logistics", "Utilities & Power", "Staff Wages", "Machine Spares", "Packaging"
    val amount: Double,
    val dateEpoch: Long,
    val isGstClaimable: Boolean,
    val gstAmount: Double,
    val paymentMode: String,
    val vendorName: String?
)

@Entity(tableName = "manufacturing_orders")
data class ManufacturingOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchCode: String,
    val finishedGoodName: String,
    val targetQuantity: Double,
    val unit: String,
    val status: String, // "PLANNED", "IN_PRODUCTION", "COMPLETED", "QUALITY_CHECK"
    val rawMaterialsUsedSummary: String,
    val estimatedCostPerUnit: Double,
    val startDateEpoch: Long,
    val targetDateEpoch: Long
)

@Entity(tableName = "copilot_messages")
data class CopilotMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER", "COPILOT"
    val messageText: String,
    val actionType: String? = null, // "PAYMENT_REMINDER", "RESTOCK_ALERT", "GST_SUMMARY", "NONE"
    val actionPayload: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
