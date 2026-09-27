package com.example.data.model

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val tagline: String,
    val monthlyPrice: Double,
    val annualPricePerMonth: Double,
    val isPopular: Boolean,
    val badge: String?,
    val aiMonthlyTokenQuota: Long
) {
    STARTER_FREE(
        id = "starter_free",
        title = "Free Starter",
        tagline = "Essential GST billing & counter sales for small shops",
        monthlyPrice = 0.0,
        annualPricePerMonth = 0.0,
        isPopular = false,
        badge = null,
        aiMonthlyTokenQuota = 10_000L
    ),
    GROWTH_PRO(
        id = "growth_pro",
        title = "Growth Pro",
        tagline = "Complete ledger, stock & automated WhatsApp collections",
        monthlyPrice = 799.0,
        annualPricePerMonth = 599.0,
        isPopular = true,
        badge = "MOST POPULAR",
        aiMonthlyTokenQuota = 250_000L
    ),
    ENTERPRISE_MUNIM(
        id = "enterprise_munim",
        title = "Enterprise Munim",
        tagline = "AI Copilot Munim, Manufacturing BOM & multi-user teams",
        monthlyPrice = 1999.0,
        annualPricePerMonth = 1499.0,
        isPopular = false,
        badge = "ALL-INCLUSIVE",
        aiMonthlyTokenQuota = 2_000_000L
    );

    fun getAnnualBilledTotal(): Double = annualPricePerMonth * 12

    fun getFeaturesList(): List<String> = when (this) {
        STARTER_FREE -> listOf(
            "Up to 50 GST invoices per month",
            "Single device counter POS",
            "Up to 100 product SKUs",
            "Basic customer ledger (Khata)",
            "Local offline storage",
            "Standard thermal receipt printing"
        )
        GROWTH_PRO -> listOf(
            "Unlimited GST & e-invoices",
            "Fast Barcode & UPI POS",
            "Up to 10,000 product SKUs",
            "Automated WhatsApp payment reminders",
            "Live Supabase Cloud Sync & automatic backup",
            "Input Tax Credit (GSTR-1, 3B) reports",
            "Up to 3 staff logins with role controls",
            "E-Way bill format generation"
        )
        ENTERPRISE_MUNIM -> listOf(
            "Everything in Growth Pro",
            "AI Business Copilot (Hindi & English)",
            "Manufacturing & Raw Materials (BOM)",
            "Multi-godown / warehouse management",
            "MSMED Act 45-day compound interest tracker",
            "Dedicated CA tax summary export",
            "Unlimited staff seats & activity audit logs",
            "24/7 Priority WhatsApp & Phone support"
        )
    }
}

enum class BillingCycle {
    MONTHLY,
    ANNUAL
}

enum class PaymentStatus {
    PAID,
    PENDING,
    FAILED,
    CANCELLED,
    REFUNDED
}

data class CurrentSubscription(
    val tier: SubscriptionTier = SubscriptionTier.GROWTH_PRO,
    val cycle: BillingCycle = BillingCycle.ANNUAL,
    val isActive: Boolean = true,
    val startDateEpoch: Long = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000), // 15 days ago
    val renewalDateEpoch: Long = System.currentTimeMillis() + (350L * 24 * 60 * 60 * 1000), // ~1 year from now
    val autoRenew: Boolean = true,
    val paymentMethod: String = "UPI Autopay (GPay · dezaa@okhdfcbank)",
    val razorpaySubscriptionId: String = "sub_Q7vX9mL2kP8j1"
)

data class BillingReceipt(
    val invoiceId: String,
    val receiptNumber: String,
    val dateEpoch: Long,
    val tier: SubscriptionTier,
    val cycle: BillingCycle,
    val baseAmount: Double,
    val gstRatePercent: Double = 18.0,
    val status: PaymentStatus,
    val paymentMode: String,
    val razorpayPaymentId: String,
    val razorpayOrderId: String
) {
    val gstAmount: Double get() = (baseAmount * gstRatePercent) / 100.0
    val totalPaid: Double get() = baseAmount + gstAmount
}
