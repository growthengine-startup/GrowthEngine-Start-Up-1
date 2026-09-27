package com.example.ui.screens

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceEntity
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    businessName: String,
    invoices: List<InvoiceEntity>,
    customers: List<PartyEntity>,
    suppliers: List<PartyEntity>,
    products: List<ProductEntity>,
    onNavigate: (AppNavTab) -> Unit,
    onViewInvoice: (InvoiceEntity) -> Unit,
    onOpenCreateInvoice: () -> Unit,
    onOpenRecordPayment: () -> Unit,
    onOpenAddCustomer: () -> Unit,
    onOpenAddProduct: () -> Unit
) {
    val totalSales = invoices.sumOf { it.totalAmount }
    val totalReceivables = customers.sumOf { it.outstandingBalance }
    val totalPayables = suppliers.sumOf { -it.outstandingBalance }
    val totalStockValuation = products.sumOf { it.currentStock * it.purchasePrice }

    val now = try { LocalTime.now() } catch (_: Exception) { null }
    val greeting = when {
        now == null -> "Hello"
        now.hour < 12 -> "Good morning"
        now.hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    val today = try {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)).uppercase()
    } catch (_: Exception) {
        "TODAY"
    }
    val fiscalYear = try {
        val y = LocalDate.now().let { if (it.monthValue >= 4) it.year else it.year - 1 }
        "FY ${y}-${(y + 1) % 100}"
    } catch (_: Exception) {
        "FY 2026-27"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // ═══════════════════════════════════════════════════════════════
        // PREMIUM GRADIENT HEADER
        // ═══════════════════════════════════════════════════════════════
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFEF9EF),
                                Color(0xFFFDF6E3),
                                Color(0xFFF7F8FA)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = "$today · $fiscalYear",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthEngineGoldDark,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$greeting,",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = businessName.ifBlank { "My Enterprise" },
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Here's how your business is doing today.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // QUICK ACTION BUTTONS
        // ═══════════════════════════════════════════════════════════════
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary: + New Invoice
                Button(
                    onClick = onOpenCreateInvoice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkInk,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier.testTag("btn_new_invoice")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New invoice", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Secondary: Open POS
                PremiumOutlinedAction(
                    text = "Open POS",
                    icon = Icons.Default.PointOfSale,
                    onClick = { onNavigate(AppNavTab.POS) },
                    testTag = "btn_open_pos"
                )

                // Secondary: Add customer
                PremiumOutlinedAction(
                    text = "Add customer",
                    icon = Icons.Default.PersonAdd,
                    onClick = onOpenAddCustomer,
                    testTag = "btn_add_customer"
                )

                // Secondary: Add item
                PremiumOutlinedAction(
                    text = "Add item",
                    icon = Icons.Default.Inventory2,
                    onClick = onOpenAddProduct,
                    testTag = "btn_add_item"
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ═══════════════════════════════════════════════════════════════
        // 2x2 METRIC CARDS — Premium with Accent Lines
        // ═══════════════════════════════════════════════════════════════
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumMetricCard(
                        title = "Today's sales",
                        value = "₹${MainViewModel.formatCurrencyPlain(totalSales)}",
                        subtitle = "${invoices.size} invoices",
                        icon = Icons.Default.CurrencyRupee,
                        accentColor = GrowthEngineGold,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavTab.INVOICING) }
                    )
                    PremiumMetricCard(
                        title = "Receivable",
                        value = "₹${MainViewModel.formatCurrencyPlain(totalReceivables)}",
                        subtitle = "${customers.size} parties",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavTab.KHATA) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumMetricCard(
                        title = "Payable",
                        value = "₹${MainViewModel.formatCurrencyPlain(totalPayables)}",
                        subtitle = "${suppliers.size} suppliers",
                        icon = Icons.Default.Receipt,
                        accentColor = Color(0xFFF97316),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavTab.SUPPLIERS) }
                    )
                    PremiumMetricCard(
                        title = "Stock value",
                        value = "₹${MainViewModel.formatCurrencyPlain(totalStockValuation)}",
                        subtitle = "${products.size} items",
                        icon = Icons.Default.Category,
                        accentColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavTab.INVENTORY) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // ═══════════════════════════════════════════════════════════════
        // SALES THIS MONTH — Mini Chart
        // ═══════════════════════════════════════════════════════════════
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Sales this month",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Daily invoiced value incl. GST",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            color = SuccessGreenContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.TrendingUp,
                                    null,
                                    tint = SuccessGreenDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Mini Bar Chart ──
                    MiniBarChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        GrowthEngineGoldContainer,
                                        Color(0xFFFFF8EB)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "₹${MainViewModel.formatCurrencyPlain(totalSales)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Total invoiced this month",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            color = DarkInk,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "GSTR-1 ✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // ═══════════════════════════════════════════════════════════════
        // AI COPILOT CARD — Premium Gradient Border
        // ═══════════════════════════════════════════════════════════════
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GrowthEngineGold,
                                GrowthEngineGoldLight,
                                GrowthEngineGold
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("dashboard_copilot_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Premium AI icon with gradient background
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            GrowthEngineGold,
                                            GrowthEngineGoldDark
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Copilot",
                                fontSize = 17.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Insights from your business data",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Insight text with subtle background
                    Surface(
                        color = Color(0xFFFFFBF0),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "₹3,42,800 is overdue from 6 retailers. Gujarat Tooling alone owes ₹2.15L for 18 days past Net 21.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pagination dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(true, false, false).forEachIndexed { idx, isActive ->
                            Box(
                                modifier = Modifier
                                    .size(
                                        width = if (isActive) 20.dp else 6.dp,
                                        height = 6.dp
                                    )
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isActive) GrowthEngineGold else Color(0xFFE0E0E0))
                            )
                            if (idx < 2) Spacer(modifier = Modifier.width(5.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CTA Button
                    Button(
                        onClick = { onNavigate(AppNavTab.COPILOT) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkInk,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_ask_copilot"),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask the copilot",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("↗", fontSize = 14.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // ═══════════════════════════════════════════════════════════════
        // RECENT INVOICES
        // ═══════════════════════════════════════════════════════════════
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent invoices",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = { onNavigate(AppNavTab.INVOICING) }) {
                    Text(
                        "View all",
                        color = GrowthEngineGoldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = GrowthEngineGoldDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        items(invoices.take(3)) { inv ->
            PremiumInvoiceCard(
                invoice = inv,
                onClick = { onViewInvoice(inv) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // ═══════════════════════════════════════════════════════════════
        // GET STARTED — Onboarding Checklist
        // ═══════════════════════════════════════════════════════════════
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("get_started_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.RocketLaunch,
                                null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Get started",
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Set up your books in a few minutes",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val steps = listOf(
                        Triple("01", "Add your first customer or supplier", Icons.Default.PersonAdd),
                        Triple("02", "Add items with HSN code and GST rate", Icons.Default.Inventory2),
                        Triple("03", "Raise your first GST invoice", Icons.Default.Description),
                        Triple("04", "Record a UPI or cash payment", Icons.Default.Payment)
                    )

                    steps.forEachIndexed { index, (num, text, icon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (index == 0) Color(0xFFF8FAFC) else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Step circle
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(1.5.dp, BorderSubtle, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = text,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = num,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextTertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (index < steps.lastIndex) {
                            HorizontalDivider(
                                color = BorderLight,
                                modifier = Modifier.padding(start = 48.dp, end = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════
// COMPONENT: Premium Outlined Action Button
// ═════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumOutlinedAction(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        modifier = Modifier.testTag(testTag)
    ) {
        Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ═════════════════════════════════════════════════════════════════════
// COMPONENT: Premium Metric Card with colored accent strip
// ═════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Colored accent strip at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.3f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════
// COMPONENT: Premium Invoice Card for Dashboard
// ═════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumInvoiceCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (invoice.paymentStatus) {
        "PAID" -> SuccessGreen
        "PARTIAL" -> WarningAmber
        "OVERDUE" -> ErrorRed
        else -> Color(0xFFEF4444) // DUE → red-ish
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(80.dp)
                    .background(statusColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.5f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = invoice.invoiceNumber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(status = invoice.paymentStatus)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = invoice.partyName,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${MainViewModel.formatDate(invoice.dateEpoch)} • GSTIN: ${invoice.partyGstin}",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${MainViewModel.formatCurrencyPlain(invoice.totalAmount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (invoice.balanceDue > 0) {
                        Text(
                            text = "Due: ₹${MainViewModel.formatCurrencyPlain(invoice.balanceDue)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (invoice.paymentStatus == "OVERDUE") ErrorRed else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════
// COMPONENT: Mini Bar Chart (visual sales graph)
// ═════════════════════════════════════════════════════════════════════
@Composable
private fun MiniBarChart(modifier: Modifier = Modifier) {
    val goldColor = GrowthEngineGold
    val goldLight = GrowthEngineGoldLight
    val borderColor = BorderLight

    Canvas(modifier = modifier) {
        val barCount = 7
        val barWidth = size.width / (barCount * 2f)
        val gap = barWidth
        val maxHeight = size.height - 8f

        // Simulated bar heights (percentage of max)
        val heights = listOf(0.4f, 0.65f, 0.35f, 0.85f, 0.55f, 0.7f, 1.0f)

        // Draw baseline
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1f
        )

        heights.forEachIndexed { i, h ->
            val x = i * (barWidth + gap) + gap / 2
            val barHeight = maxHeight * h
            val y = size.height - barHeight

            // Bar with rounded top
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(goldColor, goldLight),
                    startY = y,
                    endY = size.height
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 3, barWidth / 3)
            )
        }
    }
}
