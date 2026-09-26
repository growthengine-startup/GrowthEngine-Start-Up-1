package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
        "PAID" to "Fully Settled",
        "QUOTATION" to "Quotations"
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateInvoiceClick,
                containerColor = ImperialNavy,
                contentColor = RoyalTeakGoldLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_create_invoice")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Invoice")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Tax Invoice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        containerColor = WarmIvoryBackground,
        modifier = Modifier.testTag("invoicing_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Invoice No, Customer or GSTIN...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondaryMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("invoice_search_input"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImperialNavy,
                    unfocusedBorderColor = WarmIvoryBorder,
                    focusedContainerColor = WarmIvorySurface,
                    unfocusedContainerColor = WarmIvorySurface
                )
            )

            // Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ImperialNavy,
                            selectedLabelColor = Color.White,
                            containerColor = WarmIvorySurface,
                            labelColor = TextPrimaryDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ImperialNavy else WarmIvoryBorder
                        )
                    )
                }
            }

            // Invoices List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredInvoices.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = TextSecondaryMuted, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Invoices Found", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                Text("No bills matched your search query or filter.", fontSize = 11.sp, color = TextSecondaryMuted)
                            }
                        }
                    }
                }

                items(filteredInvoices) { inv ->
                    Card(
                        onClick = { onViewInvoice(inv) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
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
                                        color = ImperialNavy
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (inv.isInterState) Color(0xFFF3E5F5) else Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (inv.isInterState) "IGST 18%" else "CGST+SGST",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inv.isInterState) Color(0xFF6A1B9A) else Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
                                color = TextPrimaryDark
                            )

                            Text(
                                text = "GSTIN: ${inv.partyGstin} • State: ${inv.partyState}",
                                fontSize = 11.sp,
                                color = TextSecondaryMuted
                            )

                            Text(
                                text = inv.itemsSummary,
                                fontSize = 11.sp,
                                color = TextSecondaryMuted,
                                maxLines = 1
                            )

                            HorizontalDivider(color = WarmIvoryBorder, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Date: ${MainViewModel.formatDate(inv.dateEpoch)}",
                                        fontSize = 10.sp,
                                        color = TextSecondaryMuted
                                    )
                                    if (inv.balanceDue > 0) {
                                        Text(
                                            text = "Due: ₹${MainViewModel.formatCurrencyPlain(inv.balanceDue)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (inv.paymentStatus == "OVERDUE") TerracottaRed else TextPrimaryDark
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹ ${MainViewModel.formatCurrencyPlain(inv.totalAmount)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ImperialNavy
                                        )
                                        Text(
                                            text = "Incl. GST",
                                            fontSize = 9.sp,
                                            color = TextSecondaryMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "View Invoice Form",
                                        tint = ImperialNavy,
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
