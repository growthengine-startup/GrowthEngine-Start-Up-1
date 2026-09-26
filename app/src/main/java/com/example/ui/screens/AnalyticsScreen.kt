package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.ExpenseEntity
import com.example.data.model.InvoiceEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    invoices: List<InvoiceEntity>,
    expenses: List<ExpenseEntity>
) {
    val totalRevenue = invoices.sumOf { it.totalAmount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpenses
    val netMarginPercent = if (totalRevenue > 0) (netProfit / totalRevenue) * 100 else 0.0

    // GST Output vs Input
    val gstOutputLiability = invoices.sumOf { it.cgstAmount + it.sgstAmount + it.igstAmount }
    val gstInputCredit = expenses.filter { it.isGstClaimable }.sumOf { it.gstAmount }
    val netGstPayable = maxOf(0.0, gstOutputLiability - gstInputCredit)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .testTag("analytics_screen")
    ) {
        // Header
        Surface(
            color = ImperialNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Executive Business Intelligence",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Financial health, profit margins, sales trajectory, and tax optimization",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            Text("Net Operating Profit", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(netProfit)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) ForestGreen else TerracottaRed
                            )
                            Text(
                                "Margin: ${String.format(java.util.Locale.ENGLISH, "%.1f", netMarginPercent)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                        }
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Net GST Cash Payable", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(netGstPayable)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
                            )
                            Text(
                                "ITC Set-off: ₹${MainViewModel.formatCurrencyPlain(gstInputCredit)}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Revenue vs Expenses Breakdown Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MONTHLY P&L SNAPSHOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        AnalyticsMetricRow("Gross Invoiced Sales", "₹${MainViewModel.formatCurrencyPlain(totalRevenue)}", ForestGreen)
                        AnalyticsMetricRow("Operating & Factory Expenses", "₹${MainViewModel.formatCurrencyPlain(totalExpenses)}", TerracottaRed)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = BorderSubtle)
                        AnalyticsMetricRow("EBITDA Earnings", "₹${MainViewModel.formatCurrencyPlain(netProfit)}", ImperialNavy, isBold = true)
                    }
                }
            }

            // Top Revenue Categories & Channels
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SALES CHANNEL CONTRIBUTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ChannelProgressRow("B2B Enterprise Invoices (GST Registered)", 68, "₹${MainViewModel.formatCurrencyPlain(totalRevenue * 0.68)}", ImperialNavy)
                        ChannelProgressRow("Counter POS & Retail Walk-ins", 22, "₹${MainViewModel.formatCurrencyPlain(totalRevenue * 0.22)}", ElectricBlue)
                        ChannelProgressRow("Quotations & Direct Orders", 10, "₹${MainViewModel.formatCurrencyPlain(totalRevenue * 0.10)}", GrowthEngineGoldDark)
                    }
                }
            }

            // GST Tax Optimization & ITC Utilization
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "GST TAX AUDIT & COMPLIANCE MATRIX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        AnalyticsMetricRow("Total Output GST Invoiced (GSTR-1)", "₹${MainViewModel.formatCurrencyPlain(gstOutputLiability)}", DarkInk)
                        AnalyticsMetricRow("Eligible ITC on Purchases (GSTR-2B)", "₹${MainViewModel.formatCurrencyPlain(gstInputCredit)}", ForestGreen)
                        AnalyticsMetricRow("Effective Tax Savings Rate", "${String.format(java.util.Locale.ENGLISH, "%.1f", if (gstOutputLiability > 0) (gstInputCredit / gstOutputLiability) * 100 else 0.0)}%", ElectricBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsMetricRow(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = if (isBold) DarkInk else TextSecondary, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
fun ChannelProgressRow(name: String, percent: Int, amount: String, barColor: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontSize = 11.sp, color = DarkInk)
            Text("$percent% ($amount)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkInk)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = barColor,
            trackColor = BorderSubtle,
        )
    }
}
