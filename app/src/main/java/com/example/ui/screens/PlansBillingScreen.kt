package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.RazorpayPaymentService
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlansBillingScreen(
    currentSubscription: CurrentSubscription,
    billingHistory: List<BillingReceipt>,
    invoicesCount: Int,
    productsCount: Int,
    razorpayService: RazorpayPaymentService,
    onOpenCheckout: (SubscriptionTier, BillingCycle) -> Unit,
    onToggleAutoRenew: () -> Unit,
    onViewReceipt: (BillingReceipt) -> Unit
) {
    val context = LocalContext.current
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var selectedCycle by remember { mutableStateOf(currentSubscription.cycle) }

    // Razorpay developer settings state
    var isTestMode by remember { mutableStateOf(razorpayService.isTestMode) }
    var rzpKeyId by remember { mutableStateOf(razorpayService.keyId) }
    var rzpKeySecret by remember { mutableStateOf(razorpayService.keySecret) }
    var rzpMerchantName by remember { mutableStateOf(razorpayService.merchantName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("plans_billing_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLANS & BILLING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthEngineGoldDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Subscription & Payments",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Subtle status chip
                Surface(
                    color = SuccessGreenContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SuccessGreenDark)
                        )
                        Text(
                            text = "ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage your GrowthEngine subscription tier, Razorpay gateway credentials, usage quotas, and GST tax invoices.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }

        // 1. Current Subscription Summary Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
            modifier = Modifier.fillMaxWidth().testTag("current_subscription_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GrowthEngineGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = DarkInk, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = currentSubscription.tier.title,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (currentSubscription.cycle == BillingCycle.ANNUAL) "Annual billing (25% saved)" else "Monthly billing",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Surface(
                        color = DarkInk,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (currentSubscription.tier == SubscriptionTier.STARTER_FREE) "FREE"
                            else if (currentSubscription.cycle == BillingCycle.ANNUAL) "${currencyFormatter.format(currentSubscription.tier.annualPricePerMonth)}/mo"
                            else "${currencyFormatter.format(currentSubscription.tier.monthlyPrice)}/mo",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderLight)

                // Status breakdown rows
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Renewal Date:", fontSize = 12.sp, color = TextSecondary)
                    Text(dateFormatter.format(Date(currentSubscription.renewalDateEpoch)), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment Method:", fontSize = 12.sp, color = TextSecondary)
                    Text(currentSubscription.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subscription ID:", fontSize = 12.sp, color = TextSecondary)
                    Text(currentSubscription.razorpaySubscriptionId, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                }

                // Auto-renew toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundWhite, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-Renewal Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(
                            text = if (currentSubscription.autoRenew) "Active · Renews on ${dateFormatter.format(Date(currentSubscription.renewalDateEpoch))}" else "Off · Expires at end of billing period",
                            fontSize = 10.sp,
                            color = if (currentSubscription.autoRenew) SuccessGreenDark else ErrorRed
                        )
                    }
                    Switch(
                        checked = currentSubscription.autoRenew,
                        onCheckedChange = { onToggleAutoRenew() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GrowthEngineGold,
                            checkedTrackColor = DarkInk
                        ),
                        modifier = Modifier.testTag("switch_auto_renew")
                    )
                }
            }
        }

        // 2. Resource Usage & Limits Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth().testTag("usage_limits_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "USAGE & RESOURCE LIMITS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                UsageBar(
                    label = "GST Invoices (This Month)",
                    used = invoicesCount,
                    limit = if (currentSubscription.tier == SubscriptionTier.STARTER_FREE) 50 else 10000,
                    isUnlimited = currentSubscription.tier != SubscriptionTier.STARTER_FREE
                )

                UsageBar(
                    label = "Products & Inventory SKUs",
                    used = productsCount,
                    limit = if (currentSubscription.tier == SubscriptionTier.STARTER_FREE) 100 else 10000,
                    isUnlimited = currentSubscription.tier == SubscriptionTier.ENTERPRISE_MUNIM
                )

                UsageBar(
                    label = "Staff Logins & Counter Seats",
                    used = 1,
                    limit = when (currentSubscription.tier) {
                        SubscriptionTier.STARTER_FREE -> 1
                        SubscriptionTier.GROWTH_PRO -> 3
                        SubscriptionTier.ENTERPRISE_MUNIM -> 25
                    },
                    isUnlimited = currentSubscription.tier == SubscriptionTier.ENTERPRISE_MUNIM
                )

                UsageBar(
                    label = "AI Copilot Munim Queries",
                    used = 38,
                    limit = if (currentSubscription.tier == SubscriptionTier.STARTER_FREE) 20 else 5000,
                    isUnlimited = currentSubscription.tier != SubscriptionTier.STARTER_FREE
                )
            }
        }

        // 3. Plan Upgrade / Downgrade Comparison
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVAILABLE TIERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )

                // Billing Cycle Toggle (Monthly vs Annual)
                Row(
                    modifier = Modifier
                        .background(SurfaceSubtle, RoundedCornerShape(20.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
                        .padding(2.dp)
                ) {
                    Surface(
                        onClick = { selectedCycle = BillingCycle.MONTHLY },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedCycle == BillingCycle.MONTHLY) DarkInk else Color.Transparent,
                        modifier = Modifier.testTag("tab_cycle_monthly")
                    ) {
                        Text(
                            text = "Monthly",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedCycle == BillingCycle.MONTHLY) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        onClick = { selectedCycle = BillingCycle.ANNUAL },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedCycle == BillingCycle.ANNUAL) GrowthEngineGold else Color.Transparent,
                        modifier = Modifier.testTag("tab_cycle_annual")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Annual",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCycle == BillingCycle.ANNUAL) DarkInk else TextSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedCycle == BillingCycle.ANNUAL) DarkInk else GrowthEngineGoldContainer
                            ) {
                                Text(
                                    text = "-25%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCycle == BillingCycle.ANNUAL) GrowthEngineGoldLight else DarkInk,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3 Subscription Cards
            SubscriptionTier.values().forEach { tier ->
                PlanCard(
                    tier = tier,
                    cycle = selectedCycle,
                    isCurrentPlan = currentSubscription.tier == tier,
                    currencyFormatter = currencyFormatter,
                    onSelect = { onOpenCheckout(tier, selectedCycle) }
                )
            }
        }

        // 4. Razorpay Gateway Architecture & Mode Toggle Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth().testTag("razorpay_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        Text(
                            text = "RAZORPAY GATEWAY INTEGRATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Mode pill
                    Surface(
                        color = if (isTestMode) Color(0xFFFEF3C7) else SuccessGreenContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isTestMode) "DEMO / TEST" else "PRODUCTION",
                            color = if (isTestMode) Color(0xFF92400E) else SuccessGreenDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Seamless architecture to switch between Razorpay Test Mode and Live Production Mode by updating credentials.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )

                // Test Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceSubtle, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Enable Demo / Test Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(if (isTestMode) "Simulates UPI & Card checkout without real debits" else "Production Live Gateway", fontSize = 10.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = isTestMode,
                        onCheckedChange = {
                            isTestMode = it
                            razorpayService.isTestMode = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = GrowthEngineGold, checkedTrackColor = DarkInk),
                        modifier = Modifier.testTag("switch_rzp_test_mode")
                    )
                }

                OutlinedTextField(
                    value = rzpKeyId,
                    onValueChange = { rzpKeyId = it },
                    label = { Text("Razorpay Key ID (rzp_test_... or rzp_live_...)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_rzp_key_id"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = rzpKeySecret,
                    onValueChange = { rzpKeySecret = it },
                    label = { Text("Razorpay Key Secret") },
                    modifier = Modifier.fillMaxWidth().testTag("input_rzp_key_secret"),
                    singleLine = true
                )

                Button(
                    onClick = {
                        razorpayService.isTestMode = isTestMode
                        razorpayService.keyId = rzpKeyId
                        razorpayService.keySecret = rzpKeySecret
                        razorpayService.merchantName = rzpMerchantName
                        Toast.makeText(context, "Razorpay configuration updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_razorpay_config")
                ) {
                    Text("Save Razorpay Credentials", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // 5. Payment History & Invoices List
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth().testTag("billing_history_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PAYMENT HISTORY & GST INVOICES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Download tax-compliant receipts with 18% GST input credit breakdown.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (billingHistory.isEmpty()) {
                    Text("No billing receipts yet.", fontSize = 12.sp, color = TextSecondary)
                } else {
                    billingHistory.forEachIndexed { index, receipt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceSubtle)
                                .clickable { onViewReceipt(receipt) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = receipt.receiptNumber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Surface(
                                        color = SuccessGreenContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "PAID",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreenDark,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${receipt.tier.title} (${receipt.cycle.name.lowercase()}) · ${dateFormatter.format(Date(receipt.dateEpoch))}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Rzp ID: ${receipt.razorpayPaymentId}",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextTertiary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormatter.format(receipt.totalPaid),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkInk
                                )
                                Text(
                                    text = "View Invoice →",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrowthEngineGoldDark
                                )
                            }
                        }

                        if (index < billingHistory.size - 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageBar(
    label: String,
    used: Int,
    limit: Int,
    isUnlimited: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = TextPrimary)
            Text(
                text = if (isUnlimited) "$used / Unlimited" else "$used / $limit",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isUnlimited) GrowthEngineGoldDark else DarkInk
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val fraction = if (isUnlimited) 0.15f else (used.toFloat() / limit.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isUnlimited) GrowthEngineGold else DarkInk,
            trackColor = BorderLight
        )
    }
}

@Composable
private fun PlanCard(
    tier: SubscriptionTier,
    cycle: BillingCycle,
    isCurrentPlan: Boolean,
    currencyFormatter: NumberFormat,
    onSelect: () -> Unit
) {
    val pricePerMonth = if (cycle == BillingCycle.ANNUAL) tier.annualPricePerMonth else tier.monthlyPrice
    val isGold = tier == SubscriptionTier.GROWTH_PRO

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isGold) GrowthEngineGoldContainer else SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(
            if (isGold || isCurrentPlan) 1.5.dp else 1.dp,
            if (isCurrentPlan) GrowthEngineGoldDark else if (isGold) GrowthEngineGoldBorder else BorderSubtle
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = tier.title,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = tier.tagline,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }

                tier.badge?.let { badgeText ->
                    Surface(
                        color = DarkInk,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthEngineGoldLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (tier.monthlyPrice == 0.0) "₹0" else currencyFormatter.format(pricePerMonth),
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = " / month",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 3.dp)
                )

                if (cycle == BillingCycle.ANNUAL && tier.monthlyPrice > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "billed ₹${tier.getAnnualBilledTotal().toInt()}/yr",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = BorderLight)

            // Features Checklist
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tier.getFeaturesList().forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isGold) GrowthEngineGoldDark else SuccessGreenDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(text = feat, fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Button
            if (isCurrentPlan) {
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreenDark),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Current Plan (Active)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGold) DarkInk else GrowthEngineGold
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_select_tier_${tier.id}")
                ) {
                    Text(
                        text = if (tier.monthlyPrice == 0.0) "Switch to Free" else "Upgrade with Razorpay",
                        color = if (isGold) Color.White else Color(0xFF141414),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
