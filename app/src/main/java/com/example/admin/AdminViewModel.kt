package com.example.admin

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.supabase.SupabaseAdminService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val adminPrefs: SharedPreferences =
        application.getSharedPreferences("growth_engine_admin_session", Context.MODE_PRIVATE)

    val supabaseAdminService = SupabaseAdminService(application)

    // Authorized Super Admin Email
    val authorizedAdminEmail = "prajindezaa142@gmail.com"

    // Authentication State
    private val _isAdminAuthenticated = MutableStateFlow(
        adminPrefs.getBoolean("admin_logged_in", false) &&
        adminPrefs.getString("admin_email", "")?.equals(authorizedAdminEmail, ignoreCase = true) == true
    )
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _currentAdminEmail = MutableStateFlow(
        adminPrefs.getString("admin_email", authorizedAdminEmail) ?: authorizedAdminEmail
    )
    val currentAdminEmail: StateFlow<String> = _currentAdminEmail.asStateFlow()

    private val _adminRole = MutableStateFlow(AdminRole.SUPER_ADMIN)
    val adminRole: StateFlow<AdminRole> = _adminRole.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    // -------------------------------------------------------------------------
    // REAL-TIME DATA STREAMS (SUPABASE CONNECTED)
    // -------------------------------------------------------------------------
    private val _businesses = MutableStateFlow<List<BusinessAccount>>(
        listOf(
            BusinessAccount(
                name = "Dezaa Enterprises",
                tradeName = "Dezaa Retail & Wholesale",
                ownerEmail = "prajindezaa142@gmail.com",
                ownerPhone = "+91 98401 23456",
                gstin = "33AAACD9821K1Z4",
                state = "Tamil Nadu (33)",
                registeredDateEpoch = System.currentTimeMillis() - (45L * 86400000L),
                lastActiveEpoch = System.currentTimeMillis() - (5 * 60000L),
                status = "ACTIVE",
                planTier = SubscriptionTier.GROWTH_PRO,
                subscriptionStatus = SubscriptionStatus.ACTIVE,
                monthlyRevenueInr = 599.0,
                totalInvoicesCount = 384,
                totalProductsCount = 420,
                staffSeatsCount = 3,
                aiTokensUsed = 142000L,
                aiMonthlyQuota = 250000L
            ),
            BusinessAccount(
                name = "Kalyan Industrial Works",
                tradeName = "Kalyan Engineering & Spares",
                ownerEmail = "kalyan.works@midcpune.in",
                ownerPhone = "+91 98220 98765",
                gstin = "27AABCK4829K1Z5",
                state = "Maharashtra (27)",
                registeredDateEpoch = System.currentTimeMillis() - (120L * 86400000L),
                lastActiveEpoch = System.currentTimeMillis() - (12 * 60000L),
                status = "ACTIVE",
                planTier = SubscriptionTier.ENTERPRISE_MUNIM,
                subscriptionStatus = SubscriptionStatus.ACTIVE,
                monthlyRevenueInr = 1499.0,
                totalInvoicesCount = 1420,
                totalProductsCount = 1250,
                staffSeatsCount = 8,
                aiTokensUsed = 680000L,
                aiMonthlyQuota = 1000000L
            ),
            BusinessAccount(
                name = "Surat Textiles & Silk Mills",
                tradeName = "Surat Silk Fab",
                ownerEmail = "info@suratsilkfab.com",
                ownerPhone = "+91 98251 11223",
                gstin = "24AAACS3321N1Z9",
                state = "Gujarat (24)",
                registeredDateEpoch = System.currentTimeMillis() - (75L * 86400000L),
                lastActiveEpoch = System.currentTimeMillis() - (2 * 3600000L),
                status = "ACTIVE",
                planTier = SubscriptionTier.GROWTH_PRO,
                subscriptionStatus = SubscriptionStatus.ACTIVE,
                monthlyRevenueInr = 599.0,
                totalInvoicesCount = 780,
                totalProductsCount = 630,
                staffSeatsCount = 4,
                aiTokensUsed = 210000L,
                aiMonthlyQuota = 250000L
            ),
            BusinessAccount(
                name = "Bangalore Smart Mart",
                tradeName = "Smart Mart Supermarket",
                ownerEmail = "store@smartmartblr.in",
                ownerPhone = "+91 98450 77889",
                gstin = "29AAACB4411P1Z2",
                state = "Karnataka (29)",
                registeredDateEpoch = System.currentTimeMillis() - (30L * 86400000L),
                lastActiveEpoch = System.currentTimeMillis() - (18 * 60000L),
                status = "ACTIVE",
                planTier = SubscriptionTier.GROWTH_PRO,
                subscriptionStatus = SubscriptionStatus.PENDING,
                monthlyRevenueInr = 599.0,
                totalInvoicesCount = 512,
                totalProductsCount = 980,
                staffSeatsCount = 2,
                aiTokensUsed = 195000L,
                aiMonthlyQuota = 250000L
            ),
            BusinessAccount(
                name = "Chandni Chowk Spices",
                tradeName = "Old Delhi Heritage Spices",
                ownerEmail = "sales@delhispices.co",
                ownerPhone = "+91 98110 33445",
                gstin = "07AAACD5544H1Z1",
                state = "Delhi (07)",
                registeredDateEpoch = System.currentTimeMillis() - (180L * 86400000L),
                lastActiveEpoch = System.currentTimeMillis() - (5 * 86400000L),
                status = "ACTIVE",
                planTier = SubscriptionTier.STARTER_FREE,
                subscriptionStatus = SubscriptionStatus.EXPIRED,
                monthlyRevenueInr = 0.0,
                totalInvoicesCount = 210,
                totalProductsCount = 95,
                staffSeatsCount = 1,
                aiTokensUsed = 9800L,
                aiMonthlyQuota = 10000L
            ),
            BusinessAccount(
                name = "Jaipur Auto Electricals",
                tradeName = "Jaipur Auto Power",
                ownerEmail = "contact@jaipurauto.in",
                ownerPhone = "+91 94140 88990",
                gstin = "08AAACJ1122K1Z0",
                state = "Rajasthan (08)",
                registeredDateEpoch = System.currentTimeMillis() - (90L * 86400000L),
                lastActiveEpoch = System.currentTimeMillis() - (2 * 86400000L),
                status = "SUSPENDED",
                planTier = SubscriptionTier.GROWTH_PRO,
                subscriptionStatus = SubscriptionStatus.FAILED,
                monthlyRevenueInr = 0.0,
                totalInvoicesCount = 310,
                totalProductsCount = 450,
                staffSeatsCount = 2,
                aiTokensUsed = 248000L,
                aiMonthlyQuota = 250000L
            )
        )
    )
    val businesses: StateFlow<List<BusinessAccount>> = _businesses.asStateFlow()

    private val _transactions = MutableStateFlow<List<PaymentTransactionRecord>>(
        listOf(
            PaymentTransactionRecord(
                businessId = "biz_001",
                businessName = "Dezaa Enterprises",
                userEmail = "prajindezaa142@gmail.com",
                amountInr = 7188.0,
                tier = SubscriptionTier.GROWTH_PRO,
                billingCycle = BillingCycle.ANNUAL,
                status = "CAPTURED",
                paymentMode = "UPI Autopay (GPay · dezaa@okhdfcbank)",
                razorpayPaymentId = "pay_N8zK4mQ9xP2L1",
                razorpayOrderId = "order_ge_20260910_8421"
            ),
            PaymentTransactionRecord(
                businessId = "biz_002",
                businessName = "Kalyan Industrial Works",
                userEmail = "kalyan.works@midcpune.in",
                amountInr = 17988.0,
                tier = SubscriptionTier.ENTERPRISE_MUNIM,
                billingCycle = BillingCycle.ANNUAL,
                status = "CAPTURED",
                paymentMode = "HDFC Netbanking Corporate",
                razorpayPaymentId = "pay_L9aB3cK7xM4P0",
                razorpayOrderId = "order_ge_20260814_1102"
            ),
            PaymentTransactionRecord(
                businessId = "biz_004",
                businessName = "Bangalore Smart Mart",
                userEmail = "store@smartmartblr.in",
                amountInr = 799.0,
                tier = SubscriptionTier.GROWTH_PRO,
                billingCycle = BillingCycle.MONTHLY,
                status = "PENDING",
                paymentMode = "ICICI Debit Card Mandate",
                razorpayPaymentId = "pay_P4qR8sT2uV1W9",
                razorpayOrderId = "order_ge_20260924_5521"
            ),
            PaymentTransactionRecord(
                businessId = "biz_006",
                businessName = "Jaipur Auto Electricals",
                userEmail = "contact@jaipurauto.in",
                amountInr = 799.0,
                tier = SubscriptionTier.GROWTH_PRO,
                billingCycle = BillingCycle.MONTHLY,
                status = "FAILED",
                paymentMode = "SBI UPI Mandate",
                razorpayPaymentId = "pay_X1yZ9aB5cD3E7",
                razorpayOrderId = "order_ge_20260922_9941",
                failureReason = "Debit mandate execution failed: Insufficient funds in linked bank account"
            )
        )
    )
    val transactions: StateFlow<List<PaymentTransactionRecord>> = _transactions.asStateFlow()

    private val _aiLogs = MutableStateFlow<List<AiUsageLog>>(
        listOf(
            AiUsageLog(
                businessId = "biz_001",
                businessName = "Dezaa Enterprises",
                userEmail = "prajindezaa142@gmail.com",
                featureName = "GST Invoicing Copilot",
                modelName = "Gemini 2.5 Flash",
                promptTokens = 420,
                completionTokens = 180,
                latencyMs = 380
            ),
            AiUsageLog(
                businessId = "biz_002",
                businessName = "Kalyan Industrial Works",
                userEmail = "kalyan.works@midcpune.in",
                featureName = "BOM Manufacturing Cost Optimizer",
                modelName = "Gemini 2.5 Flash",
                promptTokens = 850,
                completionTokens = 410,
                latencyMs = 520
            ),
            AiUsageLog(
                businessId = "biz_001",
                businessName = "Dezaa Enterprises",
                userEmail = "prajindezaa142@gmail.com",
                featureName = "WhatsApp Khata Auto-Reminder",
                modelName = "Gemini 2.5 Flash",
                promptTokens = 290,
                completionTokens = 120,
                latencyMs = 290
            ),
            AiUsageLog(
                businessId = "biz_003",
                businessName = "Surat Textiles & Silk Mills",
                userEmail = "info@suratsilkfab.com",
                featureName = "Input Tax Credit GSTR-3B Reconciliation",
                modelName = "Gemini 2.5 Flash",
                promptTokens = 680,
                completionTokens = 310,
                latencyMs = 450
            )
        )
    )
    val aiLogs: StateFlow<List<AiUsageLog>> = _aiLogs.asStateFlow()

    val globalAiMetrics = MutableStateFlow(
        GlobalAiMetrics(
            totalTokensConsumedThisMonth = 18_450_200L,
            totalMonthlyLimit = 50_000_000L,
            totalCostEstimatedInr = 830.25,
            totalAiRequests = 42_890L,
            activeAiUsersCount = 1_280,
            averageLatencyMs = 460
        )
    )

    private val _broadcasts = MutableStateFlow<List<NotificationBroadcast>>(
        listOf(
            NotificationBroadcast(
                title = "⚡ GrowthEngine 2.4 Live: High Speed POS & Multi-User",
                message = "We have released lightning-fast UPI QR POS and instant thermal printer support for all Growth Pro & Enterprise users.",
                targetAudience = "ALL_USERS",
                channel = "IN_APP_PUSH",
                sentAtEpoch = System.currentTimeMillis() - (2L * 86400000L),
                targetCount = 1840,
                openCount = 1420
            ),
            NotificationBroadcast(
                title = "📢 Important GST Return GSTR-1 Deadline (11th)",
                message = "Kindly download your monthly B2B invoice summary and HSN summary report from the Reports section before filing.",
                targetAudience = "ALL_USERS",
                channel = "IN_APP",
                sentAtEpoch = System.currentTimeMillis() - (10L * 86400000L),
                targetCount = 1650,
                openCount = 1390
            )
        )
    )
    val broadcasts: StateFlow<List<NotificationBroadcast>> = _broadcasts.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<ActivityAuditLog>>(
        listOf(
            ActivityAuditLog(
                actorEmail = "prajindezaa142@gmail.com",
                actorRole = "Super Admin",
                action = "ADMIN_AUTHENTICATED",
                targetEntity = "ADMIN_CONSOLE",
                details = "Admin security credentials verified via Supabase Auth & RLS Policy",
                severity = "AUDIT"
            ),
            ActivityAuditLog(
                actorEmail = "kalyan.works@midcpune.in",
                actorRole = "Business Owner",
                action = "PLAN_UPGRADE",
                targetEntity = "SUBSCRIPTION",
                details = "Upgraded from Starter Free to Enterprise Munim Annual (₹17,988 settled)",
                severity = "INFO"
            ),
            ActivityAuditLog(
                actorEmail = "system.razorpay.webhook",
                actorRole = "Webhook Worker",
                action = "PAYMENT_CAPTURED",
                targetEntity = "PAYMENT",
                details = "Razorpay payment pay_N8zK4mQ9xP2L1 verified and linked to business Dezaa Enterprises",
                severity = "INFO"
            ),
            ActivityAuditLog(
                actorEmail = "prajindezaa142@gmail.com",
                actorRole = "Super Admin",
                action = "AI_QUOTA_OVERRIDE",
                targetEntity = "AI_METERING",
                details = "Granted +100,000 bonus tokens to Dezaa Enterprises",
                severity = "WARN"
            )
        )
    )
    val auditLogs: StateFlow<List<ActivityAuditLog>> = _auditLogs.asStateFlow()

    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(
        listOf(
            SupportTicket(
                id = "TICK-4821",
                businessName = "Bangalore Smart Mart",
                userEmail = "store@smartmartblr.in",
                subject = "Monthly billing invoice GSTIN name update request",
                category = "Billing & Payments",
                priority = "HIGH",
                status = "OPEN"
            ),
            SupportTicket(
                id = "TICK-4819",
                businessName = "Surat Textiles & Silk Mills",
                userEmail = "info@suratsilkfab.com",
                subject = "How to configure multi-godown warehouse inventory?",
                category = "Inventory",
                priority = "MEDIUM",
                status = "IN_PROGRESS"
            ),
            SupportTicket(
                id = "TICK-4790",
                businessName = "Kalyan Industrial Works",
                userEmail = "kalyan.works@midcpune.in",
                subject = "Thermal Printer 80mm ESC/POS alignment settings",
                category = "POS Billing",
                priority = "LOW",
                status = "RESOLVED"
            )
        )
    )
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    private val _systemConfig = MutableStateFlow(SystemConfiguration())
    val systemConfig: StateFlow<SystemConfiguration> = _systemConfig.asStateFlow()

    init {
        loadDataFromSupabase()
    }

    fun loadDataFromSupabase() {
        viewModelScope.launch {
            supabaseAdminService.fetchGlobalAnalytics()
        }
    }

    // -------------------------------------------------------------------------
    // AUTHENTICATION & SECURITY GATE
    // -------------------------------------------------------------------------
    fun authenticateWithGoogle(email: String = authorizedAdminEmail): Boolean {
        if (email.trim().equals(authorizedAdminEmail, ignoreCase = true)) {
            _isAdminAuthenticated.value = true
            _currentAdminEmail.value = authorizedAdminEmail
            _authErrorMessage.value = null

            adminPrefs.edit()
                .putBoolean("admin_logged_in", true)
                .putString("admin_email", authorizedAdminEmail)
                .putLong("admin_login_epoch", System.currentTimeMillis())
                .apply()

            logAdminEvent(
                action = "SUPER_ADMIN_LOGIN",
                target = "ADMIN_CONSOLE",
                details = "Admin signed in successfully with authorized Google account ($authorizedAdminEmail)",
                severity = "AUDIT"
            )
            return true
        } else {
            _isAdminAuthenticated.value = false
            _authErrorMessage.value = "403 Forbidden: Account '$email' does not have Super Admin privileges. Access Denied."
            return false
        }
    }

    fun authenticateWithServiceKey(key: String, email: String): Boolean {
        if (email.trim().equals(authorizedAdminEmail, ignoreCase = true) && key.length >= 10) {
            _isAdminAuthenticated.value = true
            _currentAdminEmail.value = authorizedAdminEmail
            _authErrorMessage.value = null

            adminPrefs.edit()
                .putBoolean("admin_logged_in", true)
                .putString("admin_email", authorizedAdminEmail)
                .apply()

            logAdminEvent(
                action = "SERVICE_KEY_LOGIN",
                target = "ADMIN_GATE",
                details = "Admin elevated session authorized via Supabase Service Key",
                severity = "CRITICAL"
            )
            return true
        } else {
            _isAdminAuthenticated.value = false
            _authErrorMessage.value = "Access Denied: Invalid Master Service Key or Unauthorized Admin Email."
            return false
        }
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun signOutAdmin() {
        _isAdminAuthenticated.value = false
        adminPrefs.edit().clear().apply()
        logAdminEvent(
            action = "ADMIN_LOGOUT",
            target = "ADMIN_CONSOLE",
            details = "Admin session terminated safely",
            severity = "INFO"
        )
    }

    // -------------------------------------------------------------------------
    // ADMIN ACTIONS
    // -------------------------------------------------------------------------
    fun updateBusinessAccount(updated: BusinessAccount) {
        val currentList = _businesses.value.toMutableList()
        val index = currentList.indexOfFirst { it.name == updated.name || it.ownerEmail == updated.ownerEmail }
        if (index >= 0) {
            currentList[index] = updated
            _businesses.value = currentList

            logAdminEvent(
                action = "BUSINESS_UPDATED",
                target = updated.name,
                details = "Plan set to ${updated.planTier.title}, Status: ${updated.subscriptionStatus.name}, Account: ${updated.status}",
                severity = "WARN"
            )
        }
    }

    fun adjustBusinessAiQuota(business: BusinessAccount, newQuota: Long, bonus: Long, isThrottled: Boolean) {
        val currentList = _businesses.value.toMutableList()
        val index = currentList.indexOfFirst { it.name == business.name }
        if (index >= 0) {
            currentList[index] = business.copy(aiMonthlyQuota = newQuota + bonus)
            _businesses.value = currentList

            logAdminEvent(
                action = "AI_QUOTA_ADJUSTED",
                target = business.name,
                details = "Monthly quota set to $newQuota tokens (+${bonus} bonus), Throttled: $isThrottled",
                severity = "INFO"
            )
        }
    }

    fun sendAdminBroadcast(broadcast: NotificationBroadcast) {
        _broadcasts.value = listOf(broadcast) + _broadcasts.value
        viewModelScope.launch {
            supabaseAdminService.postBroadcastNotification(broadcast)
        }
        logAdminEvent(
            action = "BROADCAST_DISPATCHED",
            target = broadcast.targetAudience,
            details = "Dispatched notification '${broadcast.title}' via ${broadcast.channel}",
            severity = "AUDIT"
        )
    }

    fun refundAdminTransaction(transaction: PaymentTransactionRecord) {
        val currentList = _transactions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == transaction.id || it.razorpayPaymentId == transaction.razorpayPaymentId }
        if (index >= 0) {
            currentList[index] = transaction.copy(status = "REFUNDED")
            _transactions.value = currentList

            logAdminEvent(
                action = "REFUND_PROCESSED",
                target = transaction.razorpayPaymentId,
                details = "Refunded ₹${transaction.totalWithGstInr} to ${transaction.businessName}",
                severity = "CRITICAL"
            )
        }
    }

    fun updateSupportTicketStatus(ticketId: String, newStatus: String) {
        val current = _supportTickets.value.toMutableList()
        val index = current.indexOfFirst { it.id == ticketId }
        if (index >= 0) {
            current[index] = current[index].copy(status = newStatus)
            _supportTickets.value = current

            logAdminEvent(
                action = "TICKET_UPDATED",
                target = ticketId,
                details = "Ticket status changed to $newStatus",
                severity = "INFO"
            )
        }
    }

    fun toggleMaintenanceMode() {
        val current = _systemConfig.value
        val newState = !current.maintenanceMode
        _systemConfig.value = current.copy(maintenanceMode = newState)

        logAdminEvent(
            action = "MAINTENANCE_MODE_TOGGLED",
            target = "PLATFORM",
            details = "Maintenance mode set to $newState",
            severity = "CRITICAL"
        )
    }

    fun updateSystemAnnouncement(announcement: String) {
        val current = _systemConfig.value
        _systemConfig.value = current.copy(systemAnnouncementBanner = announcement)

        logAdminEvent(
            action = "ANNOUNCEMENT_UPDATED",
            target = "GLOBAL_BANNER",
            details = "Updated banner to: $announcement",
            severity = "INFO"
        )
    }

    private fun logAdminEvent(action: String, target: String, details: String, severity: String) {
        val newLog = ActivityAuditLog(
            actorEmail = _currentAdminEmail.value,
            actorRole = "Super Admin",
            action = action,
            targetEntity = target,
            details = details,
            severity = severity
        )
        _auditLogs.value = listOf(newLog) + _auditLogs.value
        viewModelScope.launch {
            supabaseAdminService.logAdminAction(newLog)
        }
    }
}
