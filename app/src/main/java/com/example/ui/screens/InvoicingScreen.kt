package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceEntity
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun InvoicingScreen(
    invoices: List<InvoiceEntity>,
    onViewInvoice: (InvoiceEntity) -> Unit,
    onCreateInvoiceClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf(
        "ALL" to "All Invoices (${invoices.size})",
        "UNPAID" to "Pending & Due",
        "OVERDUE" to "Overdue",
        "PAID" to "Settled",
        "QUOTATION" to "Estimates"
    )

    val filteredInvoices = invoices.filter { inv ->
        val matchesSearch = inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.partyName.contains(searchQuery, ignoreCase = true) ||
                inv.partyGstin.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "UNPAID" -> inv.paymentStatus == "UNPAID" || inv.paymentStatus == "PARTIAL"
            "OVERDUE" -> inv.paymentStatus == "OVERDUE"
            "PAID" -> inv.paymentStatus == "PAID"
            "QUOTATION" -> inv.invoiceType == "QUOTATION"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    val totalSales = invoices.sumOf { it.totalAmount }
    val totalPending = invoices.filter { it.paymentStatus != "PAID" }.sumOf { it.balanceDue }
    val paidCount = invoices.count { it.paymentStatus == "PAID" }
    val overdueCount = invoices.count { it.paymentStatus == "OVERDUE" }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateInvoiceClick,
                containerColor = DarkInk,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Create Invoice") },
                text = { Text("New Tax Invoice", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("fab_create_invoice")
            )
        },
        containerColor = Color(0xFFF7F8FA),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.testTag("invoicing_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ═══════════════════════════════════════════════════════════
            // PREMIUM HEADER WITH GRADIENT
            // ═══════════════════════════════════════════════════════════
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
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column {
                    // Title Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GST BILLING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Tax Invoices & Bills",
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Total invoiced badge
                        Surface(
                            color = DarkInk,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    "Invoiced",
                                    fontSize = 9.sp,
                                    color = GrowthEngineGoldLight,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalSales)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Quick Stats Row ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InvoiceStatChip(
                            label = "Pending",
                            value = "₹${MainViewModel.formatCurrencyPlain(totalPending)}",
                            color = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        InvoiceStatChip(
                            label = "Paid",
                            value = "$paidCount",
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        InvoiceStatChip(
                            label = "Overdue",
                            value = "$overdueCount",
                            color = ErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Search ──
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search by Invoice No, Customer or GSTIN...",
                                fontSize = 13.sp,
                                color = TextTertiary
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invoice_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthEngineGold,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Filter Chips ──
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterOptions) { (key, label) ->
                            val isSelected = selectedFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = key },
                                label = {
                                    Text(
                                        label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkInk,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) DarkInk else BorderSubtle
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════════════
            // INVOICE LIST
            // ═══════════════════════════════════════════════════════════
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredInvoices.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ReceiptLong,
                                        null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No Invoices Found",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "No invoices match your search or filter.",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                items(filteredInvoices, key = { it.id }) { inv ->
                    PremiumInvoiceListCard(
                        invoice = inv,
                        onClick = { onViewInvoice(inv) }
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════
// COMPONENT: Invoice Stat Chip
// ═════════════════════════════════════════════════════════════════════
@Composable
private fun InvoiceStatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(label, fontSize = 9.sp, color = TextSecondary)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════
// COMPONENT: Premium Invoice List Card
// ═════════════════════════════════════════════════════════════════════
@Composable
private fun PremiumInvoiceListCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit
) {
    val statusColor = when (invoice.paymentStatus) {
        "PAID" -> SuccessGreen
        "PARTIAL" -> WarningAmber
        "OVERDUE" -> ErrorRed
        else -> Color(0xFFEF4444)
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("invoice_card_${invoice.invoiceNumber}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(statusColor, statusColor.copy(alpha = 0.2f))
                        )
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Row 1: Invoice number + Tax type + Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = invoice.invoiceNumber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Tax type badge
                        Surface(
                            color = if (invoice.isInterState) Color(0xFFF3E8FF) else Color(0xFFECFDF5),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (invoice.isInterState) "IGST 18%" else "CGST+SGST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (invoice.isInterState) Color(0xFF7E22CE) else Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    StatusBadge(status = invoice.paymentStatus)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Party name
                Text(
                    text = invoice.partyName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Row 3: GSTIN + State
                Text(
                    text = "GSTIN: ${if (invoice.partyGstin.isNotBlank()) invoice.partyGstin else "Unregistered"} • State: ${invoice.partyState}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Row 4: Items summary
                Text(
                    text = invoice.itemsSummary,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(10.dp))

                // Row 5: Date + Due + Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Date: ${MainViewModel.formatDate(invoice.dateEpoch)}",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                        if (invoice.balanceDue > 0) {
                            Text(
                                text = "Due: ₹${MainViewModel.formatCurrencyPlain(invoice.balanceDue)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (invoice.paymentStatus == "OVERDUE") ErrorRed else TextPrimary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${MainViewModel.formatCurrencyPlain(invoice.totalAmount)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
                            )
                            Text(
                                text = "Incl. GST",
                                fontSize = 9.sp,
                                color = TextTertiary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            "View",
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
