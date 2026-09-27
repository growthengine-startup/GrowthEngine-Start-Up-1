package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.util.InvoicePdfGenerator

@Composable
fun GstInvoiceDetailDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("gst_invoice_dialog"),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // ═══════════════════════════════════════════════
                // TOP ACTION BAR
                // ═══════════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GrowthEngineGoldContainer,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGold)
                        ) {
                            Text(
                                text = "TAX INVOICE",
                                color = GrowthEngineGoldDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(status = invoice.paymentStatus)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Share as Text
                        FilledTonalIconButton(
                            onClick = onShare,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFEEF2FF)
                            )
                        ) {
                            Icon(Icons.Default.Share, "Share Text", tint = Color(0xFF4F46E5), modifier = Modifier.size(18.dp))
                        }

                        // Share as PDF
                        FilledTonalIconButton(
                            onClick = {
                                try {
                                    InvoicePdfGenerator.shareInvoicePdf(context, invoice)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("invoice_pdf_button"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFFEF2F2)
                            )
                        ) {
                            Icon(Icons.Default.PictureAsPdf, "Share PDF", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        }

                        // Close
                        FilledTonalIconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp).testTag("invoice_close_button"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFF1F5F9)
                            )
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // ═══════════════════════════════════════════════
                // SCROLLABLE INVOICE BODY
                // ═══════════════════════════════════════════════
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // ── Company Letterhead ──
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkInk),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Gold accent stripe at top
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(GrowthEngineGold, GrowthEngineGoldLight, GrowthEngineGold)
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Logo placeholder
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(GrowthEngineGold),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "G",
                                                    color = DarkInk,
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Serif
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "GROWTHENGINE",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Industrial Works & Precision Manufacturing",
                                            color = GrowthEngineGoldLight,
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Plot 88, PCMC Industrial Area, Bhosari MIDC, Pune - 411026",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 9.sp
                                        )
                                    }

                                    // Invoice badge
                                    Surface(
                                        color = GrowthEngineGold,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "RULE 46",
                                            color = DarkInk,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "GSTIN: 27AABCK4829K1Z5",
                                        color = GrowthEngineGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "State: Maharashtra (27)",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Billed To & Invoice Details ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Billed To
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9EF)),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "BILLED TO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthEngineGoldDark,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = invoice.partyName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "GSTIN: ${invoice.partyGstin.ifBlank { "Unregistered" }}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "State: ${invoice.partyState}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                if (invoice.partyPhone.isNotBlank()) {
                                    Text(
                                        text = "Phone: ${invoice.partyPhone}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Invoice Details
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "INVOICE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthEngineGoldDark,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = invoice.invoiceNumber,
                                    fontSize = 14.sp,
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
                                    color = if (invoice.paymentStatus == "OVERDUE") ErrorRed else TextPrimary,
                                    fontWeight = if (invoice.paymentStatus == "OVERDUE") FontWeight.Bold else FontWeight.Normal
                                )
                                if (!invoice.eWayBillNumber.isNullOrBlank()) {
                                    Text(
                                        text = "e-Way: ${invoice.eWayBillNumber}",
                                        fontSize = 9.sp,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Items Table ──
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkInk)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DESCRIPTION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1.8f))
                                Text("QTY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                Text("TAXABLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
                                Text("TAX", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Item Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
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
                                        text = "HSN: 8482 • ${if (invoice.isInterState) "IGST 18%" else "CGST 9% + SGST 9%"}",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text("${invoice.itemsCount}", fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                Text("₹${MainViewModel.formatCurrencyPlain(invoice.subtotal)}", fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(invoice.cgstAmount + invoice.sgstAmount + invoice.igstAmount)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkInk,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Tax & Total Breakdown ──
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            TaxRow("Taxable Amount", "₹${MainViewModel.formatCurrencyPlain(invoice.subtotal)}")

                            if (!invoice.isInterState) {
                                TaxRow("CGST (9%)", "₹${MainViewModel.formatCurrencyPlain(invoice.cgstAmount)}")
                                TaxRow("SGST (9%)", "₹${MainViewModel.formatCurrencyPlain(invoice.sgstAmount)}")
                            } else {
                                TaxRow("IGST (18%)", "₹${MainViewModel.formatCurrencyPlain(invoice.igstAmount)}")
                            }

                            if (invoice.discount > 0) {
                                TaxRow("Discount", "-₹${MainViewModel.formatCurrencyPlain(invoice.discount)}", valueColor = SuccessGreen)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = BorderSubtle)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Total
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(GrowthEngineGoldContainer, Color(0xFFFFF8EB))
                                        )
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "TOTAL (INR)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(invoice.totalAmount)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthEngineGoldDark
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TaxRow("Amount Received", "₹${MainViewModel.formatCurrencyPlain(invoice.amountPaid)}", valueColor = SuccessGreen)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Balance Due",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (invoice.balanceDue > 0) ErrorRed else SuccessGreen
                                )
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(invoice.balanceDue)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (invoice.balanceDue > 0) ErrorRed else SuccessGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Bank Details ──
                    Surface(
                        color = GrowthEngineGoldContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "BANK DETAILS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthEngineGoldDark,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("HDFC Bank (Industrial Branch)", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("A/C: 50200049102 | IFSC: HDFC0001245", fontSize = 10.sp, color = TextPrimary)
                                Text("UPI: growthengine@hdfcbank", fontSize = 10.sp, color = GrowthEngineGoldDark, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, GrowthEngineGold, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.QrCode2, "UPI QR", tint = DarkInk, modifier = Modifier.size(40.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Terms & Signatory ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                "TERMS & CONDITIONS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "1. Goods once sold will not be taken back.\n2. Interest @ 18% p.a. after 30 days.\n3. Subject to local jurisdiction only.",
                                fontSize = 8.sp,
                                color = TextTertiary,
                                lineHeight = 12.sp
                            )
                        }

                        Column(
                            modifier = Modifier.weight(0.8f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("For GrowthEngine Works", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                            Spacer(modifier = Modifier.height(30.dp))
                            HorizontalDivider(modifier = Modifier.width(100.dp))
                            Text("Authorized Signatory", fontSize = 8.sp, color = TextSecondary)
                        }
                    }

                    // ── Notes ──
                    if (!invoice.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Default.StickyNote2, null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(invoice.notes!!, fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                // ═══════════════════════════════════════════════
                // BOTTOM ACTION BAR
                // ═══════════════════════════════════════════════
                Surface(
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Share PDF Button
                        Button(
                            onClick = {
                                try {
                                    InvoicePdfGenerator.shareInvoicePdf(context, invoice)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "PDF error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrowthEngineGold,
                                contentColor = DarkInk
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("invoice_share_pdf_btn"),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Close Button
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("close_invoice_modal_btn")
                        ) {
                            Text("Close", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaxRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
