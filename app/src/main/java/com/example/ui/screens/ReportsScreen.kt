package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun ReportsScreen(
    invoices: List<InvoiceEntity>,
    expenses: List<ExpenseEntity>
) {
    val totalSales = invoices.sumOf { it.totalAmount }
    val totalTaxable = invoices.sumOf { it.subtotal }
    val totalCgst = invoices.sumOf { it.cgstAmount }
    val totalSgst = invoices.sumOf { it.sgstAmount }
    val totalIgst = invoices.sumOf { it.igstAmount }
    val totalOutputTax = totalCgst + totalSgst + totalIgst

    val totalExpenses = expenses.sumOf { it.amount }
    val totalItc = expenses.filter { it.isGstClaimable }.sumOf { it.gstAmount }
    val netGstPayable = (totalOutputTax - totalItc).coerceAtLeast(0.0)

    val cogs = totalTaxable * 0.58 // estimated cost of materials & manufacturing
    val grossProfit = totalTaxable - cogs
    val netProfit = grossProfit - totalExpenses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // GSTR-1 Outward Supply Summary Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
            modifier = Modifier.fillMaxWidth().testTag("gstr1_report_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GSTR-1 TAX SUMMARY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Monthly Return of Outward Supplies",
                            fontSize = 10.sp,
                            color = TextSecondaryMuted
                        )
                    }

                    Surface(
                        color = ForestEmeraldContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "GSTR-1 READY",
                            color = ForestEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(color = WarmIvoryBorder, modifier = Modifier.padding(vertical = 10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Invoiced Sales:", fontSize = 11.sp, color = TextSecondaryMuted)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(totalSales)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Aggregate Taxable Value:", fontSize = 11.sp, color = TextSecondaryMuted)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(totalTaxable)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = WarmIvorySurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Central GST (CGST):", fontSize = 10.sp, color = TextSecondaryMuted)
                            Text("₹ ${MainViewModel.formatCurrencyPlain(totalCgst)}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("State GST (SGST):", fontSize = 10.sp, color = TextSecondaryMuted)
                            Text("₹ ${MainViewModel.formatCurrencyPlain(totalSgst)}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Integrated GST (IGST Inter-state):", fontSize = 10.sp, color = TextSecondaryMuted)
                            Text("₹ ${MainViewModel.formatCurrencyPlain(totalIgst)}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(color = WarmIvoryBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Output Tax Liability:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("₹ ${MainViewModel.formatCurrencyPlain(totalOutputTax)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ImperialNavy)
                        }
                    }
                }
            }
        }

        // GSTR-3B Liability & ITC Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
            modifier = Modifier.fillMaxWidth().testTag("gstr3b_report_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GSTR-3B TAX SETTLEMENT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Electronic Cash Ledger vs Input Tax Credit",
                            fontSize = 10.sp,
                            color = TextSecondaryMuted
                        )
                    }

                    Surface(
                        color = RoyalTeakGoldContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "DUE: 20TH",
                            color = RoyalTeakGoldDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(color = WarmIvoryBorder, modifier = Modifier.padding(vertical = 10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Output Tax (Sales):", fontSize = 11.sp, color = TextSecondaryMuted)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(totalOutputTax)}", fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Eligible Input Tax Credit (ITC):", fontSize = 11.sp, color = ForestEmerald)
                    Text("(-) ₹ ${MainViewModel.formatCurrencyPlain(totalItc)}", fontSize = 11.sp, color = ForestEmerald, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = WarmIvoryBorder, modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Cash GST Payable:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Text(
                        text = "₹ ${MainViewModel.formatCurrencyPlain(netGstPayable)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalTeakGoldDark
                    )
                }
            }
        }

        // P&L Statement Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
            modifier = Modifier.fillMaxWidth().testTag("pnl_report_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PROFIT & LOSS (P&L) STATEMENT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImperialNavy,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Financial Year 2024-25 Run Rate",
                    fontSize = 10.sp,
                    color = TextSecondaryMuted
                )

                HorizontalDivider(color = WarmIvoryBorder, modifier = Modifier.padding(vertical = 10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gross Revenue (Sales Excl. GST):", fontSize = 11.sp, color = TextSecondaryMuted)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(totalTaxable)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cost of Goods Sold (COGS 58%):", fontSize = 11.sp, color = TextSecondaryMuted)
                    Text("(-) ₹ ${MainViewModel.formatCurrencyPlain(cogs)}", fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Operating Expenses (Rent, Power, Wages):", fontSize = 11.sp, color = TextSecondaryMuted)
                    Text("(-) ₹ ${MainViewModel.formatCurrencyPlain(totalExpenses)}", fontSize = 11.sp)
                }

                HorizontalDivider(color = WarmIvoryBorder, modifier = Modifier.padding(vertical = 4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Operating Profit (EBIT):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Text(
                        text = "₹ ${MainViewModel.formatCurrencyPlain(netProfit)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestEmerald
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Operating Margin:", fontSize = 11.sp, color = TextSecondaryMuted)
                    val marginPercent = if (totalTaxable > 0) (netProfit / totalTaxable) * 100.0 else 0.0
                    Text(
                        text = "%.1f%%".format(marginPercent),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestEmerald
                    )
                }
            }
        }
    }
}
