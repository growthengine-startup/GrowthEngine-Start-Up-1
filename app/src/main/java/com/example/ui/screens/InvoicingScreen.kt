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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val totalPending = invoices.sumOf { it.balanceDue }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateInvoiceClick,
                containerColor = GrowthEngineGold,
                contentColor = DarkInk,
                shape = RoundedCornerShape(12.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Create Invoice") },
                text = { Text("New Tax Invoice", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("fab_create_invoice")
            )
        },
        containerColor = BackgroundWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.testTag("invoicing_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Surface(
                color = BackgroundWhite,
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "GST BILLING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Tax Invoices & Bills",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            color = SurfaceSubtle,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Invoiced:", fontSize = 10.sp, color = TextSecondary)
                                Text("₹${MainViewModel.formatCurrencyPlain(totalSales)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by Invoice No, Customer or GSTIN...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invoice_search_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Tabs
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filterOptions) { (key, label) ->
                            val isSelected = selectedFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkInk,
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceSubtle,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) DarkInk else BorderSubtle
                                )
                            )
                        }
                    }
                }
            }

            // Invoices List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredInvoices.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceWhite)
                                        .border(1.dp, BorderSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No Invoices Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("No tax invoices matched your search query or filter.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                items(filteredInvoices, key = { it.id }) { inv ->
                    Card(
                        onClick = { onViewInvoice(inv) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth().testTag("invoice_card_${inv.invoiceNumber}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = inv.invoiceNumber,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (inv.isInterState) Color(0xFFF3E8FF) else Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (inv.isInterState) "IGST 18%" else "CGST+SGST",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inv.isInterState) Color(0xFF7E22CE) else Color(0xFF047857),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                StatusBadge(status = inv.paymentStatus)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = inv.partyName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Text(
                                text = "GSTIN: ${if (inv.partyGstin.isNotBlank()) inv.partyGstin else "Unregistered"} • State: ${inv.partyState}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Text(
                                text = inv.itemsSummary,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )

                            HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Date: ${MainViewModel.formatDate(inv.dateEpoch)}",
                                        fontSize = 10.sp,
                                        color = TextTertiary
                                    )
                                    if (inv.balanceDue > 0) {
                                        Text(
                                            text = "Due: ₹${MainViewModel.formatCurrencyPlain(inv.balanceDue)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inv.paymentStatus == "OVERDUE") ErrorRedDark else TextPrimary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${MainViewModel.formatCurrencyPlain(inv.totalAmount)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkInk
                                        )
                                        Text(
                                            text = "Incl. GST",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "View Invoice",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
