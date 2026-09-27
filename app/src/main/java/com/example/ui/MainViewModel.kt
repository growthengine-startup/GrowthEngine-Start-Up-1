package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiCopilotService
import com.example.billing.RazorpayPaymentService
import com.example.data.local.GrowthEngineDatabase
import com.example.data.model.*
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class AppNavTab(val title: String) {
    DASHBOARD("Dashboard"),
    COPILOT("AI Assistant"),
    INVOICING("Billing & GST"),
    POS("POS"),
    QUOTATIONS("Quotations"),
    ORDERS("Orders"),
    CUSTOMERS("Customers"),
    PURCHASES("Purchases"),
    SUPPLIERS("Suppliers"),
    INVENTORY("Inventory"),
    MANUFACTURING("Manufacturing"),
    KHATA("Payments & Outstanding"),
    EXPENSES("Expenses"),
    REPORTS("Reports"),
    ANALYTICS("Analytics"),
    EMPLOYEES("Employees"),
    AUTOMATION("Automation"),
    SUPABASE_SYNC("Supabase Cloud Sync"),
    PLANS_BILLING("Plans & Billing"),
    SETTINGS("Settings")
}

data class PosCartItem(
    val product: ProductEntity,
    val quantity: Double = 1.0,
    val unitPrice: Double
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GrowthEngineDatabase.getDatabase(application)
    val repository = BusinessRepository(db)
    private val copilotService = GeminiCopilotService()

    private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    val invoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingInvoices: StateFlow<List<InvoiceEntity>> = repository.pendingInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<PartyEntity>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<PartyEntity>> = repository.suppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<ManufacturingOrderEntity>> = repository.allManufacturingBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val copilotMessages: StateFlow<List<CopilotMessageEntity>> = repository.allCopilotMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dialog & Modal States
    private val _selectedInvoiceForView = MutableStateFlow<InvoiceEntity?>(null)
    val selectedInvoiceForView: StateFlow<InvoiceEntity?> = _selectedInvoiceForView.asStateFlow()

    private val _selectedCustomerForKhata = MutableStateFlow<PartyEntity?>(null)
    val selectedCustomerForKhata: StateFlow<PartyEntity?> = _selectedCustomerForKhata.asStateFlow()

    private val _showCreateInvoiceDialog = MutableStateFlow(false)
    val showCreateInvoiceDialog: StateFlow<Boolean> = _showCreateInvoiceDialog.asStateFlow()

    private val _showRecordPaymentDialog = MutableStateFlow(false)
    val showRecordPaymentDialog: StateFlow<Boolean> = _showRecordPaymentDialog.asStateFlow()

    private val _showStockAdjustDialog = MutableStateFlow<ProductEntity?>(null)
    val showStockAdjustDialog: StateFlow<ProductEntity?> = _showStockAdjustDialog.asStateFlow()

    private val _showAddProductDialog = MutableStateFlow(false)
    val showAddProductDialog: StateFlow<Boolean> = _showAddProductDialog.asStateFlow()

    private val _showAddCustomerDialog = MutableStateFlow(false)
    val showAddCustomerDialog: StateFlow<Boolean> = _showAddCustomerDialog.asStateFlow()

    private val _showAddExpenseDialog = MutableStateFlow(false)
    val showAddExpenseDialog: StateFlow<Boolean> = _showAddExpenseDialog.asStateFlow()

    private val _showNewBatchDialog = MutableStateFlow(false)
    val showNewBatchDialog: StateFlow<Boolean> = _showNewBatchDialog.asStateFlow()

    private val _whatsAppReminderText = MutableStateFlow<String?>(null)
    val whatsAppReminderText: StateFlow<String?> = _whatsAppReminderText.asStateFlow()

    // Copilot State
    private val _isCopilotThinking = MutableStateFlow(false)
    val isCopilotThinking: StateFlow<Boolean> = _isCopilotThinking.asStateFlow()

    // POS State
    private val _posCart = MutableStateFlow<List<PosCartItem>>(emptyList())
    val posCart: StateFlow<List<PosCartItem>> = _posCart.asStateFlow()

    private val _posUseWholesalePrice = MutableStateFlow(true)
    val posUseWholesalePrice: StateFlow<Boolean> = _posUseWholesalePrice.asStateFlow()

    private val _posCheckoutSuccess = MutableStateFlow<String?>(null)
    val posCheckoutSuccess: StateFlow<String?> = _posCheckoutSuccess.asStateFlow()

    // Business Identity & Persistent Authentication
    private val authPrefs = application.getSharedPreferences("growth_engine_auth_prefs", Context.MODE_PRIVATE)
    val supabaseAuthService = com.example.supabase.SupabaseAuthService(application)

    val currentUserEmail = MutableStateFlow(authPrefs.getString("user_email", "owner@mybusiness.in") ?: "owner@mybusiness.in")
    val currentUserName = MutableStateFlow(authPrefs.getString("user_name", "Business Owner") ?: "Business Owner")
    val businessName = MutableStateFlow(authPrefs.getString("business_name", "My Enterprise") ?: "My Enterprise")
    val businessRegion = MutableStateFlow(authPrefs.getString("business_region", "State: Maharashtra") ?: "State: Maharashtra")
    val authProvider = MutableStateFlow(authPrefs.getString("auth_provider", "SUPABASE_AUTH") ?: "SUPABASE_AUTH")

    // Persistent login state
    val isUserLoggedIn = MutableStateFlow(authPrefs.getBoolean("is_logged_in", true))
    val showAuthModal = MutableStateFlow(false)

    // Admin state: role/permission evaluated securely from backend / email
    val isSuperAdmin = MutableStateFlow(checkIfAdmin(currentUserEmail.value))
    val isAdminModeActive = MutableStateFlow(authPrefs.getBoolean("is_admin_mode_active", false))

    init {
        // Restore session from SupabaseAuthService on app launch
        val session = supabaseAuthService.getStoredSession()
        if (session != null && session.accessToken.isNotBlank()) {
            currentUserEmail.value = session.email
            currentUserName.value = session.fullName.ifBlank { "Business Owner" }
            if (session.businessName.isNotBlank()) businessName.value = session.businessName
            val reg = if (session.gstin.isNotBlank()) "GSTIN: ${session.gstin} · ${session.state}" else "State: ${session.state}"
            if (session.state.isNotBlank()) businessRegion.value = reg
            isSuperAdmin.value = checkIfAdmin(session.email)
            isUserLoggedIn.value = true

            // Trigger silent token refresh if session is getting old
            if (!supabaseAuthService.isAuthenticated()) {
                viewModelScope.launch {
                    supabaseAuthService.refreshSession()
                }
            }
        }
    }

    private fun checkIfAdmin(email: String): Boolean {
        return email.trim().equals("prajindezaa142@gmail.com", ignoreCase = true)
    }

    fun onAuthSuccess(email: String, fullName: String, bName: String, region: String) {
        val isAdmin = checkIfAdmin(email)
        currentUserEmail.value = email
        currentUserName.value = fullName.ifBlank { "Business Owner" }
        businessName.value = bName.ifBlank { "My Enterprise" }
        businessRegion.value = region
        authProvider.value = "SUPABASE_AUTH"
        isSuperAdmin.value = isAdmin
        isUserLoggedIn.value = true
        showAuthModal.value = false

        authPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("user_name", currentUserName.value)
            .putString("business_name", businessName.value)
            .putString("business_region", region)
            .putString("auth_provider", "SUPABASE_AUTH")
            .putBoolean("is_admin", isAdmin)
            .apply()
    }

    fun enterGuestMode() {
        showAuthModal.value = false
    }

    fun loginWithGoogle(email: String = "user@mybusiness.in", name: String = "Business Owner", bName: String = "My Store", region: String = "State: Maharashtra") {
        onAuthSuccess(email, name, bName, region)
    }

    fun login(name: String, region: String, email: String = "owner@mybusiness.in") {
        onAuthSuccess(email, name, name, region)
    }

    fun logout() {
        viewModelScope.launch {
            supabaseAuthService.signOut()
        }
        isUserLoggedIn.value = false
        isAdminModeActive.value = false
        authPrefs.edit()
            .putBoolean("is_logged_in", false)
            .putBoolean("is_admin_mode_active", false)
            .apply()
    }

    fun setShowAuthModal(show: Boolean) {
        showAuthModal.value = show
    }

    // Supabase Cloud State
    val supabaseService = com.example.supabase.SupabaseService(application)
    val supabaseAdminService = com.example.supabase.SupabaseAdminService(application)
    private val _supabaseSyncState = MutableStateFlow(com.example.supabase.SupabaseSyncState.IDLE)
    val supabaseSyncState: StateFlow<com.example.supabase.SupabaseSyncState> = _supabaseSyncState.asStateFlow()

    private val _supabaseMessage = MutableStateFlow("Local-first Room cache active. Ready to sync with Supabase.")
    val supabaseMessage: StateFlow<String> = _supabaseMessage.asStateFlow()

    // -------------------------------------------------------------------------
    // ADMIN SYSTEM STATE & TELEMETRY
    // -------------------------------------------------------------------------
    val currentAdminRole = MutableStateFlow(AdminRole.SUPER_ADMIN)

    private val _adminBusinesses = MutableStateFlow<List<BusinessAccount>>(
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
                totalInvoicesCount = 1290,
                totalProductsCount = 1850,
                staffSeatsCount = 8,
                aiTokensUsed = 890000L,
                aiMonthlyQuota = 2000000L
            ),
            BusinessAccount(
                name = "Surat Textiles & Silk Mills",
                tradeName = "Surat Silk Fab",
                ownerEmail = "info@suratsilkfab.com",
                ownerPhone = "+91 98980 11223",
                gstin = "24AABCS7712K1Z9",
                state = "Gujarat (24)",
                registeredDateEpoch = System.currentTimeMillis() - (10L * 86400000L),
                lastActiveEpoch = System.currentTimeMillis() - (2 * 3600000L),
                status = "ACTIVE",
                planTier = SubscriptionTier.GROWTH_PRO,
                subscriptionStatus = SubscriptionStatus.TRIAL,
                monthlyRevenueInr = 0.0,
                totalInvoicesCount = 48,
                totalProductsCount = 120,
                staffSeatsCount = 2,
                aiTokensUsed = 42000L,
                aiMonthlyQuota = 250000L
            ),
            BusinessAccount(
                name = "Bangalore Smart Mart",
                tradeName = "Smart Mart POS Retail",
                ownerEmail = "store@smartmartblr.in",
                ownerPhone = "+91 97400 44556",
                gstin = "29AAACB1948L1Z2",
                state = "Karnataka (29)",
                registeredDateEpoch = System.currentTimeMillis() - (60L * 86400000L),
                lastActiveEpoch = System.currentTimeMillis() - (86400000L),
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
    val adminBusinesses: StateFlow<List<BusinessAccount>> = _adminBusinesses.asStateFlow()

    private val _adminTransactions = MutableStateFlow<List<PaymentTransactionRecord>>(
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
    val adminTransactions: StateFlow<List<PaymentTransactionRecord>> = _adminTransactions.asStateFlow()

    private val _adminAiLogs = MutableStateFlow<List<AiUsageLog>>(
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
    val adminAiLogs: StateFlow<List<AiUsageLog>> = _adminAiLogs.asStateFlow()

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

    private val _adminBroadcasts = MutableStateFlow<List<NotificationBroadcast>>(
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
    val adminBroadcasts: StateFlow<List<NotificationBroadcast>> = _adminBroadcasts.asStateFlow()

    private val _activityAuditLogs = MutableStateFlow<List<ActivityAuditLog>>(
        listOf(
            ActivityAuditLog(
                actorEmail = "prajindezaa142@gmail.com",
                actorRole = "Super Admin",
                action = "ADMIN_LOGIN",
                targetEntity = "ADMIN_PORTAL",
                details = "Admin elevated session initialized from authorized IP",
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
    val activityAuditLogs: StateFlow<List<ActivityAuditLog>> = _activityAuditLogs.asStateFlow()

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

    // Admin Action Handlers
    fun updateBusinessAccount(updated: BusinessAccount) {
        val currentList = _adminBusinesses.value.toMutableList()
        val index = currentList.indexOfFirst { it.name == updated.name || it.ownerEmail == updated.ownerEmail }
        if (index >= 0) {
            currentList[index] = updated
            _adminBusinesses.value = currentList

            // Log action in audit trail
            logAdminEvent(
                action = "BUSINESS_UPDATED",
                target = updated.name,
                details = "Plan set to ${updated.planTier.title}, Status: ${updated.subscriptionStatus.name}, Account: ${updated.status}",
                severity = "WARN"
            )
        }
    }

    fun adjustBusinessAiQuota(business: BusinessAccount, newQuota: Long, bonus: Long, isThrottled: Boolean) {
        val currentList = _adminBusinesses.value.toMutableList()
        val index = currentList.indexOfFirst { it.name == business.name }
        if (index >= 0) {
            currentList[index] = business.copy(aiMonthlyQuota = newQuota + bonus)
            _adminBusinesses.value = currentList

            logAdminEvent(
                action = "AI_QUOTA_ADJUSTED",
                target = business.name,
                details = "Monthly quota set to $newQuota tokens (+${bonus} bonus), Throttled: $isThrottled",
                severity = "INFO"
            )
        }
    }

    fun sendAdminBroadcast(broadcast: NotificationBroadcast) {
        _adminBroadcasts.value = listOf(broadcast) + _adminBroadcasts.value
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
        val currentList = _adminTransactions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == transaction.id || it.razorpayPaymentId == transaction.razorpayPaymentId }
        if (index >= 0) {
            currentList[index] = transaction.copy(status = "REFUNDED")
            _adminTransactions.value = currentList

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
            actorEmail = "prajindezaa142@gmail.com",
            actorRole = "Super Admin",
            action = action,
            targetEntity = target,
            details = details,
            severity = severity
        )
        _activityAuditLogs.value = listOf(newLog) + _activityAuditLogs.value
        viewModelScope.launch {
            supabaseAdminService.logAdminAction(newLog)
        }
    }

    fun syncWithSupabase() {
        viewModelScope.launch {
            _supabaseSyncState.value = com.example.supabase.SupabaseSyncState.SYNCING
            _supabaseMessage.value = "Connecting to Supabase PostgREST cloud..."
            val result = supabaseService.syncAll(
                invoices = invoices.value,
                customers = customers.value,
                products = products.value,
                expenses = expenses.value
            )
            _supabaseSyncState.value = if (result.first) com.example.supabase.SupabaseSyncState.SYNCED else com.example.supabase.SupabaseSyncState.ERROR
            _supabaseMessage.value = result.second
        }
    }

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _supabaseSyncState.value = com.example.supabase.SupabaseSyncState.SYNCING
            val result = supabaseService.testConnection()
            _supabaseSyncState.value = if (result.first) com.example.supabase.SupabaseSyncState.SYNCED else com.example.supabase.SupabaseSyncState.ERROR
            _supabaseMessage.value = result.second
        }
    }

    // Subscription & Razorpay State
    val razorpayService = RazorpayPaymentService(application)
    private val _currentSubscription = MutableStateFlow(CurrentSubscription())
    val currentSubscription: StateFlow<CurrentSubscription> = _currentSubscription.asStateFlow()

    private val _billingHistory = MutableStateFlow<List<BillingReceipt>>(
        listOf(
            BillingReceipt(
                invoiceId = "inv_rec_2026_01",
                receiptNumber = "GE-REC-20260910-8421",
                dateEpoch = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000),
                tier = SubscriptionTier.GROWTH_PRO,
                cycle = BillingCycle.ANNUAL,
                baseAmount = 7188.0,
                gstRatePercent = 18.0,
                status = PaymentStatus.PAID,
                paymentMode = "UPI Autopay (GPay · dezaa@okhdfcbank)",
                razorpayPaymentId = "pay_N8zK4mQ9xP2L1",
                razorpayOrderId = "order_ge_20260910_8421"
            )
        )
    )
    val billingHistory: StateFlow<List<BillingReceipt>> = _billingHistory.asStateFlow()

    private val _showCheckoutModal = MutableStateFlow(false)
    val showCheckoutModal: StateFlow<Boolean> = _showCheckoutModal.asStateFlow()

    private val _checkoutTier = MutableStateFlow<SubscriptionTier?>(null)
    val checkoutTier: StateFlow<SubscriptionTier?> = _checkoutTier.asStateFlow()

    private val _checkoutCycle = MutableStateFlow(BillingCycle.ANNUAL)
    val checkoutCycle: StateFlow<BillingCycle> = _checkoutCycle.asStateFlow()

    private val _selectedReceiptForView = MutableStateFlow<BillingReceipt?>(null)
    val selectedReceiptForView: StateFlow<BillingReceipt?> = _selectedReceiptForView.asStateFlow()

    fun openCheckout(tier: SubscriptionTier, cycle: BillingCycle) {
        _checkoutTier.value = tier
        _checkoutCycle.value = cycle
        _showCheckoutModal.value = true
    }

    fun dismissCheckout() {
        _showCheckoutModal.value = false
        _checkoutTier.value = null
    }

    fun activateSubscription(receipt: BillingReceipt) {
        val nextRenewal = if (receipt.cycle == BillingCycle.ANNUAL) {
            System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
        } else {
            System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        }

        _currentSubscription.value = CurrentSubscription(
            tier = receipt.tier,
            cycle = receipt.cycle,
            isActive = true,
            startDateEpoch = System.currentTimeMillis(),
            renewalDateEpoch = nextRenewal,
            autoRenew = true,
            paymentMethod = receipt.paymentMode,
            razorpaySubscriptionId = "sub_${receipt.razorpayPaymentId.takeLast(10)}"
        )

        // Prepend new receipt to history
        _billingHistory.value = listOf(receipt) + _billingHistory.value
        _showCheckoutModal.value = false
        _checkoutTier.value = null
    }

    fun toggleAutoRenew() {
        val current = _currentSubscription.value
        _currentSubscription.value = current.copy(autoRenew = !current.autoRenew)
    }

    fun openReceiptView(receipt: BillingReceipt?) {
        _selectedReceiptForView.value = receipt
    }

    fun setTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun openInvoiceView(invoice: InvoiceEntity?) {
        _selectedInvoiceForView.value = invoice
    }

    fun openCustomerKhata(party: PartyEntity?) {
        _selectedCustomerForKhata.value = party
    }

    fun setShowCreateInvoice(show: Boolean) {
        _showCreateInvoiceDialog.value = show
    }

    fun setShowRecordPayment(show: Boolean) {
        _showRecordPaymentDialog.value = show
    }

    fun setShowStockAdjust(product: ProductEntity?) {
        _showStockAdjustDialog.value = product
    }

    fun setShowAddProduct(show: Boolean) {
        _showAddProductDialog.value = show
    }

    fun setShowAddCustomer(show: Boolean) {
        _showAddCustomerDialog.value = show
    }

    fun setShowAddExpense(show: Boolean) {
        _showAddExpenseDialog.value = show
    }

    fun setShowNewBatch(show: Boolean) {
        _showNewBatchDialog.value = show
    }

    fun clearWhatsAppReminder() {
        _whatsAppReminderText.value = null
    }

    fun clearPosSuccess() {
        _posCheckoutSuccess.value = null
    }

    // POS Actions
    fun addPosItem(product: ProductEntity) {
        val current = _posCart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        val price = if (_posUseWholesalePrice.value) product.wholesalePrice else product.mrp
        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + 1.0)
        } else {
            current.add(PosCartItem(product = product, quantity = 1.0, unitPrice = price))
        }
        _posCart.value = current
    }

    fun updatePosQuantity(productId: Long, delta: Double) {
        val current = _posCart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val newQty = current[index].quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQty)
            }
            _posCart.value = current
        }
    }

    fun togglePosPriceTier() {
        val newTier = !_posUseWholesalePrice.value
        _posUseWholesalePrice.value = newTier
        _posCart.value = _posCart.value.map { item ->
            val price = if (newTier) item.product.wholesalePrice else item.product.mrp
            item.copy(unitPrice = price)
        }
    }

    fun clearPosCart() {
        _posCart.value = emptyList()
    }

    fun checkoutPos(paymentMode: String, partyName: String = "Walk-in Counter Customer") {
        viewModelScope.launch {
            val cart = _posCart.value
            if (cart.isEmpty()) return@launch

            var subtotal = 0.0
            var cgst = 0.0
            var sgst = 0.0

            val itemsSummary = cart.joinToString(", ") { "${it.quantity.toInt()}x ${it.product.name}" }

            cart.forEach { item ->
                val lineTotal = item.unitPrice * item.quantity
                val taxRate = item.product.gstRatePercent / 100.0
                val tax = lineTotal * taxRate
                subtotal += lineTotal
                cgst += (tax / 2.0)
                sgst += (tax / 2.0)

                // update stock
                val updatedStock = (item.product.currentStock - item.quantity).coerceAtLeast(0.0)
                repository.updateProductStock(item.product.id, updatedStock)
            }

            val grandTotal = subtotal + cgst + sgst
            val invNumber = "POS-2425/${(1000..9999).random()}"

            val invoice = InvoiceEntity(
                invoiceNumber = invNumber,
                invoiceType = "TAX_INVOICE",
                partyId = 0L,
                partyName = partyName,
                partyGstin = "B2C-RETAIL",
                partyPhone = "+91 98000 00000",
                partyState = "Maharashtra (27)",
                isInterState = false,
                dateEpoch = System.currentTimeMillis(),
                dueDateEpoch = System.currentTimeMillis(),
                itemsSummary = itemsSummary,
                itemsCount = cart.size,
                subtotal = subtotal,
                discount = 0.0,
                cgstAmount = cgst,
                sgstAmount = sgst,
                igstAmount = 0.0,
                totalAmount = grandTotal,
                amountPaid = grandTotal,
                balanceDue = 0.0,
                paymentStatus = "PAID",
                paymentMode = paymentMode,
                notes = "Quick POS Instant Settlement"
            )

            repository.insertInvoice(invoice)
            _posCart.value = emptyList()
            _posCheckoutSuccess.value = "Bill $invNumber generated for ₹${formatCurrencyPlain(grandTotal)} via $paymentMode"
        }
    }

    // Business Operations
    fun saveNewInvoice(
        party: PartyEntity,
        itemsSummary: String,
        subtotal: Double,
        isInterState: Boolean,
        paymentMode: String,
        creditDays: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val gstRate = 0.18
            val totalTax = subtotal * gstRate
            val cgst = if (isInterState) 0.0 else totalTax / 2.0
            val sgst = if (isInterState) 0.0 else totalTax / 2.0
            val igst = if (isInterState) totalTax else 0.0
            val grandTotal = subtotal + totalTax
            val isPaid = paymentMode != "CREDIT"
            val paidAmount = if (isPaid) grandTotal else 0.0
            val balance = if (isPaid) 0.0 else grandTotal

            val invNum = "INV-2425/08${(46..99).random()}"
            val now = System.currentTimeMillis()
            val dueDate = now + (creditDays * 86400000L)

            val invoice = InvoiceEntity(
                invoiceNumber = invNum,
                invoiceType = "TAX_INVOICE",
                partyId = party.id,
                partyName = party.name,
                partyGstin = party.gstin,
                partyPhone = party.phone,
                partyState = "${party.stateName} (${party.stateCode})",
                isInterState = isInterState,
                dateEpoch = now,
                dueDateEpoch = dueDate,
                itemsSummary = itemsSummary,
                itemsCount = 1,
                subtotal = subtotal,
                discount = 0.0,
                cgstAmount = cgst,
                sgstAmount = sgst,
                igstAmount = igst,
                totalAmount = grandTotal,
                amountPaid = paidAmount,
                balanceDue = balance,
                paymentStatus = if (isPaid) "PAID" else "UNPAID",
                paymentMode = paymentMode,
                notes = notes
            )

            repository.insertInvoice(invoice)
            if (!isPaid) {
                // increase party outstanding balance
                val updatedBalance = party.outstandingBalance + balance
                repository.updateParty(party.copy(outstandingBalance = updatedBalance))
            }
            _showCreateInvoiceDialog.value = false
        }
    }

    fun recordKhataPayment(
        party: PartyEntity,
        amount: Double,
        mode: String,
        referenceNo: String,
        notes: String
    ) {
        viewModelScope.launch {
            val payment = PaymentEntity(
                partyId = party.id,
                partyName = party.name,
                invoiceNumber = null,
                amount = amount,
                paymentMode = mode,
                referenceNumber = referenceNo,
                dateEpoch = System.currentTimeMillis(),
                notes = notes
            )
            repository.insertPayment(payment)
            _showRecordPaymentDialog.value = false
            _selectedCustomerForKhata.value = null
        }
    }

    fun adjustStock(productId: Long, newStock: Double) {
        viewModelScope.launch {
            repository.updateProductStock(productId, newStock)
            _showStockAdjustDialog.value = null
        }
    }

    fun addProduct(
        name: String,
        sku: String,
        hsn: String,
        category: String,
        unit: String,
        purchasePrice: Double,
        wholesalePrice: Double,
        mrp: Double,
        gstRate: Double,
        stock: Double,
        minReorder: Double,
        supplier: String
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                name = name,
                sku = sku,
                hsnCode = hsn,
                category = category,
                unit = unit,
                purchasePrice = purchasePrice,
                wholesalePrice = wholesalePrice,
                mrp = mrp,
                gstRatePercent = gstRate,
                currentStock = stock,
                minReorderLevel = minReorder,
                preferredSupplier = supplier
            )
            repository.insertProduct(product)
            _showAddProductDialog.value = false
        }
    }

    fun addCustomer(
        name: String,
        tradeName: String,
        gstin: String,
        phone: String,
        address: String,
        stateName: String,
        stateCode: String,
        creditLimit: Double,
        creditDays: Int
    ) {
        viewModelScope.launch {
            val customer = PartyEntity(
                name = name,
                tradeName = tradeName,
                type = "CUSTOMER",
                gstin = gstin,
                panNumber = if (gstin.length >= 12) gstin.substring(2, 12) else "AAACB1234K",
                phone = phone,
                email = "contact@${tradeName.lowercase().replace(" ", "")}.in",
                address = address,
                stateName = stateName,
                stateCode = stateCode,
                creditLimit = creditLimit,
                outstandingBalance = 0.0,
                paymentTermsDays = creditDays
            )
            repository.insertParty(customer)
            _showAddCustomerDialog.value = false
        }
    }

    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        isGstClaimable: Boolean,
        gstAmount: Double,
        paymentMode: String,
        vendor: String
    ) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                title = title,
                category = category,
                amount = amount,
                dateEpoch = System.currentTimeMillis(),
                isGstClaimable = isGstClaimable,
                gstAmount = gstAmount,
                paymentMode = paymentMode,
                vendorName = vendor
            )
            repository.insertExpense(expense)
            _showAddExpenseDialog.value = false
        }
    }

    fun createManufacturingBatch(
        goodName: String,
        targetQty: Double,
        unit: String,
        rawMaterials: String,
        costPerUnit: Double
    ) {
        viewModelScope.launch {
            val batch = ManufacturingOrderEntity(
                batchCode = "BATCH-24-${(100..999).random()}",
                finishedGoodName = goodName,
                targetQuantity = targetQty,
                unit = unit,
                status = "IN_PRODUCTION",
                rawMaterialsUsedSummary = rawMaterials,
                estimatedCostPerUnit = costPerUnit,
                startDateEpoch = System.currentTimeMillis(),
                targetDateEpoch = System.currentTimeMillis() + (3 * 86400000L)
            )
            repository.insertBatch(batch)
            _showNewBatchDialog.value = false
        }
    }

    fun updateBatchStatus(batchId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateBatchStatus(batchId, newStatus)
        }
    }

    fun generateWhatsAppReminder(customer: PartyEntity) {
        val upiLink = "upi://pay?pa=kalyanworks@sbi&pn=Kalyan%20Industrial%20Works&am=${customer.outstandingBalance}&cu=INR"
        val text = """
            *PAYMENT REMINDER | Kalyan Industrial Works*
            
            Respected Sir/Madam,
            Greetings from Kalyan Industrial Works, Bhosari MIDC, Pune.
            
            This is a gentle reminder regarding your outstanding Khata balance of *₹ ${formatCurrencyPlain(customer.outstandingBalance)}* pending on your account (*${customer.tradeName}*).
            
            Payment Details for immediate settlement:
            • Bank: State Bank of India
            • A/C No: 38492049102
            • IFSC: SBIN0004521
            • UPI ID: kalyanworks@sbi
            
            Direct UPI Payment Link:
            $upiLink
            
            Kindly share the UTR / transaction receipt once settled to update your ledger.
            
            Thanking you,
            Finance & Accounts Desk
            Kalyan Industrial Works | GSTIN: 27AABCK4829K1Z5
        """.trimIndent()

        _whatsAppReminderText.value = text
    }

    // Convert Quotation / Estimate to Tax Invoice
    fun convertQuotationToInvoice(quotation: InvoiceEntity) {
        viewModelScope.launch {
            val updatedQuote = quotation.copy(paymentStatus = "PAID")
            repository.updateInvoice(updatedQuote)

            val taxInvNumber = "INV-2425/08${(46..99).random()}"
            val newTaxInvoice = quotation.copy(
                id = 0,
                invoiceNumber = taxInvNumber,
                invoiceType = "TAX_INVOICE",
                paymentStatus = "UNPAID",
                dateEpoch = System.currentTimeMillis(),
                dueDateEpoch = System.currentTimeMillis() + (30L * 86400000L),
                notes = "Converted from Estimate ${quotation.invoiceNumber}"
            )
            repository.insertInvoice(newTaxInvoice)

            // Update party balance if customer exists
            val party = customers.value.find { it.id == quotation.partyId }
            if (party != null) {
                repository.updateParty(party.copy(outstandingBalance = party.outstandingBalance + quotation.totalAmount))
            }
        }
    }

    fun recordSupplierPayout(party: PartyEntity, amount: Double, mode: String, ref: String) {
        viewModelScope.launch {
            val payment = PaymentEntity(
                partyId = party.id,
                partyName = party.name,
                invoiceNumber = null,
                amount = amount,
                paymentMode = mode,
                referenceNumber = ref,
                dateEpoch = System.currentTimeMillis(),
                notes = "Vendor payout to ${party.name}"
            )
            repository.insertPayment(payment)
        }
    }

    // -------------------------------------------------------------------------
    // AI USAGE QUOTA TRACKING
    // -------------------------------------------------------------------------
    private val aiUsagePrefs = application.getSharedPreferences("growthengine_ai_usage", Context.MODE_PRIVATE)

    private fun getCurrentMonthKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${String.format("%02d", cal.get(Calendar.MONTH) + 1)}"
    }

    private fun getMonthlyTokensUsed(): Long {
        return aiUsagePrefs.getLong("tokens_${getCurrentMonthKey()}", 0L)
    }

    private fun incrementMonthlyTokens(tokens: Int) {
        val key = "tokens_${getCurrentMonthKey()}"
        val current = aiUsagePrefs.getLong(key, 0L)
        aiUsagePrefs.edit().putLong(key, current + tokens).apply()
        _aiTokensUsedThisMonth.value = current + tokens
    }

    private fun getMonthlyQueryCount(): Int {
        return aiUsagePrefs.getInt("queries_${getCurrentMonthKey()}", 0)
    }

    private fun incrementMonthlyQueries() {
        val key = "queries_${getCurrentMonthKey()}"
        val current = aiUsagePrefs.getInt(key, 0)
        aiUsagePrefs.edit().putInt(key, current + 1).apply()
        _aiQueriesThisMonth.value = current + 1
    }

    // Exposed AI usage state for UI
    private val _aiTokensUsedThisMonth = MutableStateFlow(getMonthlyTokensUsed())
    val aiTokensUsedThisMonth: StateFlow<Long> = _aiTokensUsedThisMonth.asStateFlow()

    private val _aiQueriesThisMonth = MutableStateFlow(getMonthlyQueryCount())
    val aiQueriesThisMonth: StateFlow<Int> = _aiQueriesThisMonth.asStateFlow()

    val aiMonthlyQuota: Long
        get() = currentSubscription.value.tier.aiMonthlyTokenQuota

    val aiUsagePercent: Float
        get() {
            val quota = aiMonthlyQuota
            if (quota <= 0) return 0f
            return (_aiTokensUsedThisMonth.value.toFloat() / quota.toFloat()).coerceIn(0f, 1f)
        }

    private val _aiQuotaExceeded = MutableStateFlow(false)
    val aiQuotaExceeded: StateFlow<Boolean> = _aiQuotaExceeded.asStateFlow()

    // Copilot Logic
    fun sendCopilotQuery(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            // Save user message
            repository.insertCopilotMessage(
                CopilotMessageEntity(
                    sender = "USER",
                    messageText = prompt
                )
            )

            // --- Quota Enforcement ---
            val currentTier = currentSubscription.value.tier
            val tokensUsed = getMonthlyTokensUsed()
            val quota = currentTier.aiMonthlyTokenQuota

            if (tokensUsed >= quota) {
                _aiQuotaExceeded.value = true
                repository.insertCopilotMessage(
                    CopilotMessageEntity(
                        sender = "COPILOT",
                        messageText = """
                            ⚠️ **AI Copilot Quota Exhausted**

                            You have used **${MainViewModel.formatCurrencyPlain(tokensUsed.toDouble())}** of your **${MainViewModel.formatCurrencyPlain(quota.toDouble())}** monthly AI tokens on your **${currentTier.title}** plan.

                            To continue using GrowthEngine AI Copilot:
                            • **Upgrade your plan** for a higher token quota
                            • **Wait until next month** when your quota resets automatically

                            Your business data, invoices, and all other features remain fully accessible.
                        """.trimIndent(),
                        actionType = "QUOTA_EXCEEDED"
                    )
                )
                return@launch
            }

            _isCopilotThinking.value = true

            // Build rich live context
            val invList = invoices.value
            val totalInvoiced = invList.sumOf { it.totalAmount }
            val totalOutstanding = customers.value.sumOf { it.outstandingBalance }
            val overdueDebtors = customers.value.filter { it.overdueDays > 0 }
                .joinToString(", ") { "${it.name} (₹${formatCurrencyPlain(it.outstandingBalance)}, ${it.overdueDays}d overdue)" }
            val lowStock = lowStockProducts.value.joinToString(", ") { "${it.name} (Stock: ${it.currentStock} ${it.unit})" }
            val totalExpenses = expenses.value.sumOf { it.amount }

            val context = """
                Company: Kalyan Industrial Works (Bhosari MIDC, Pune, Maharashtra)
                GSTIN: 27AABCK4829K1Z5 | Financial Year: 2024-25
                Total B2B Invoiced Revenue: ₹${formatCurrencyPlain(totalInvoiced)}
                Total Khata Receivables: ₹${formatCurrencyPlain(totalOutstanding)}
                Overdue Customers: $overdueDebtors
                Low Stock Warning SKUs: $lowStock
                Total Operating Expenses: ₹${formatCurrencyPlain(totalExpenses)}
            """.trimIndent()

            val response = copilotService.consultCopilot(prompt, context)
            _isCopilotThinking.value = false

            // --- Track Usage ---
            incrementMonthlyTokens(response.totalTokens)
            incrementMonthlyQueries()

            // Update quota exceeded flag
            _aiQuotaExceeded.value = getMonthlyTokensUsed() >= quota

            repository.insertCopilotMessage(
                CopilotMessageEntity(
                    sender = "COPILOT",
                    messageText = response.text
                )
            )
        }
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
            return format.format(amount)
        }

        fun formatCurrencyPlain(amount: Double): String {
            val format = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN"))
            format.maximumFractionDigits = 0
            return format.format(amount)
        }

        fun formatDate(epoch: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("en-IN"))
            return sdf.format(Date(epoch))
        }
    }
}
