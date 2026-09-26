package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.billing.RazorpayCheckoutResult
import com.example.billing.RazorpayPaymentService
import com.example.data.model.BillingCycle
import com.example.data.model.BillingReceipt
import com.example.data.model.SubscriptionTier
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@Composable
fun RazorpayCheckoutModal(
    tier: SubscriptionTier,
    cycle: BillingCycle,
    businessName: String,
    razorpayService: RazorpayPaymentService,
    onDismiss: () -> Unit,
    onPaymentSuccess: (BillingReceipt) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    val baseAmount = if (cycle == BillingCycle.ANNUAL) tier.getAnnualBilledTotal() else tier.monthlyPrice
    val gstAmount = (baseAmount * 18.0) / 100.0
    val totalAmount = baseAmount + gstAmount

    var selectedMethod by remember { mutableStateOf("UPI") } // UPI, CARD, NETBANKING
    var upiVpa by remember { mutableStateOf("dezaa@okhdfcbank") }
    var cardNumber by remember { mutableStateOf("4532 •••• •••• 9018") }
    var cardExpiry by remember { mutableStateOf("08/29") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 520.dp)
                .heightIn(max = 700.dp)
                .imePadding()
                .padding(vertical = 12.dp)
                .testTag("razorpay_checkout_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Razorpay Header & Branding
                Surface(
                    color = DarkInk,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Razorpay Logo Pill
                                Surface(
                                    color = Color(0xFF0C2340),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Razorpay",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = "Checkout Gateway",
                                    color = TextSecondaryMuted,
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                enabled = !isProcessing,
                                modifier = Modifier.size(28.dp).testTag("btn_close_checkout")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = tier.title,
                                    color = GrowthEngineGoldLight,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (cycle == BillingCycle.ANNUAL) "Annual Subscription (Billed Yearly)" else "Monthly Subscription",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormatter.format(totalAmount),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "incl. 18% GST",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Demo / Test Mode Banner
                if (razorpayService.isTestMode) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                            Text(
                                text = "TEST / DEMO MODE ACTIVE: Key [${razorpayService.keyId}]. No real money will be charged.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                // Body content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Price Breakdown
                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Base Subscription (${cycle.name.lowercase()}):", fontSize = 12.sp, color = TextSecondary)
                                Text(currencyFormatter.format(baseAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CGST (9%) + SGST (9%):", fontSize = 12.sp, color = TextSecondary)
                                Text(currencyFormatter.format(gstAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderLight)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount Payable:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(currencyFormatter.format(totalAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                            }
                            Text(
                                text = "• Input Tax Credit eligible with your GSTIN registration.",
                                fontSize = 10.sp,
                                color = GrowthEngineGoldDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Payment Method Tabs
                    Text(
                        text = "SELECT PAYMENT METHOD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceSubtle, RoundedCornerShape(8.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("UPI" to Icons.Default.QrCodeScanner, "CARD" to Icons.Default.CreditCard, "NETBANKING" to Icons.Default.AccountBalance).forEach { (mode, icon) ->
                            val isSelected = selectedMethod == mode
                            Surface(
                                onClick = { selectedMethod = mode },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) BackgroundWhite else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle) else null,
                                modifier = Modifier.weight(1f).testTag("tab_payment_$mode")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSelected) DarkInk else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = mode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DarkInk else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Mode Details
                    when (selectedMethod) {
                        "UPI" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = upiVpa,
                                    onValueChange = { upiVpa = it },
                                    label = { Text("UPI Virtual Payment Address (VPA)") },
                                    placeholder = { Text("e.g. yourname@okhdfcbank") },
                                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = GrowthEngineGoldDark) },
                                    modifier = Modifier.fillMaxWidth().testTag("input_upi_vpa"),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Google Pay", "PhonePe", "Paytm", "BHIM").forEach { appName ->
                                        Surface(
                                            onClick = { upiVpa = "${businessName.lowercase().replace(" ", "")}@${appName.lowercase().take(4)}" },
                                            shape = RoundedCornerShape(6.dp),
                                            color = SurfaceSubtle,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = appName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                                modifier = Modifier.padding(vertical = 6.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "CARD" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    label = { Text("Card Number (Visa / MasterCard / RuPay)") },
                                    modifier = Modifier.fillMaxWidth().testTag("input_card_number"),
                                    singleLine = true
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = { cardExpiry = it },
                                        label = { Text("Expiry (MM/YY)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = "•••",
                                        onValueChange = { },
                                        label = { Text("CVV") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                        "NETBANKING" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Popular Banks:", fontSize = 11.sp, color = TextSecondary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("HDFC Bank", "ICICI Bank", "SBI", "Axis Bank").forEach { bank ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SurfaceSubtle,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = bank,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Error Message
                    errorMessage?.let { err ->
                        Surface(
                            color = ErrorRedContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                Text(text = err, fontSize = 11.sp, color = ErrorRed)
                            }
                        }
                    }

                    // Processing Indicator
                    if (isProcessing) {
                        Surface(
                            color = SurfaceSubtle,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = GrowthEngineGoldDark
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Connecting to Razorpay & verifying signature...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                isProcessing = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val methodLabel = when (selectedMethod) {
                                        "UPI" -> "UPI ($upiVpa)"
                                        "CARD" -> "Debit Card ($cardNumber)"
                                        else -> "Netbanking (HDFC)"
                                    }
                                    when (val result = razorpayService.processPayment(tier, cycle, methodLabel, forceFailure = false)) {
                                        is RazorpayCheckoutResult.Success -> {
                                            isProcessing = false
                                            Toast.makeText(context, "Payment verified by Razorpay!", Toast.LENGTH_SHORT).show()
                                            onPaymentSuccess(result.receipt)
                                        }
                                        is RazorpayCheckoutResult.Failure -> {
                                            isProcessing = false
                                            errorMessage = "Payment Failed: ${result.description} (${result.reason})"
                                        }
                                        RazorpayCheckoutResult.Cancelled -> {
                                            isProcessing = false
                                            onDismiss()
                                        }
                                    }
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_pay_razorpay_success")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF141414), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pay ${currencyFormatter.format(totalAmount)} with Razorpay",
                                color = Color(0xFF141414),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Simulation helper button for testing failed payment flow
                        OutlinedButton(
                            onClick = {
                                isProcessing = true
                                errorMessage = null
                                coroutineScope.launch {
                                    when (val result = razorpayService.processPayment(tier, cycle, "UPI ($upiVpa)", forceFailure = true)) {
                                        is RazorpayCheckoutResult.Failure -> {
                                            isProcessing = false
                                            errorMessage = "Payment Failed: ${result.description} (Reason: ${result.reason})"
                                            Toast.makeText(context, "Simulated Payment Decline", Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {
                                            isProcessing = false
                                        }
                                    }
                                }
                            },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            modifier = Modifier.fillMaxWidth().testTag("btn_simulate_failure")
                        ) {
                            Text("Simulate Bank Failure / Decline", fontSize = 11.sp)
                        }
                    }

                    // Security Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = SuccessGreenDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PCI-DSS Level 1 Compliant · 256-Bit SSL Secured",
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
