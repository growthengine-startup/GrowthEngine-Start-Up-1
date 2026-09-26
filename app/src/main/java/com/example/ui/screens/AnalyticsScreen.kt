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
import androidx.compose.ui.text.font.FontFamily
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

    val gstOutputLiability = invoices.sumOf { it.cgstAmount + it.sgstAmount + it.igstAmount }
    val gstInputCredit = expenses.filter { it.isGstClaimable }.sumOf { it.gstAmount }
    val netGstPayable = maxOf(0.0, gstOutputLiability - gstInputCredit)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .testTag("analytics_screen")
    ) {
        // Header
        Surface(
            color = BackgroundWhite,
            tonalElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BUSINESS INTELLIGENCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthEngineGoldDark,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Financial Analytics & Trends",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Financial health, profit margins, sales trajectory, and tax optimization",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Net Operating Profit", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(netProfit)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) SuccessGreenDark else ErrorRedDark
                            )
                            Text(
                                "Margin: ${String.format(java.util.Locale.ENGLISH, "%.1f", netMarginPercent)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreenDark
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
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MONTHLY P&L SNAPSHOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        AnalyticsMetricRow("Gross Invoiced Sales", "₹${MainViewModel.formatCurrencyPlain(totalRevenue)}", SuccessGreenDark)
                        AnalyticsMetricRow("Operating & Factory Expenses", "₹${MainViewModel.formatCurrencyPlain(totalExpenses)}", ErrorRedDark)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = BorderLight)
                        AnalyticsMetricRow("EBITDA Earnings", "₹${MainViewModel.formatCurrencyPlain(netProfit)}", DarkInk, isBold = true)
                    }
                }
            }

            // Top Customer Performance
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "REVENUE CONTRIBUTION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        invoices.groupBy { it.partyName }.toList().take(4).forEach { (name, partyInvoices) ->
                            val partySum = partyInvoices.sumOf { it.totalAmount }
                            val pct = if (totalRevenue > 0) ((partySum / totalRevenue) * 100).toInt() else 0
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text("₹${MainViewModel.formatCurrencyPlain(partySum)} ($pct%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                LinearProgressIndicator(
                                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = GrowthEngineGold,
                                    trackColor = BorderLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMetricRow(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = if (isBold) 14.sp else 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold, color = valueColor)
    }
}
