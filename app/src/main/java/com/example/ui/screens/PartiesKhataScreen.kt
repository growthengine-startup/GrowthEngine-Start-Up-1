package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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

@Composable
fun PartiesKhataScreen(
    customers: List<PartyEntity>,
    suppliers: List<PartyEntity>,
    onSelectCustomerForPayment: (PartyEntity) -> Unit,
    onSendWhatsAppReminder: (PartyEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf("CUSTOMERS") }
    var searchQuery by remember { mutableStateOf("") }

    val activeList = if (selectedTab == "CUSTOMERS") customers else suppliers
    val totalBalance = if (selectedTab == "CUSTOMERS") {
        customers.sumOf { it.outstandingBalance }
    } else {
        suppliers.sumOf { -it.outstandingBalance }
    }

    val filteredList = activeList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.tradeName.contains(searchQuery, ignoreCase = true) ||
                it.gstin.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .testTag("khata_screen")
    ) {
        // Khata Ledger Switcher Header
        Surface(
            color = WarmIvorySurface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedTab == "CUSTOMERS") "Customer Khata (Receivables)" else "Supplier Khata (Payables)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                        Text(
                            text = if (selectedTab == "CUSTOMERS") "Total Pending: ₹${MainViewModel.formatCurrencyPlain(totalBalance)}" else "Total Due: ₹${MainViewModel.formatCurrencyPlain(totalBalance)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedTab == "CUSTOMERS") TerracottaRed else ImperialNavy
                        )
                    }

                    Row {
                        FilterChip(
                            selected = selectedTab == "CUSTOMERS",
                            onClick = { selectedTab = "CUSTOMERS" },
                            label = { Text("Debtors", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ImperialNavy,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("tab_debtors")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = selectedTab == "SUPPLIERS",
                            onClick = { selectedTab = "SUPPLIERS" },
                            label = { Text("Vendors", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ImperialNavy,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("tab_vendors")
                        )
                    }
                }
            }
        }

        // Aging Analysis Summary Strip (for Debtors)
        if (selectedTab == "CUSTOMERS") {
            Surface(
                color = WarmIvorySurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Current (0-15d)", fontSize = 9.sp, color = TextSecondaryMuted)
                        Text("₹ 92,000", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestEmerald)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("16-30d", fontSize = 9.sp, color = TextSecondaryMuted)
                        Text("₹ 4,85,000", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("31-45d", fontSize = 9.sp, color = TextSecondaryMuted)
                        Text("₹ 2,15,400", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaRed)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("45d+ (MSME Risk)", fontSize = 9.sp, color = TextSecondaryMuted)
                        Text("₹ 18,500", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaRed)
                    }
                }
            }
        }

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Firm Name, GSTIN or Contact...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondaryMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("khata_search_input"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ImperialNavy,
                unfocusedBorderColor = WarmIvoryBorder,
                focusedContainerColor = WarmIvorySurface,
                unfocusedContainerColor = WarmIvorySurface
            )
        )

        // Party List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { party ->
                val balance = if (selectedTab == "CUSTOMERS") party.outstandingBalance else -party.outstandingBalance
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
                    modifier = Modifier.fillMaxWidth().testTag("khata_party_${party.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = party.tradeName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = party.name,
                                    fontSize = 11.sp,
                                    color = TextSecondaryMuted
                                )
                                Text(
                                    text = "GSTIN: ${party.gstin} • State: ${party.stateName} (${party.stateCode})",
                                    fontSize = 10.sp,
                                    color = TextSecondaryMuted
                                )
                                Text(
                                    text = "Phone: ${party.phone} • Terms: Net ${party.paymentTermsDays}",
                                    fontSize = 10.sp,
                                    color = TextSecondaryMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹ ${MainViewModel.formatCurrencyPlain(balance)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (balance > 0) TerracottaRed else ForestEmerald
                                )
                                if (party.overdueDays > 0) {
                                    Surface(
                                        color = TerracottaRedContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${party.overdueDays}d Overdue",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaRed,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (balance == 0.0) {
                                    Surface(
                                        color = ForestEmeraldContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "All Clear",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestEmerald,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedTab == "CUSTOMERS" && balance > 0) {
                            HorizontalDivider(color = WarmIvoryBorder, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onSendWhatsAppReminder(party) },
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_whatsapp_${party.id}")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = ForestEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp Notice", fontSize = 11.sp, color = ForestEmerald)
                                }

                                Button(
                                    onClick = { onSelectCustomerForPayment(party) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_receive_khata_${party.id}")
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receive ₹", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
