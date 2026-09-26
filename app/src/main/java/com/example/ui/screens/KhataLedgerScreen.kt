package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.model.PartyEntity
import com.example.data.model.PaymentEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataLedgerScreen(
    customers: List<PartyEntity>,
    suppliers: List<PartyEntity>,
    invoices: List<InvoiceEntity>,
    payments: List<PaymentEntity>,
    onRecordInwardPayment: () -> Unit,
    onRecordOutwardPayout: () -> Unit,
    onSelectPartyForLedger: (PartyEntity) -> Unit
) {
    var selectedView by remember { mutableStateOf("ALL") } // "ALL", "CASH_IN", "CASH_OUT"
    var searchQuery by remember { mutableStateOf("") }

    val totalReceivables = customers.sumOf { it.outstandingBalance }
    val totalPayables = suppliers.sumOf { -it.outstandingBalance }
    val netPosition = totalReceivables - totalPayables

    // Aging Buckets
    val aging0To30 = customers.filter { it.overdueDays in 0..30 && it.outstandingBalance > 0 }.sumOf { it.outstandingBalance }
    val aging31To60 = customers.filter { it.overdueDays in 31..60 }.sumOf { it.outstandingBalance }
    val aging60Plus = customers.filter { it.overdueDays > 60 }.sumOf { it.outstandingBalance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .testTag("khata_ledger_screen")
    ) {
        // Executive Cash & Khata Header
        Surface(
            color = ImperialNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Cashbook & Digital Khata",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = "Real-time receivables, payables, and working capital balance",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("You'll Receive (Debtors)", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(totalReceivables)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaRed
                            )
                        }
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("You'll Pay (Creditors)", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(totalPayables)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Cash In & Cash Out
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRecordInwardPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Cash In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRecordOutwardPayout,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Cash Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Aging Analysis Banner
        Surface(
            color = WarmIvorySurface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "RECEIVABLES AGING ANALYSIS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImperialNavy
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AgingBucketCard("0–30 Days", "₹${MainViewModel.formatCurrencyPlain(aging0To30)}", ForestGreen, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    AgingBucketCard("31–60 Days", "₹${MainViewModel.formatCurrencyPlain(aging31To60)}", WarningAmber, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    AgingBucketCard("60+ Days (Critical)", "₹${MainViewModel.formatCurrencyPlain(aging60Plus)}", ErrorRed, Modifier.weight(1f))
                }
            }
        }

        // Parties Khata List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Outstanding Customer Accounts (${customers.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkInk
                )
            }

            items(customers.sortedByDescending { it.outstandingBalance }, key = { it.id }) { cust ->
                KhataPartyRowCard(
                    party = cust,
                    isCustomer = true,
                    onClick = { onSelectPartyForLedger(cust) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Supplier Payables Accounts (${suppliers.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkInk
                )
            }

            items(suppliers.sortedByDescending { -it.outstandingBalance }, key = { it.id }) { supp ->
                KhataPartyRowCard(
                    party = supp,
                    isCustomer = false,
                    onClick = { onSelectPartyForLedger(supp) }
                )
            }
        }
    }
}

@Composable
fun AgingBucketCard(
    title: String,
    amount: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BackgroundWhite,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, fontSize = 9.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@Composable
fun KhataPartyRowCard(
    party: PartyEntity,
    isCustomer: Boolean,
    onClick: () -> Unit
) {
    val amount = if (isCustomer) party.outstandingBalance else -party.outstandingBalance
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = party.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DarkInk
                )
                Text(
                    text = "${if (isCustomer) "Customer" else "Supplier"} • ${party.phone}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${MainViewModel.formatCurrencyPlain(amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isCustomer) {
                        if (amount > 0) TerracottaRed else ForestGreen
                    } else {
                        if (amount > 0) ImperialNavy else ForestGreen
                    }
                )
                Text(
                    text = if (isCustomer) "Receivable" else "Payable",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
