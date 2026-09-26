package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    onAddExpenseClick: () -> Unit
) {
    val totalExpense = expenses.sumOf { it.amount }
    val totalClaimableGst = expenses.filter { it.isGstClaimable }.sumOf { it.gstAmount }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = GrowthEngineGold,
                contentColor = DarkInk,
                shape = RoundedCornerShape(12.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Log Expense") },
                text = { Text("Log Expense Voucher", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_expense")
            )
        },
        containerColor = BackgroundWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.testTag("expenses_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header summary
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
                        Column {
                            Text(
                                text = "OPERATING VOUCHERS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Expenses & Petty Cash",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            color = SuccessGreenContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("ITC Claimable", fontSize = 9.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalClaimableGst)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Operating Expenditure", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalExpense)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkInk
                                )
                            }
                            Text("${expenses.size} Vouchers", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        }
                    }
                }
            }

            // Expenses List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (expenses.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceWhite)
                                        .border(1.dp, BorderSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No Expenses Logged", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text("Record daily office rent, salaries, freight, electricity, and raw material purchase bills.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                items(expenses, key = { it.id }) { expense ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth().testTag("expense_card_${expense.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = expense.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Category: ${expense.category} • Via ${expense.paymentMode}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (!expense.vendorName.isNullOrBlank()) {
                                    Text(
                                        text = "Vendor: ${expense.vendorName}",
                                        fontSize = 10.sp,
                                        color = TextTertiary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${MainViewModel.formatCurrencyPlain(expense.amount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkInk
                                )
                                if (expense.isGstClaimable) {
                                    Surface(
                                        color = SuccessGreenContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ITC: ₹${MainViewModel.formatCurrencyPlain(expense.gstAmount)}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreenDark,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
