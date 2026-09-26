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
import androidx.compose.ui.text.font.FontFamily
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

    val cogs = totalTaxable * 0.58
    val grossProfit = totalTaxable - cogs
    val netProfit = grossProfit - totalExpenses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TAX & COMPLIANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GrowthEngineGoldDark,
                letterSpacing = 1.sp
            )
            Text(
                text = "GST & Financial Reports",
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CA-ready GSTR-1, GSTR-3B tax calculations, Input Tax Credit (ITC) audits, and profit & loss summaries.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }

        // GSTR-1 Outward Supply Summary Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
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
                            text = "GSTR-1 TAX RECONCILIATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Monthly Return of Outward Supplies",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        color = SuccessGreenContainer,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, SuccessGreen)
                    ) {
                        Text(
                            text = "GSTR-1 READY",
                            color = SuccessGreenDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Invoiced Sales:", fontSize = 11.sp, color = TextSecondary)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(totalSales)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Aggregate Taxable Value:", fontSize = 11.sp, color = TextSecondary)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(totalTaxable)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = SurfaceSubtle,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Central GST (CGST 9%):", fontSize = 11.sp, color = TextSecondary)
                            Text("₹ ${MainViewModel.formatCurrencyPlain(totalCgst)}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("State GST (SGST 9%):", fontSize = 11.sp, color = TextSecondary)
                            Text("₹ ${MainViewModel.formatCurrencyPlain(totalSgst)}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Integrated GST (IGST 18%):", fontSize = 11.sp, color = TextSecondary)
                            Text("₹ ${MainViewModel.formatCurrencyPlain(totalIgst)}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                        }
                        HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Output Tax Liability:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("₹ ${MainViewModel.formatCurrencyPlain(totalOutputTax)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                        }
                    }
                }
            }
        }

        // GSTR-3B Liability & ITC Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GSTR-3B SET-OFF & INPUT TAX CREDIT (ITC)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Automated tax credit set-off against B2B outward liability",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gross Output Tax Payable:", fontSize = 11.sp, color = TextSecondary)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(totalOutputTax)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Eligible Input Tax Credit (ITC):", fontSize = 11.sp, color = SuccessGreenDark)
                    Text("(-) ₹ ${MainViewModel.formatCurrencyPlain(totalItc)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreenDark)
                }

                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Cash GST Payable to Govt:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(netGstPayable)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                }
            }
        }

        // Profit & Loss Summary Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GrowthEngineGoldContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EXECUTIVE PROFIT & LOSS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthEngineGoldDark,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gross Revenue:", fontSize = 11.sp, color = TextSecondary)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(totalTaxable)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated COGS (58%):", fontSize = 11.sp, color = TextSecondary)
                    Text("(-) ₹ ${MainViewModel.formatCurrencyPlain(cogs)}", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Operating Expenses:", fontSize = 11.sp, color = TextSecondary)
                    Text("(-) ₹ ${MainViewModel.formatCurrencyPlain(totalExpenses)}", fontSize = 12.sp, color = TextSecondary)
                }

                HorizontalDivider(color = GrowthEngineGoldBorder, modifier = Modifier.padding(vertical = 6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Operating Earnings:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                    Text("₹ ${MainViewModel.formatCurrencyPlain(netProfit)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
