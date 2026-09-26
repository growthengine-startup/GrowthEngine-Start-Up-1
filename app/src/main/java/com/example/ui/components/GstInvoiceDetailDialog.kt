package com.example.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.InvoiceEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun GstInvoiceDetailDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("gst_invoice_dialog"),
            color = SurfaceWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GrowthEngineGoldContainer,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGold)
                        ) {
                            Text(
                                text = "TAX INVOICE [RULE 46]",
                                color = GrowthEngineGoldDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(status = invoice.paymentStatus)
                    }

                    Row {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.testTag("invoice_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Invoice",
                                tint = DarkInk
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("invoice_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

                // Invoice Document Body (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Supplier Letterhead
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkInk),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "GROWTHENGINE INDUSTRIAL WORKS",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Precision Machine Tools & Hydraulic Assemblies",
                                        color = GrowthEngineGold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Plot 88, Sector 10, PCMC Industrial Area, Bhosari MIDC, Pune - 411026",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "GSTIN: 27AABCK4829K1Z5",
                                    color = GrowthEngineGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "State: Maharashtra (27)",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Billed To & Invoice Metadata Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "BILLED TO (BUYER):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = invoice.partyName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "GSTIN: ${invoice.partyGstin}",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Place of Supply: ${invoice.partyState}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Contact: ${invoice.partyPhone}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Column(
                            modifier = Modifier.weight(0.9f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "INVOICE DETAILS:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = invoice.invoiceNumber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
                            )
                            Text(
                                text = "Date: ${MainViewModel.formatDate(invoice.dateEpoch)}",
                                fontSize = 10.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Due: ${MainViewModel.formatDate(invoice.dueDateEpoch)}",
                                fontSize = 10.sp,
                                color = if (invoice.paymentStatus == "OVERDUE") TerracottaRed else TextPrimary,
                                fontWeight = if (invoice.paymentStatus == "OVERDUE") FontWeight.Bold else FontWeight.Normal
                            )
                            if (!invoice.eWayBillNumber.isNullOrBlank()) {
                                Text(
                                    text = "e-Way Bill: ${invoice.eWayBillNumber}",
                                    fontSize = 10.sp,
                                    color = ForestEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Line Items Table
                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkInk.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Item Description",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1.8f)
                                )
                                Text(
                                    text = "Qty",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.5f)
                                )
                                Text(
                                    text = "Taxable",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = "Tax",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Items row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1.8f)) {
                                    Text(
                                        text = invoice.itemsSummary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "HSN/SAC: 8482 / 8412 • Rate: 18%",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = "${invoice.itemsCount}x",
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.5f)
                                )
                                Text(
                                    text = "₹${MainViewModel.formatCurrencyPlain(invoice.subtotal)}",
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = "₹${MainViewModel.formatCurrencyPlain(invoice.cgstAmount + invoice.sgstAmount + invoice.igstAmount)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkInk,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tax Summary Breakdown Card
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Taxable Amount:", fontSize = 11.sp, color = TextSecondary)
                                Text("₹${MainViewModel.formatCurrencyPlain(invoice.subtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            if (!invoice.isInterState) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("CGST (9.0%):", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${MainViewModel.formatCurrencyPlain(invoice.cgstAmount)}", fontSize = 11.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("SGST (9.0%):", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${MainViewModel.formatCurrencyPlain(invoice.sgstAmount)}", fontSize = 11.sp)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("IGST (18.0% Inter-state):", fontSize = 11.sp, color = TextSecondary)
                                    Text("₹${MainViewModel.formatCurrencyPlain(invoice.igstAmount)}", fontSize = 11.sp)
                                }
                            }
                            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Invoice Value (INR):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(
                                    text = "₹${MainViewModel.formatCurrencyPlain(invoice.totalAmount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthEngineGoldDark
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount Received:", fontSize = 11.sp, color = ForestEmerald)
                                Text("₹${MainViewModel.formatCurrencyPlain(invoice.amountPaid)}", fontSize = 11.sp, color = ForestEmerald, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Balance Outstanding Due:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (invoice.balanceDue > 0) TerracottaRed else ForestEmerald)
                                Text(
                                    text = "₹${MainViewModel.formatCurrencyPlain(invoice.balanceDue)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (invoice.balanceDue > 0) TerracottaRed else ForestEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bank Details & UPI Payment QR Box
                    Surface(
                        color = GrowthEngineGoldContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = "BANK SETTLEMENT DETAILS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthEngineGoldDark
                                )
                                Text("Bank: HDFC Bank (Industrial Branch)", fontSize = 10.sp, color = TextPrimary)
                                Text("A/C No: 50200049102 • IFSC: HDFC0001245", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("UPI VPA: growthengine@hdfcbank", fontSize = 10.sp, color = GrowthEngineGoldDark, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color.White, RoundedCornerShape(6.dp))
                                    .border(1.dp, GrowthEngineGoldDark, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "UPI QR Code",
                                    tint = DarkInk,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Terms & Authorized Signatory Box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "TERMS & CONDITIONS:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text("1. Goods once sold will not be taken back.\n2. Interest @ 18% p.a. applicable post 30 days.\n3. Subject to jurisdiction only.", fontSize = 8.sp, color = TextSecondary)
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("For GrowthEngine Works", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                            Spacer(modifier = Modifier.height(28.dp))
                            Text("Authorized Signatory", fontSize = 9.sp, color = TextSecondary)
                        }
                    }
                }

                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))

                // Bottom Close button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("close_invoice_modal_btn")
                ) {
                    Text("Close Document View", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
