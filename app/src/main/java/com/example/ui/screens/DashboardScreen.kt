package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        // Date & Greeting Header (Matching Screenshot 1)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FRIDAY, 25 SEPTEMBER · FY 2026-27",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Good evening, $businessName",
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Here’s how your business is doing today.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Action Buttons Row (Matching Screenshot 1)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // + New invoice (Solid Gold Button)
                Button(
                    onClick = onOpenCreateInvoice,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                    modifier = Modifier.testTag("btn_new_invoice")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF141414),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New invoice",
                        color = Color(0xFF141414),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Open POS Button
                OutlinedButton(
                    onClick = { onNavigate(AppNavTab.POS) },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                    modifier = Modifier.testTag("btn_open_pos")
                ) {
                    Icon(
                        imageVector = Icons.Default.PointOfSale,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open POS", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                // Add customer Button
                OutlinedButton(
                    onClick = onOpenAddCustomer,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                    modifier = Modifier.testTag("btn_add_customer")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add customer", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                // Add item Button
                OutlinedButton(
                    onClick = onOpenAddProduct,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                    modifier = Modifier.testTag("btn_add_item")
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add item", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 2x2 Metric Cards Grid (Matching Screenshot 1)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Today's sales
                    WhiteMetricCard(
                        title = "Today's sales",
                        value = "₹${MainViewModel.formatCurrencyPlain(totalSales)}",
                        subtitle = "${invoices.size} invoices",
                        icon = Icons.Default.CurrencyRupee,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavTab.INVOICING) }
                    )

                    // Receivable (udhaar)
                    WhiteMetricCard(
                        title = "Receivable (udhaar)",
                        value = "₹${MainViewModel.formatCurrencyPlain(totalReceivables)}",
                        subtitle = "${customers.size} parties",
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavTab.KHATA) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Payable
                    WhiteMetricCard(
                        title = "Payable",
                        value = "₹${MainViewModel.formatCurrencyPlain(totalPayables)}",
                        subtitle = "${suppliers.size} suppliers",
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavTab.SUPPLIERS) }
                    )

                    // Stock value
                    WhiteMetricCard(
                        title = "Stock value",
                        value = "₹${MainViewModel.formatCurrencyPlain(totalStockValuation)}",
                        subtitle = "${products.size} items",
                        icon = Icons.Default.Category,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppNavTab.INVENTORY) }
                    )
                }
            }
        }

        // Section: Sales this month (Matching Screenshot 2)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sales this month",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                    Text(
                        text = "Daily invoiced value including GST",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BackgroundWhite)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = GrowthEngineGoldDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "₹${MainViewModel.formatCurrencyPlain(totalSales)} invoiced",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = "100% GSTR-1 reconciled across ${invoices.size} B2B & retail invoices.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Section: Copilot (Matching Screenshot 2, 5, 8)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
                modifier = Modifier.fillMaxWidth().testTag("dashboard_copilot_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(GrowthEngineGoldContainer)
                                .border(0.5.dp, GrowthEngineGold, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GrowthEngineGoldDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Copilot",
                            fontSize = 17.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Insights from your business data",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "₹3,42,800 is overdue from 6 retailers. Gujarat Tooling alone owes ₹2.15L for 18 days past Net 21.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Slider indicator dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(GrowthEngineGoldDark))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(GrowthEngineGold))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(TextTertiary))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onNavigate(AppNavTab.COPILOT) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_ask_copilot")
                    ) {
                        Text(
                            text = "Ask the copilot ↗",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Section: Recent Invoices (Matching Screenshot 3)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent invoices",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )

                TextButton(onClick = { onNavigate(AppNavTab.INVOICING) }) {
                    Text("View all", color = GrowthEngineGoldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Invoice cards
        items(invoices.take(3)) { inv ->
            Card(
                onClick = { onViewInvoice(inv) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                                text = inv.invoiceNumber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(status = inv.paymentStatus)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = inv.partyName,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${MainViewModel.formatDate(inv.dateEpoch)} • GSTIN: ${inv.partyGstin}",
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${MainViewModel.formatCurrencyPlain(inv.totalAmount)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (inv.balanceDue > 0) {
                            Text(
                                text = "Due: ₹${MainViewModel.formatCurrencyPlain(inv.balanceDue)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (inv.paymentStatus == "OVERDUE") ErrorRed else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Section: Get started (Matching Screenshot 3)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth().testTag("get_started_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Get started",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                    Text(
                        text = "Set up your books in a few minutes",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val steps = listOf(
                        "01" to "Add your first customer or supplier",
                        "02" to "Add items with HSN code and GST rate",
                        "03" to "Raise your first GST invoice",
                        "04" to "Record a UPI or cash payment"
                    )

                    steps.forEach { (num, text) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.5.dp, TextTertiary, CircleShape)
                                )
                                Text(
                                    text = text,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = num,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary
                            )
                        }
                        if (num != "04") {
                            HorizontalDivider(color = BorderLight, modifier = Modifier.padding(start = 26.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WhiteMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
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
