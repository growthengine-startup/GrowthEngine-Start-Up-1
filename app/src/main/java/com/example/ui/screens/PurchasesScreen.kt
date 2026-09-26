package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.PartyEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class PurchaseBillModel(
    val id: Long,
    val billNumber: String,
    val supplierName: String,
    val supplierGstin: String,
    val billDate: Long,
    val itemsSummary: String,
    val taxableAmount: Double,
    val itcGstAmount: Double,
    val totalAmount: Double,
    val paymentStatus: String, // "PAID", "UNPAID", "PARTIAL"
    val isItcClaimable: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    suppliers: List<PartyEntity>,
    onAddPurchaseBillClick: () -> Unit,
    onViewBillDetails: (PurchaseBillModel) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") }

    // Seed/derive realistic purchase bills
    val purchaseBills = remember(suppliers) {
        val sampleSuppliers = if (suppliers.isNotEmpty()) suppliers else listOf(
            PartyEntity(name = "Kirloskar Ferrous Industries", tradeName = "Kirloskar Plant", type = "SUPPLIER", gstin = "27AAACK1928M1Z2", panNumber = "AAACK1928M", phone = "+91 98229 11029", email = "sales@kirloskar.com", address = "Solapur Road, Pune", stateName = "Maharashtra", stateCode = "27", creditLimit = 2000000.0, outstandingBalance = -340000.0, paymentTermsDays = 30)
        )

        sampleSuppliers.mapIndexed { idx, supp ->
            val taxable = 120000.0 + (idx * 45000.0)
            val gst = taxable * 0.18
            PurchaseBillModel(
                id = 100L + idx,
                billNumber = "PB-2026-${501 + idx}",
                supplierName = supp.name,
                supplierGstin = if (supp.gstin.isNotBlank()) supp.gstin else "27AABCS9910J1Z4",
                billDate = System.currentTimeMillis() - (idx * 86400000L * 4),
                itemsSummary = "Raw Material Castings, MS Flats, Industrial Fasteners, Lubricants Grade 46",
                taxableAmount = taxable,
                itcGstAmount = gst,
                totalAmount = taxable + gst,
                paymentStatus = if (idx % 2 == 0) "PAID" else "UNPAID",
                isItcClaimable = true
            )
        }
    }

    val filteredBills = purchaseBills.filter { bill ->
        (searchQuery.isBlank() || bill.supplierName.contains(searchQuery, ignoreCase = true) || bill.billNumber.contains(searchQuery, ignoreCase = true)) &&
                (filterType == "ALL" || (filterType == "PAID" && bill.paymentStatus == "PAID") || (filterType == "UNPAID" && bill.paymentStatus != "PAID"))
    }

    val totalPurchases = purchaseBills.sumOf { it.totalAmount }
    val totalItcClaimable = purchaseBills.filter { it.isItcClaimable }.sumOf { it.itcGstAmount }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPurchaseBillClick,
                containerColor = ImperialNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Purchase Bill", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_purchase")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvoryBackground)
                .padding(paddingValues)
                .testTag("purchases_screen")
        ) {
            // Header
            Surface(
                color = WarmIvorySurface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Purchases & Inward GST",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialNavy
                            )
                            Text(
                                text = "${purchaseBills.size} Inward Bills • ₹${MainViewModel.formatCurrencyPlain(totalPurchases)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = ForestGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("GSTR-2B ITC", fontSize = 9.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalItcClaimable)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_purchase_input"),
                        placeholder = { Text("Search by supplier name or bill #", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImperialNavy,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BackgroundWhite,
                            unfocusedContainerColor = BackgroundWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("ALL" to "All Purchases", "UNPAID" to "Pending Payment", "PAID" to "Paid Bills").forEach { (key, label) ->
                            FilterChip(
                                selected = filterType == key,
                                onClick = { filterType = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ImperialNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            if (filteredBills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(56.dp))
                        Text("No Purchase Bills", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkInk)
                        Text("Record vendor purchases and raw material bills to automatically claim Input Tax Credit (ITC) on GSTR-3B.", fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBills, key = { it.id }) { bill ->
                        PurchaseBillCard(
                            bill = bill,
                            onClick = { onViewBillDetails(bill) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseBillCard(
    bill: PurchaseBillModel,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(bill.billDate))
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("purchase_card_${bill.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bill.supplierName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkInk
                    )
                    Text(
                        text = "${bill.billNumber} • $dateStr • GSTIN: ${bill.supplierGstin}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = if (bill.paymentStatus == "PAID") ForestGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = bill.paymentStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (bill.paymentStatus == "PAID") ForestGreen else WarningAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bill.itemsSummary,
                fontSize = 12.sp,
                color = DarkInk.copy(alpha = 0.85f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Inward Bill", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        "₹${MainViewModel.formatCurrencyPlain(bill.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkInk
                    )
                }

                Surface(
                    color = ForestGreen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, ForestGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(12.dp))
                        Text(
                            text = "ITC: ₹${MainViewModel.formatCurrencyPlain(bill.itcGstAmount)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen
                        )
                    }
                }
            }
        }
    }
}
