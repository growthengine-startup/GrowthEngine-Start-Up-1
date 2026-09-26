package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
    var selectedView by remember { mutableStateOf("ALL") }
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
            .background(BackgroundWhite)
            .testTag("khata_ledger_screen")
    ) {
        // Executive Cash & Khata Header
        Surface(
            color = BackgroundWhite,
            tonalElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DIGITAL KHATA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthEngineGoldDark,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Cashbook & Ledgers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    color = TextPrimary
                )
                Text(
                    text = "Real-time receivables, payables, and working capital balance",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorRedContainer),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("You'll Receive (Debtors)", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(totalReceivables)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRedDark
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("You'll Pay (Creditors)", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(totalPayables)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
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
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Cash In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRecordOutwardPayout,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Payout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // MSME Aging Buckets Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "RECEIVABLES AGING ANALYSIS (MSMED COMPLIANT)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AgingPill("0 - 30 Days", aging0To30, SuccessGreenDark, SuccessGreenContainer)
                            AgingPill("31 - 60 Days", aging31To60, WarningAmber, WarningAmberContainer)
                            AgingPill("60+ Days (Overdue)", aging60Plus, ErrorRedDark, ErrorRedContainer)
                        }
                    }
                }
            }

            // Parties Khata List
            item {
                Text(
                    text = "ACTIVE PARTIES & LEDGERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            items(customers.filter { it.outstandingBalance > 0 }, key = { it.id }) { customer ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectPartyForLedger(customer) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text("Customer • Phone: ${customer.phone}", fontSize = 11.sp, color = TextSecondary)
                            if (customer.overdueDays > 0) {
                                Text("Overdue by ${customer.overdueDays} days", fontSize = 10.sp, color = ErrorRedDark, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(customer.outstandingBalance)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ErrorRedDark
                            )
                            Text("You'll Receive", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgingPill(label: String, amount: Double, textColor: Color, containerColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, textColor.copy(alpha = 0.4f)),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Text(
                "₹${MainViewModel.formatCurrencyPlain(amount)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
