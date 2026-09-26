package com.example.data.model

import java.util.UUID

enum class AdminRole(val label: String, val level: Int) {
    SUPER_ADMIN("Super Admin", 100),
    PLATFORM_ADMIN("Platform Admin", 80),
    FINANCE_ADMIN("Billing & Finance Admin", 60),
    SUPPORT_ADMIN("Support Specialist", 40),
    AUDITOR("Read-Only Auditor", 20)
}

enum class SubscriptionStatus(val label: String, val badgeColorHex: Long) {
    ACTIVE("Active", 0xFF059669),
    TRIAL("Trial", 0xFF2563EB),
    PENDING("Pending", 0xFFD97706),
    EXPIRED("Expired", 0xFFDC2626),
    CANCELLED("Cancelled", 0xFF6B7280),
    FAILED("Payment Failed", 0xFFE11D48),
    PAUSED("Paused", 0xFF7C3AED)
}

data class BusinessAccount(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val tradeName: String,
    val ownerEmail: String,
    val ownerPhone: String,
    val gstin: String,
    val state: String,
    val registeredDateEpoch: Long,
    val lastActiveEpoch: Long,
    val status: String = "ACTIVE", // ACTIVE, SUSPENDED, PENDING_VERIFICATION
    val planTier: SubscriptionTier = SubscriptionTier.GROWTH_PRO,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val monthlyRevenueInr: Double = 0.0,
    val totalInvoicesCount: Int = 0,
    val totalProductsCount: Int = 0,
    val staffSeatsCount: Int = 1,
    val aiTokensUsed: Long = 0L,
    val aiMonthlyQuota: Long = 250_000L,
    val isAutoRenew: Boolean = true,
    val renewalDateEpoch: Long = System.currentTimeMillis() + (180L * 86400000L),
    val razorpaySubscriptionId: String = "sub_${UUID.randomUUID().toString().take(10)}"
)

data class PaymentTransactionRecord(
    val id: String = "pay_${UUID.randomUUID().toString().take(12)}",
    val businessId: String,
    val businessName: String,
    val userEmail: String,
    val amountInr: Double,
    val gstAmountInr: Double = amountInr * 0.18,
    val totalWithGstInr: Double = amountInr * 1.18,
    val tier: SubscriptionTier,
    val billingCycle: BillingCycle,
    val status: String = "CAPTURED", // CAPTURED, FAILED, REFUNDED, PENDING
    val paymentMode: String = "UPI (Google Pay)",
    val razorpayPaymentId: String = "pay_${UUID.randomUUID().toString().take(14)}",
    val razorpayOrderId: String = "order_${UUID.randomUUID().toString().take(14)}",
    val timestamp: Long = System.currentTimeMillis(),
    val failureReason: String? = null,
    val invoiceUrl: String = "https://growthengine.in/invoices/inv_2026_${(1000..9999).random()}"
)

data class AiUsageLog(
    val id: String = UUID.randomUUID().toString(),
    val businessId: String,
    val businessName: String,
    val userEmail: String,
    val featureName: String, // "GST Invoicing Copilot", "Khata Payment Collector", "BOM Optimizer", "Financial Insights"
    val modelName: String = "Gemini 2.5 Flash",
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int = promptTokens + completionTokens,
    val estimatedCostInr: Double = (totalTokens / 1000.0) * 0.045, // approx cost
    val latencyMs: Long = (300..1200).random().toLong(),
    val timestamp: Long = System.currentTimeMillis()
)

data class GlobalAiMetrics(
    val totalTokensConsumedThisMonth: Long = 18_450_200L,
    val totalMonthlyLimit: Long = 50_000_000L,
    val totalCostEstimatedInr: Double = 830.25,
    val totalAiRequests: Long = 42_890L,
    val activeAiUsersCount: Int = 1_280,
    val averageLatencyMs: Int = 460
)

data class NotificationBroadcast(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: String = "BROADCAST", // BROADCAST, TARGETED, ALERT, BILLING_REMINDER
    val targetAudience: String = "ALL_USERS", // ALL_USERS, FREE_TIER, PRO_TIER, OVERDUE_USERS
    val channel: String = "IN_APP_PUSH", // IN_APP, PUSH, BOTH
    val sentAtEpoch: Long = System.currentTimeMillis(),
    val targetCount: Int = 1420,
    val openCount: Int = 980,
    val status: String = "SENT" // SENT, SCHEDULED, DRAFT
)

data class ActivityAuditLog(
    val id: String = UUID.randomUUID().toString(),
    val actorEmail: String,
    val actorRole: String,
    val action: String, // "USER_LOGIN", "PLAN_UPGRADE", "AI_QUOTA_OVERRIDE", "BUSINESS_SUSPENDED", "INVOICE_GENERATED"
    val targetEntity: String,
    val details: String,
    val ipAddress: String = "103.14.120.${(10..250).random()}",
    val severity: String = "INFO", // INFO, WARN, CRITICAL, AUDIT
    val timestamp: Long = System.currentTimeMillis()
)

data class SupportTicket(
    val id: String = "TICK-${(1000..9999).random()}",
    val businessName: String,
    val userEmail: String,
    val subject: String,
    val category: String = "Billing & Payments", // Billing, GST Invoicing, AI Copilot, Inventory, Sync
    val priority: String = "HIGH", // LOW, MEDIUM, HIGH, URGENT
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    val createdAtEpoch: Long = System.currentTimeMillis() - (4L * 3600000L),
    val lastResponseEpoch: Long = System.currentTimeMillis() - (1L * 3600000L),
    val messagesCount: Int = 3
)

data class SystemConfiguration(
    val maintenanceMode: Boolean = false,
    val allowNewSignups: Boolean = true,
    val forceAppUpdateMinVersion: String = "1.0.0",
    val globalAiRateLimitPerMin: Int = 120,
    val defaultTrialPeriodDays: Int = 14,
    val defaultFreeMonthlyInvoices: Int = 50,
    val razorpayLiveMode: Boolean = true,
    val supabaseRealtimeEnabled: Boolean = true,
    val systemAnnouncementBanner: String? = "⚡ GrowthEngine v2.4 Live: High-speed GST E-Invoice & AI Copilot released."
)
