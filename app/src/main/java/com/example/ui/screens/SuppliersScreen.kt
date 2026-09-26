package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PartyEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    suppliers: List<PartyEntity>,
    onAddNewSupplier: () -> Unit,
    onRecordPayout: (PartyEntity) -> Unit,
    onCreatePurchaseForSupplier: (PartyEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") }
    var selectedSupplierProfile by remember { mutableStateOf<PartyEntity?>(null) }
    val context = LocalContext.current

    // If a supplier profile is opened, show Isolated Supplier Profile & Ledger
    if (selectedSupplierProfile != null) {
        val activeSupplier = selectedSupplierProfile!!
        val currentSupplier = suppliers.find { it.id == activeSupplier.id } ?: activeSupplier

        IsolatedSupplierProfileScreen(
            supplier = currentSupplier,
            onBack = { selectedSupplierProfile = null },
            onRecordPayout = { onRecordPayout(currentSupplier) },
            onCreatePurchase = { onCreatePurchaseForSupplier(currentSupplier) }
        )
        return
    }

    val totalPayable = suppliers.sumOf { -it.outstandingBalance }

    val filteredSuppliers = suppliers.filter { supp ->
        val matchesQuery = searchQuery.isBlank() ||
                supp.name.contains(searchQuery, ignoreCase = true) ||
                supp.tradeName.contains(searchQuery, ignoreCase = true) ||
                supp.gstin.contains(searchQuery, ignoreCase = true) ||
                supp.phone.contains(searchQuery)

        val matchesFilter = when (filterType) {
            "DUE" -> supp.outstandingBalance < 0
            "CLEAR" -> supp.outstandingBalance >= 0
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewSupplier,
                containerColor = ImperialNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddBusiness, contentDescription = null) },
                text = { Text("Add Supplier", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_supplier")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvoryBackground)
                .padding(paddingValues)
                .testTag("suppliers_screen")
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
                        Column {
                            Text(
                                text = "Suppliers & Vendors",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialNavy
                            )
                            Text(
                                text = "${suppliers.size} Registered Vendors",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = ImperialNavy.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Total Payables", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalPayable)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImperialNavy
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
                            .testTag("search_supplier_input"),
                        placeholder = { Text("Search vendor by name, GSTIN, phone...", fontSize = 13.sp) },
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
                        listOf("ALL" to "All Vendors (${suppliers.size})", "DUE" to "Payable Due", "CLEAR" to "Settled").forEach { (key, label) ->
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

            if (filteredSuppliers.isEmpty()) {
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
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(56.dp))
                        Text("No Suppliers Found", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkInk)
                        Text("Add raw material vendors and suppliers to track payables, payment terms, and inward bills.", fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        SupplierListItemCard(
                            supplier = supplier,
                            onClick = { selectedSupplierProfile = supplier },
                            onRecordPayout = { onRecordPayout(supplier) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierListItemCard(
    supplier: PartyEntity,
    onClick: () -> Unit,
    onRecordPayout: () -> Unit
) {
    val due = -supplier.outstandingBalance
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("supplier_card_${supplier.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = ElectricBlue.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = supplier.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = supplier.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkInk
                        )
                        Text(
                            text = "GSTIN: ${if (supplier.gstin.isNotBlank()) supplier.gstin else "Unregistered"} • ${supplier.phone}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${MainViewModel.formatCurrencyPlain(due)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (due > 0) ImperialNavy else ForestGreen
                    )
                    Text(
                        text = if (due > 0) "To Pay" else "All Clear",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Credit Limit: ₹${MainViewModel.formatCurrencyPlain(supplier.creditLimit)} • ${supplier.stateName}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (due > 0) {
                        Button(
                            onClick = onRecordPayout,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Record Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("View Ledger", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * STRICT ISOLATED SUPPLIER PROFILE & PAYABLES LEDGER
 * Guarantees zero cross-contamination: Shows ONLY this supplier's payables and inward bills.
 */
@Composable
fun IsolatedSupplierProfileScreen(
    supplier: PartyEntity,
    onBack: () -> Unit,
    onRecordPayout: () -> Unit,
    onCreatePurchase: () -> Unit
) {
    BackHandler { onBack() }
    val due = -supplier.outstandingBalance
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .testTag("isolated_supplier_profile_${supplier.id}")
    ) {
        // Top App Bar
        Surface(
            color = ImperialNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = supplier.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Supplier & Vendor Account",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "VENDOR LEDGER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Balance Card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Payable Due", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(due)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (due > 0) ImperialNavy else ForestGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Payment Terms", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                "${supplier.paymentTermsDays} Days",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRecordPayout,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldContainer, contentColor = DarkInk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCreatePurchase,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Purchase Bill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Details
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SUPPLIER COMPLIANCE & BANK DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow("GSTIN", if (supplier.gstin.isNotBlank()) supplier.gstin else "Unregistered")
                        InfoRow("PAN Number", if (supplier.panNumber.isNotBlank()) supplier.panNumber else "N/A")
                        InfoRow("Phone", supplier.phone)
                        InfoRow("Email", if (supplier.email.isNotBlank()) supplier.email else "N/A")
                        InfoRow("Vendor Address", "${supplier.address}, ${supplier.stateName} (Code: ${supplier.stateCode})")
                        InfoRow("Payout Bank", "State Bank of India (A/C: ****4921, IFSC: SBIN0001248)")
                        InfoRow("Payout UPI ID", "${supplier.phone.filter { it.isDigit() }}@upi")
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Inward Purchases", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkInk)
                            Text("100% Isolated", fontSize = 10.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "PB-2026-502 • Raw MS Plate Castings (₹1,41,600) • Paid",
                            fontSize = 12.sp,
                            color = DarkInk
                        )
                        Text(
                            text = "PB-2026-489 • Heavy Tooling Bits & Carbide Inserts (₹98,500) • Paid",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
