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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BillingReceipt
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscriptionReceiptDialog(
    receipt: BillingReceipt,
    businessName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 520.dp)
                .heightIn(max = 680.dp)
                .imePadding()
                .padding(vertical = 12.dp)
                .testTag("subscription_receipt_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with GrowthEngine logo and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GrowthEngineLogo()
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Title & Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TAX INVOICE / RECEIPT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthEngineGoldDark,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = receipt.receiptNumber,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        color = SuccessGreenContainer,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                    ) {
                        Text(
                            text = "PAID · SUCCESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderLight)

                // Seller & Customer Details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ISSUER:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("GrowthEngine Technologies Pvt Ltd", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("GSTIN: 33AABCG9102K1Z8", fontSize = 10.sp, color = TextSecondary)
                        Text("SAC Code: 998313 (Cloud Computing / SaaS)", fontSize = 10.sp, color = TextSecondary)
                        Text("Chennai, Tamil Nadu - 600001", fontSize = 10.sp, color = TextSecondary)
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("BILLED TO:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(businessName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Date: ${dateFormatter.format(Date(receipt.dateEpoch))}", fontSize = 10.sp, color = TextSecondary)
                        Text("Payment: ${receipt.paymentMode}", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                HorizontalDivider(color = BorderLight)

                // Line items
                Surface(
                    color = SurfaceSubtle,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Description", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Amount", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        HorizontalDivider(color = BorderLight)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(
                                    text = "GrowthEngine ${receipt.tier.title} (${receipt.cycle.name.lowercase().replaceFirstChar { it.uppercase() }})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text("Software as a Service (SaaS) Business ERP", fontSize = 9.sp, color = TextSecondary)
                            }
                            Text(currencyFormatter.format(receipt.baseAmount), fontSize = 11.sp, color = TextPrimary)
                        }
                        HorizontalDivider(color = BorderLight)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST (9.0%):", fontSize = 10.sp, color = TextSecondary)
                            Text(currencyFormatter.format(receipt.gstAmount / 2.0), fontSize = 10.sp, color = TextSecondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST (9.0%):", fontSize = 10.sp, color = TextSecondary)
                            Text(currencyFormatter.format(receipt.gstAmount / 2.0), fontSize = 10.sp, color = TextSecondary)
                        }
                        HorizontalDivider(color = BorderLight)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid (INR):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(currencyFormatter.format(receipt.totalPaid), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                        }
                    }
                }

                // Razorpay Transaction Reference Details
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                            Text("Razorpay Gateway Reference", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("Payment ID: ${receipt.razorpayPaymentId}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                        Text("Order ID: ${receipt.razorpayOrderId}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Invoice ${receipt.receiptNumber} downloaded to device", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).testTag("btn_download_receipt")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF141414), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", color = Color(0xFF141414), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Receipt shared via WhatsApp / Email", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).testTag("btn_share_receipt")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
