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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
                containerColor = GrowthEngineGold,
                contentColor = DarkInk,
                icon = { Icon(Icons.Default.AddBusiness, contentDescription = null) },
                text = { Text("Add Supplier", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_supplier")
            )
        },
        containerColor = BackgroundWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("suppliers_screen")
        ) {
            // Header
            Surface(
                color = BackgroundWhite,
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
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
                                text = "PROCUREMENT & VENDORS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Suppliers & Payables",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${suppliers.size} Registered Vendors",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = SurfaceSubtle,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Total Payables", fontSize = 9.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalPayable)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkInk
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
                        placeholder = { Text("Search by name, GSTIN, phone...", fontSize = 13.sp) },
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
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("ALL" to "All Vendors", "DUE" to "Outstanding Payables", "CLEAR" to "Settled").forEach { (key, label) ->
                            val isSelected = filterType == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { filterType = key },
                                label = { Text(label, fontSize = 11.sp) },
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
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SurfaceSubtle)
                                .border(1.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                        }
                        Text("No Suppliers Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Add vendor profiles to record purchase bills, track input tax credits (ITC), and manage payout schedules.", fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        SupplierListItemCard(
                            supplier = supplier,
                            onClick = { selectedSupplierProfile = supplier },
                            onRecordPayout = { onRecordPayout(supplier) },
                            onCreatePurchase = { onCreatePurchaseForSupplier(supplier) }
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
    onRecordPayout: () -> Unit,
    onCreatePurchase: () -> Unit
) {
    val payableAmt = -supplier.outstandingBalance

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
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
                        color = SurfaceSubtle,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = supplier.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = DarkInk,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = supplier.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        if (supplier.tradeName.isNotBlank() && supplier.tradeName != supplier.name) {
                            Text(
                                text = supplier.tradeName,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "GSTIN: ${if (supplier.gstin.isNotBlank()) supplier.gstin else "Unregistered"} • ${supplier.phone}",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${MainViewModel.formatCurrencyPlain(if (payableAmt > 0) payableAmt else 0.0)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (payableAmt > 0) DarkInk else SuccessGreenDark
                    )
                    Text(
                        text = if (payableAmt > 0) "To Pay" else "Settled",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${supplier.stateName} • Code: ${supplier.stateCode}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCreatePurchase,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = DarkInk, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Bill", fontSize = 11.sp, color = DarkInk)
                    }

                    if (payableAmt > 0) {
                        Button(
                            onClick = onRecordPayout,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkInk, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text("Pay Vendor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * STRICT ISOLATED SUPPLIER PROFILE & LEDGER VIEW
 */
@Composable
fun IsolatedSupplierProfileScreen(
    supplier: PartyEntity,
    onBack: () -> Unit,
    onRecordPayout: () -> Unit,
    onCreatePurchase: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val payableAmt = -supplier.outstandingBalance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .testTag("isolated_supplier_profile_${supplier.id}")
    ) {
        Surface(
            color = BackgroundWhite,
            tonalElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkInk)
                        }
                        Column {
                            Text(
                                text = supplier.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (supplier.tradeName.isNotBlank()) supplier.tradeName else "Supplier Account",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            text = "VENDOR LEDGER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
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
                            Text("Total Payable Balance", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(if (payableAmt > 0) payableAmt else 0.0)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (payableAmt > 0) DarkInk else SuccessGreenDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("State of Supply", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                "${supplier.stateName} (${supplier.stateCode})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRecordPayout,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Payout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCreatePurchase,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold, contentColor = DarkInk),
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "VENDOR REGISTRATION & TAX INFO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow("GSTIN", if (supplier.gstin.isNotBlank()) supplier.gstin else "Unregistered")
                        InfoRow("PAN Number", if (supplier.panNumber.isNotBlank()) supplier.panNumber else "N/A")
                        InfoRow("Phone", supplier.phone)
                        InfoRow("Email", if (supplier.email.isNotBlank()) supplier.email else "N/A")
                        InfoRow("Dispatch Origin", "${supplier.address}, ${supplier.stateName}")
                    }
                }
            }
        }
    }
}
