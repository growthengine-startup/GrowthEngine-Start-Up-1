package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
            FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = ImperialNavy,
                contentColor = RoyalTeakGoldLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_expense")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Log Expense")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Expense", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        containerColor = WarmIvoryBackground,
        modifier = Modifier.testTag("expenses_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header summary
            Surface(
                color = WarmIvorySurface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OPERATING EXPENDITURE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "₹ ${MainViewModel.formatCurrencyPlain(totalExpense)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = ForestEmeraldContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ITC CLAIMABLE: ₹${MainViewModel.formatCurrencyPlain(totalClaimableGst)}",
                                color = ForestEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "Deductible in GSTR-3B",
                            fontSize = 9.sp,
                            color = TextSecondaryMuted
                        )
                    }
                }
            }

            // Expenses List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(expenses) { expense ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
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
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "Category: ${expense.category} • Via ${expense.paymentMode}",
                                    fontSize = 10.sp,
                                    color = TextSecondaryMuted
                                )
                                if (!expense.vendorName.isNullOrBlank()) {
                                    Text(
                                        text = "Vendor: ${expense.vendorName}",
                                        fontSize = 10.sp,
                                        color = TextSecondaryMuted
                                    )
                                }
                                Text(
                                    text = "Date: ${MainViewModel.formatDate(expense.dateEpoch)}",
                                    fontSize = 9.sp,
                                    color = TextSecondaryMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹ ${MainViewModel.formatCurrencyPlain(expense.amount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ImperialNavy
                                )
                                if (expense.isGstClaimable) {
                                    Text(
                                        text = "+ ITC: ₹${MainViewModel.formatCurrencyPlain(expense.gstAmount)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForestEmerald
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
