package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

enum class AdminSubTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    BUSINESSES("Businesses", Icons.Default.Business),
    SUBSCRIPTIONS("Subscriptions", Icons.Default.CardMembership),
    PAYMENTS("Payments & Razorpay", Icons.Default.Payments),
    AI_METERING("AI Usage & Quotas", Icons.Default.AutoAwesome),
    FEATURE_USAGE("ERP Telemetry", Icons.Default.Analytics),
    NOTIFICATIONS("Broadcasts", Icons.Default.Campaign),
    PLANS("Plans & Pricing", Icons.Default.Tune),
    SUPPORT_AUDIT("Support & Logs", Icons.Default.Security),
    SETTINGS("System & Supabase", Icons.Default.Storage)
}

@Composable
fun AdminSystemScreen(
    adminRole: AdminRole = AdminRole.SUPER_ADMIN,
    businesses: List<BusinessAccount>,
    transactions: List<PaymentTransactionRecord>,
    aiLogs: List<AiUsageLog>,
    globalAiMetrics: GlobalAiMetrics,
    broadcasts: List<NotificationBroadcast>,
    auditLogs: List<ActivityAuditLog>,
    supportTickets: List<SupportTicket>,
    systemConfig: SystemConfiguration,
    currentSubTab: AdminSubTab = AdminSubTab.OVERVIEW,
    onSelectSubTab: (AdminSubTab) -> Unit = {},
    onUpdateBusiness: (BusinessAccount) -> Unit,
    onAdjustAiQuota: (BusinessAccount, Long, Long, Boolean) -> Unit,
    onSendBroadcast: (NotificationBroadcast) -> Unit,
    onRefundTransaction: (PaymentTransactionRecord) -> Unit,
    onUpdateTicketStatus: (String, String) -> Unit,
    onToggleMaintenance: () -> Unit,
    onUpdateSystemAnnouncement: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedSubTab by remember(currentSubTab) { mutableStateOf(currentSubTab) }

    // Dialog state
    var editingBusiness by remember { mutableStateOf<BusinessAccount?>(null) }
    var adjustingAiBusiness by remember { mutableStateOf<BusinessAccount?>(null) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<PaymentTransactionRecord?>(null) }
    var selectedTicket by remember { mutableStateOf<SupportTicket?>(null) }
    var showSqlDialog by remember { mutableStateOf(false) }

    // Filters
    var businessSearchQuery by remember { mutableStateOf("") }
    var businessStatusFilter by remember { mutableStateOf("ALL") }
    var subscriptionStatusFilter by remember { mutableStateOf("ALL") }
    var paymentStatusFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .testTag("admin_system_screen")
    ) {
        // Platform Maintenance Alert Strip (if active)
        if (systemConfig.maintenanceMode) {
            Surface(
                color = TerracottaRed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(
                        text = "MAINTENANCE MODE ACTIVE ACROSS PLATFORM",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // SubTab Navigation Ribbon
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab.ordinal,
            containerColor = SurfaceSubtle,
            contentColor = ImperialNavy,
            edgePadding = 12.dp,
            divider = { HorizontalDivider(color = BorderSubtle) },
            modifier = Modifier.fillMaxWidth()
        ) {
            AdminSubTab.values().forEach { tab ->
                val isSelected = selectedSubTab == tab
                Tab(
                    selected = isSelected,
                    onClick = {
                        selectedSubTab = tab
                        onSelectSubTab(tab)
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) ImperialNavy else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) ImperialNavy else TextSecondary
                            )
                        }
                    }
                )
            }
        }

        // Main Tab Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedSubTab) {
                AdminSubTab.OVERVIEW -> {
                    AdminOverviewSection(
                        businesses = businesses,
                        transactions = transactions,
                        globalAiMetrics = globalAiMetrics,
                        broadcasts = broadcasts,
                        supportTickets = supportTickets,
                        systemConfig = systemConfig,
                        onOpenBroadcast = { showBroadcastDialog = true },
                        onOpenSql = { showSqlDialog = true },
                        onNavigateTab = {
                            selectedSubTab = it
                            onSelectSubTab(it)
                        }
                    )
                }
                AdminSubTab.BUSINESSES -> {
                    AdminBusinessesSection(
                        businesses = businesses,
                        searchQuery = businessSearchQuery,
                        statusFilter = businessStatusFilter,
                        onSearchChange = { businessSearchQuery = it },
                        onStatusFilterChange = { businessStatusFilter = it },
                        onEditPlan = { editingBusiness = it },
                        onAdjustAi = { adjustingAiBusiness = it }
                    )
                }
                AdminSubTab.SUBSCRIPTIONS -> {
                    AdminSubscriptionsSection(
                        businesses = businesses,
                        statusFilter = subscriptionStatusFilter,
                        onStatusFilterChange = { subscriptionStatusFilter = it },
                        onEditSubscription = { editingBusiness = it }
                    )
                }
                AdminSubTab.PAYMENTS -> {
                    AdminPaymentsSection(
                        transactions = transactions,
                        statusFilter = paymentStatusFilter,
                        onStatusFilterChange = { paymentStatusFilter = it },
                        onViewTransaction = { selectedTransaction = it }
                    )
                }
                AdminSubTab.AI_METERING -> {
                    AdminAiMeteringSection(
                        globalAiMetrics = globalAiMetrics,
                        aiLogs = aiLogs,
                        businesses = businesses,
                        onAdjustQuota = { adjustingAiBusiness = it }
                    )
                }
                AdminSubTab.FEATURE_USAGE -> {
                    AdminFeatureUsageSection(
                        businesses = businesses
                    )
                }
                AdminSubTab.NOTIFICATIONS -> {
                    AdminNotificationsSection(
                        broadcasts = broadcasts,
                        onNewBroadcast = { showBroadcastDialog = true }
                    )
                }
                AdminSubTab.PLANS -> {
                    AdminPlansSection()
                }
                AdminSubTab.SUPPORT_AUDIT -> {
                    AdminSupportAndAuditSection(
                        supportTickets = supportTickets,
                        auditLogs = auditLogs,
                        onSelectTicket = { selectedTicket = it }
                    )
                }
                AdminSubTab.SETTINGS -> {
                    AdminSettingsSection(
                        systemConfig = systemConfig,
                        onToggleMaintenance = onToggleMaintenance,
                        onOpenSql = { showSqlDialog = true },
                        onUpdateAnnouncement = onUpdateSystemAnnouncement
                    )
                }
            }
        }
    }

    // Interactive Modals
    editingBusiness?.let { biz ->
        EditBusinessPlanDialog(
            business = biz,
            onDismiss = { editingBusiness = null },
            onSave = { updated ->
                onUpdateBusiness(updated)
                editingBusiness = null
                Toast.makeText(context, "Updated ${updated.name} Plan", Toast.LENGTH_SHORT).show()
            }
        )
    }

    adjustingAiBusiness?.let { biz ->
        AdjustAiQuotaDialog(
            business = biz,
            onDismiss = { adjustingAiBusiness = null },
            onSave = { quota, bonus, throttled ->
                onAdjustAiQuota(biz, quota, bonus, throttled)
                adjustingAiBusiness = null
                Toast.makeText(context, "AI Quota adjusted for ${biz.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showBroadcastDialog) {
        BroadcastNotificationDialog(
            onDismiss = { showBroadcastDialog = false },
            onSend = { broadcast ->
                onSendBroadcast(broadcast)
                showBroadcastDialog = false
                Toast.makeText(context, "Broadcast dispatched to ${broadcast.targetAudience}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    selectedTransaction?.let { tx ->
        TransactionDetailDialog(
            transaction = tx,
            onDismiss = { selectedTransaction = null },
            onRefund = { record ->
                onRefundTransaction(record)
                selectedTransaction = null
                Toast.makeText(context, "Refund initiated for ${record.razorpayPaymentId}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    selectedTicket?.let { ticket ->
        SupportTicketDialog(
            ticket = ticket,
            onDismiss = { selectedTicket = null },
            onUpdateStatus = { newStatus ->
                onUpdateTicketStatus(ticket.id, newStatus)
                selectedTicket = null
            }
        )
    }

    if (showSqlDialog) {
        SupabaseSqlViewerDialog(
            onDismiss = { showSqlDialog = false }
        )
    }
}

// -----------------------------------------------------------------------------
// SECTION 1: OVERVIEW & EXECUTIVE ANALYTICS
// -----------------------------------------------------------------------------
@Composable
private fun AdminOverviewSection(
    businesses: List<BusinessAccount>,
    transactions: List<PaymentTransactionRecord>,
    globalAiMetrics: GlobalAiMetrics,
    broadcasts: List<NotificationBroadcast>,
    supportTickets: List<SupportTicket>,
    systemConfig: SystemConfiguration,
    onOpenBroadcast: () -> Unit,
    onOpenSql: () -> Unit,
    onNavigateTab: (AdminSubTab) -> Unit
) {
    val totalBusinesses = businesses.size
    val activeSubs = businesses.count { it.subscriptionStatus == SubscriptionStatus.ACTIVE }
    val totalRevenue = transactions.filter { it.status == "CAPTURED" }.sumOf { it.totalWithGstInr }
    val mrr = businesses.filter { it.subscriptionStatus == SubscriptionStatus.ACTIVE }.sumOf { it.planTier.annualPricePerMonth }
    val openTickets = supportTickets.count { it.status == "OPEN" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Metric Grid
        item {
            Text("Executive Key Performance Indicators", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(
                    title = "Total Businesses",
                    value = "$totalBusinesses",
                    sub = "${businesses.count { it.status == "ACTIVE" }} active in India",
                    icon = Icons.Default.Business,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Active Subscriptions",
                    value = "$activeSubs",
                    sub = "MRR: ₹${MainViewModel.formatCurrencyPlain(mrr)}",
                    icon = Icons.Default.CardMembership,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(
                    title = "Total Revenue (Gross)",
                    value = "₹${MainViewModel.formatCurrencyPlain(totalRevenue)}",
                    sub = "Razorpay settlements",
                    icon = Icons.Default.CurrencyRupee,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "AI Tokens Used",
                    value = "${globalAiMetrics.totalTokensConsumedThisMonth / 1_000_000.0}M",
                    sub = "Cost: ₹${globalAiMetrics.totalCostEstimatedInr.toInt()}",
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Admin Controls Strip
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GrowthEngineGoldContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Admin Action Center", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenBroadcast,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Broadcast", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = onOpenSql,
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Supabase SQL", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // System Health & Alerts
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("System Health & Infrastructure", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("ALL SYSTEMS NORMAL", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SystemHealthRow("PostgreSQL Multi-Tenant DB", "Healthy • 12ms latency", SuccessGreen)
                    SystemHealthRow("Supabase Realtime Engine", "Active • Connected", SuccessGreen)
                    SystemHealthRow("Razorpay Subscriptions Webhook", "Active • 100% Delivery", SuccessGreen)
                    SystemHealthRow("Gemini 2.5 Flash LLM API", "Active • 460ms avg", SuccessGreen)
                }
            }
        }

        // Recent Signups & Top Businesses
        item {
            Text("Top Active Businesses", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(businesses.take(4)) { biz ->
            BusinessSummaryCard(
                business = biz,
                onEditPlan = { onNavigateTab(AdminSubTab.BUSINESSES) },
                onAdjustAi = { onNavigateTab(AdminSubTab.AI_METERING) }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 2: BUSINESSES & USERS HUB
// -----------------------------------------------------------------------------
@Composable
private fun AdminBusinessesSection(
    businesses: List<BusinessAccount>,
    searchQuery: String,
    statusFilter: String,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onEditPlan: (BusinessAccount) -> Unit,
    onAdjustAi: (BusinessAccount) -> Unit
) {
    val filtered = businesses.filter {
        (statusFilter == "ALL" || it.status == statusFilter || it.subscriptionStatus.name == statusFilter) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.ownerEmail.contains(searchQuery, ignoreCase = true) || it.gstin.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by Business Name, Email or GSTIN...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val filters = listOf("ALL" to "All (${businesses.size})", "ACTIVE" to "Active", "TRIAL" to "Trial", "SUSPENDED" to "Suspended")
            items(filters) { (key, label) ->
                FilterChip(
                    selected = statusFilter == key,
                    onClick = { onStatusFilterChange(key) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered) { biz ->
                BusinessSummaryCard(
                    business = biz,
                    onEditPlan = { onEditPlan(biz) },
                    onAdjustAi = { onAdjustAi(biz) }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 3: SUBSCRIPTIONS CENTRAL
// -----------------------------------------------------------------------------
@Composable
private fun AdminSubscriptionsSection(
    businesses: List<BusinessAccount>,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onEditSubscription: (BusinessAccount) -> Unit
) {
    val filtered = businesses.filter {
        statusFilter == "ALL" || it.subscriptionStatus.name == statusFilter
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Subscription Plans & Lifecycle Management", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val statuses = listOf("ALL" to "All") + SubscriptionStatus.values().map { it.name to it.label }
            items(statuses) { (key, label) ->
                FilterChip(
                    selected = statusFilter == key,
                    onClick = { onStatusFilterChange(key) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { biz ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(biz.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(biz.ownerEmail, fontSize = 11.sp, color = TextSecondary)
                            }
                            Surface(
                                color = Color(biz.subscriptionStatus.badgeColorHex).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = biz.subscriptionStatus.label.uppercase(),
                                    color = Color(biz.subscriptionStatus.badgeColorHex),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Plan: ${biz.planTier.title}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GrowthEngineGoldDark)
                            Text("Renewal: ${MainViewModel.formatDate(biz.renewalDateEpoch)}", fontSize = 11.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Razorpay Sub ID: ${biz.razorpaySubscriptionId}", fontSize = 10.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onEditSubscription(biz) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Manage Subscription", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 4: PAYMENTS & RAZORPAY BILLING
// -----------------------------------------------------------------------------
@Composable
private fun AdminPaymentsSection(
    transactions: List<PaymentTransactionRecord>,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onViewTransaction: (PaymentTransactionRecord) -> Unit
) {
    val filtered = transactions.filter {
        statusFilter == "ALL" || it.status == statusFilter
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Razorpay Transactions & Invoices Ledger", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val paymentFilters = listOf("ALL" to "All", "CAPTURED" to "Captured", "FAILED" to "Failed", "REFUNDED" to "Refunded", "PENDING" to "Pending")
            items(paymentFilters) { (key, label) ->
                FilterChip(
                    selected = statusFilter == key,
                    onClick = { onStatusFilterChange(key) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { tx ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth().clickable { onViewTransaction(tx) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.businessName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${tx.tier.title} • ${tx.paymentMode}", fontSize = 11.sp, color = TextSecondary)
                            Text("ID: ${tx.razorpayPaymentId}", fontSize = 10.sp, color = GrowthEngineGoldDark)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${MainViewModel.formatCurrencyPlain(tx.totalWithGstInr)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkInk)
                            Surface(
                                color = when (tx.status) {
                                    "CAPTURED" -> SuccessGreen.copy(alpha = 0.15f)
                                    "FAILED" -> ErrorRed.copy(alpha = 0.15f)
                                    else -> WarningAmber.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tx.status,
                                    color = when (tx.status) {
                                        "CAPTURED" -> SuccessGreen
                                        "FAILED" -> ErrorRed
                                        else -> WarningAmber
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 5: AI USAGE METERING & QUOTAS
// -----------------------------------------------------------------------------
@Composable
private fun AdminAiMeteringSection(
    globalAiMetrics: GlobalAiMetrics,
    aiLogs: List<AiUsageLog>,
    businesses: List<BusinessAccount>,
    onAdjustQuota: (BusinessAccount) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = GrowthEngineGoldContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Global AI Token Consumption", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${globalAiMetrics.totalTokensConsumedThisMonth / 1_000_000.0}M / ${globalAiMetrics.totalMonthlyLimit / 1_000_000}M Tokens",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthEngineGoldDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (globalAiMetrics.totalTokensConsumedThisMonth.toFloat() / globalAiMetrics.totalMonthlyLimit.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = GrowthEngineGoldDark,
                        trackColor = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Requests: ${globalAiMetrics.totalAiRequests}", fontSize = 11.sp, color = DarkInk)
                        Text("Est. Cost: ₹${globalAiMetrics.totalCostEstimatedInr.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                    }
                }
            }
        }

        item {
            Text("Recent AI Request Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(aiLogs) { log ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(log.businessName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${log.featureName} • ${log.modelName}", fontSize = 10.sp, color = TextSecondary)
                        Text("${log.promptTokens} in / ${log.completionTokens} out (${log.latencyMs}ms)", fontSize = 10.sp, color = GrowthEngineGoldDark)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${log.totalTokens} tokens", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("₹${String.format("%.4f", log.estimatedCostInr)}", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 6: ERP & FEATURE TELEMETRY
// -----------------------------------------------------------------------------
@Composable
private fun AdminFeatureUsageSection(
    businesses: List<BusinessAccount>
) {
    val totalInvoices = businesses.sumOf { it.totalInvoicesCount }
    val totalProducts = businesses.sumOf { it.totalProductsCount }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Platform-Wide ERP & Business Activity", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(
                    title = "Total Invoices Billed",
                    value = "$totalInvoices",
                    sub = "GST B2B & Counter POS",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Total Product SKUs",
                    value = "$totalProducts",
                    sub = "Active inventory across shops",
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Module Usage Adoption Rate", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    ModuleUsageRow("GST Invoicing & Billing", 98)
                    ModuleUsageRow("Counter POS with UPI", 84)
                    ModuleUsageRow("Khata & WhatsApp Reminders", 92)
                    ModuleUsageRow("AI Copilot Munim", 76)
                    ModuleUsageRow("Manufacturing BOM", 38)
                    ModuleUsageRow("Reports & Tax Filing Exports", 88)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 7: BROADCAST & NOTIFICATIONS
// -----------------------------------------------------------------------------
@Composable
private fun AdminNotificationsSection(
    broadcasts: List<NotificationBroadcast>,
    onNewBroadcast: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Broadcast & Notification Engine", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = onNewBroadcast,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Broadcast", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(broadcasts) { b ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(b.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(color = GrowthEngineGoldContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(b.targetAudience, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkInk, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(b.message, fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sent: ${MainViewModel.formatDate(b.sentAtEpoch)}", fontSize = 10.sp, color = TextSecondary)
                            Text("Delivered: ${b.targetCount} • Opened: ${b.openCount}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = GrowthEngineGoldDark)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 8: PLANS & PRICING
// -----------------------------------------------------------------------------
@Composable
private fun AdminPlansSection() {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Subscription Plans Matrix", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        items(SubscriptionTier.values()) { tier ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (tier.isPopular) GrowthEngineGoldBorder else BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(tier.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkInk)
                            Text(tier.tagline, fontSize = 11.sp, color = TextSecondary)
                        }
                        Text(
                            if (tier.monthlyPrice == 0.0) "Free" else "₹${tier.monthlyPrice.toInt()}/mo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GrowthEngineGoldDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    tier.getFeaturesList().take(4).forEach { feat ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(feat, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 9: SUPPORT TICKETS & AUDIT LOGS
// -----------------------------------------------------------------------------
@Composable
private fun AdminSupportAndAuditSection(
    supportTickets: List<SupportTicket>,
    auditLogs: List<ActivityAuditLog>,
    onSelectTicket: (SupportTicket) -> Unit
) {
    var selectedSubSection by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(
            selectedTabIndex = selectedSubSection,
            containerColor = SurfaceSubtle,
            contentColor = GrowthEngineGoldDark
        ) {
            Tab(
                selected = selectedSubSection == 0,
                onClick = { selectedSubSection = 0 },
                text = { Text("Support Desk (${supportTickets.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubSection == 1,
                onClick = { selectedSubSection = 1 },
                text = { Text("Audit Trail (${auditLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedSubSection == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(supportTickets) { ticket ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        modifier = Modifier.fillMaxWidth().clickable { onSelectTicket(ticket) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${ticket.id} • ${ticket.subject}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Surface(
                                    color = when (ticket.status) {
                                        "OPEN" -> ErrorRed.copy(alpha = 0.15f)
                                        "RESOLVED" -> SuccessGreen.copy(alpha = 0.15f)
                                        else -> WarningAmber.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(ticket.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${ticket.businessName} (${ticket.userEmail})", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(auditLogs) { log ->
                    Card(
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${log.action} • ${log.actorEmail}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(log.details, fontSize = 10.sp, color = TextSecondary)
                            }
                            Text(log.ipAddress, fontSize = 9.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 10: ADMIN CONTROLS & SYSTEM SETTINGS
// -----------------------------------------------------------------------------
@Composable
private fun AdminSettingsSection(
    systemConfig: SystemConfiguration,
    onToggleMaintenance: () -> Unit,
    onOpenSql: () -> Unit,
    onUpdateAnnouncement: (String) -> Unit
) {
    var announcementInput by remember { mutableStateOf(systemConfig.systemAnnouncementBanner ?: "") }
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Global Platform Controls & Infrastructure", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Suspends all user logins & shows maintenance splash", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = systemConfig.maintenanceMode,
                            onCheckedChange = { onToggleMaintenance() }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Allow New Business Signups", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Registration & onboarding funnel", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(checked = systemConfig.allowNewSignups, onCheckedChange = {})
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Global System Announcement Banner", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = announcementInput,
                        onValueChange = { announcementInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter announcement...") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onUpdateAnnouncement(announcementInput)
                            Toast.makeText(context, "Announcement published", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark)
                    ) {
                        Text("Publish Announcement", color = Color.White)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkInk),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Supabase Backend Schema & SQL Editor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Complete PostgreSQL schema with UUIDs, RLS policies, multi-tenant isolation, triggers and indexes ready for deployment.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenSql,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = DarkInk, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Supabase Architecture Center", color = DarkInk, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPOSABLES
// -----------------------------------------------------------------------------
@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = GrowthEngineGoldDark, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkInk)
            Text(sub, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun BusinessSummaryCard(
    business: BusinessAccount,
    onEditPlan: () -> Unit,
    onAdjustAi: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(business.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkInk)
                    Text("${business.tradeName} • ${business.state}", fontSize = 11.sp, color = TextSecondary)
                }
                Surface(
                    color = Color(business.subscriptionStatus.badgeColorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        business.subscriptionStatus.label,
                        color = Color(business.subscriptionStatus.badgeColorHex),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Plan: ${business.planTier.title}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GrowthEngineGoldDark)
                Text("Invoices: ${business.totalInvoicesCount} | SKUs: ${business.totalProductsCount}", fontSize = 11.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // AI Quota usage bar
            val ratio = (business.aiTokensUsed.toFloat() / business.aiMonthlyQuota.toFloat()).coerceIn(0f, 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AI Quota: ${business.aiTokensUsed / 1000}k / ${business.aiMonthlyQuota / 1000}k tokens", fontSize = 10.sp, color = TextSecondary)
                Text("${(ratio * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (ratio > 0.85f) ErrorRed else GrowthEngineGoldDark)
            }
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (ratio > 0.85f) ErrorRed else GrowthEngineGoldDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAdjustAi,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("AI Quota", fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onEditPlan,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Manage Plan", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SystemHealthRow(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = TextPrimary)
        Text(status, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun ModuleUsageRow(module: String, percentage: Int) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(module, fontSize = 11.sp, color = TextPrimary)
            Text("$percentage%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthEngineGoldDark)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = GrowthEngineGoldDark
        )
    }
}
