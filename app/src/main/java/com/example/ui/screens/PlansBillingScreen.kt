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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.RazorpayPaymentService
import com.example.data.model.BillingCycle
import com.example.data.model.BillingReceipt
import com.example.data.model.CurrentSubscription
import com.example.data.model.SubscriptionTier
import com.example.ui.MainViewModel
import com.example.ui.theme.*

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
    var selectedBillingCycle by remember { mutableStateOf(BillingCycle.ANNUAL) }
    var showCancelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("screen_plans_billing"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card: Current Subscription
        CurrentPlanStatusCard(
            sub = currentSubscription,
            invoicesCount = invoicesCount,
            productsCount = productsCount,
            onManageAutoRenew = {
                onToggleAutoRenew()
                Toast.makeText(
                    context,
                    if (currentSubscription.autoRenew) "Auto-renew disabled" else "Auto-renew enabled",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onCancelRequest = { showCancelDialog = true }
        )

        // Billing Cycle Toggle (Monthly vs Annual - Save 25%)
        Surface(
            color = SurfaceWhite,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Billing Frequency",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pay annually to get 2 months free",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceSubtle)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { selectedBillingCycle = BillingCycle.MONTHLY },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedBillingCycle == BillingCycle.MONTHLY) DarkInk else Color.Transparent
                    ) {
                        Text(
                            text = "Monthly",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedBillingCycle == BillingCycle.MONTHLY) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        onClick = { selectedBillingCycle = BillingCycle.ANNUAL },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedBillingCycle == BillingCycle.ANNUAL) GrowthEngineGold else Color.Transparent
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Annual",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedBillingCycle == BillingCycle.ANNUAL) Color(0xFF141414) else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = if (selectedBillingCycle == BillingCycle.ANNUAL) DarkInk else SuccessGreenDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "-25%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tier Pricing Cards
        Text(
            text = "GrowthEngine Subscription Plans",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        SubscriptionTier.entries.forEach { tier ->
            val isCurrentTier = currentSubscription.tier == tier
            TierPricingCard(
                tier = tier,
                cycle = selectedBillingCycle,
                isCurrentTier = isCurrentTier,
                onSelectTier = {
                    if (isCurrentTier) {
                        Toast.makeText(context, "You are currently on the ${tier.title} plan", Toast.LENGTH_SHORT).show()
                    } else {
                        onOpenCheckout(tier, selectedBillingCycle)
                    }
                }
            )
        }

        // Payment Security Guarantee Card
        Surface(
            color = SurfaceWhite,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SuccessGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = SuccessGreenDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "100% Secure Payments via Razorpay",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "UPI, RuPay, Visa, Mastercard, NetBanking & Corporate EMI accepted. Tax invoices generated with full ITC credit.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Invoices / Receipts History Section
        if (billingHistory.isNotEmpty()) {
            Text(
                text = "Billing & Tax Invoices",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            billingHistory.forEach { receipt ->
                Surface(
                    onClick = { onViewReceipt(receipt) },
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Surface(
                                    color = SuccessGreenContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = receipt.status.name,
                                        color = SuccessGreenDark,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${receipt.tier.title} (${receipt.cycle.name.lowercase().replaceFirstChar { it.uppercase() }}) • ${MainViewModel.formatDate(receipt.dateEpoch)}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${MainViewModel.formatCurrencyPlain(receipt.totalPaid)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
                            )
                            Text(
                                text = "incl. 18% GST",
                                fontSize = 9.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Subscription Renewal?") },
            text = {
                Text(
                    "Your plan will remain active until ${MainViewModel.formatDate(currentSubscription.renewalDateEpoch)}. After that, your account will move to Free Starter mode."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleAutoRenew()
                        showCancelDialog = false
                        Toast.makeText(context, "Auto-renewal cancelled", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Plan")
                }
            }
        )
    }
}

@Composable
fun CurrentPlanStatusCard(
    sub: CurrentSubscription,
    invoicesCount: Int,
    productsCount: Int,
    onManageAutoRenew: () -> Unit,
    onCancelRequest: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkInk),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GrowthEngineGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = GrowthEngineGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CURRENT ACTIVE PLAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthEngineGold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = sub.tier.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = if (sub.isActive) SuccessGreenDark.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (sub.isActive) SuccessGreenDark else Color.Gray)
                ) {
                    Text(
                        text = if (sub.isActive) "ACTIVE" else "EXPIRED",
                        color = if (sub.isActive) Color(0xFF66BB6A) else Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resource Usage Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Invoices Created", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("$invoicesCount / Unlimited", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (invoicesCount / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GrowthEngineGold,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Products", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("$productsCount / Unlimited", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (productsCount / 200f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SuccessGreenDark,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Renews on: ${MainViewModel.formatDate(sub.renewalDateEpoch)}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "Payment: ${sub.paymentMethod}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onManageAutoRenew,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (sub.autoRenew) "Disable Autopay" else "Enable Autopay",
                            color = GrowthEngineGold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TierPricingCard(
    tier: SubscriptionTier,
    cycle: BillingCycle,
    isCurrentTier: Boolean,
    onSelectTier: () -> Unit
) {
    val price = if (cycle == BillingCycle.ANNUAL) tier.annualPricePerMonth else tier.monthlyPrice
    val isFree = tier.monthlyPrice == 0.0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(
            if (isCurrentTier) 2.dp else 1.dp,
            if (isCurrentTier) GrowthEngineGold else BorderSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tier_card_${tier.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tier.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        tier.badge?.let { badgeText ->
                            Surface(
                                color = GrowthEngineGoldContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthEngineGoldDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = tier.tagline,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (isFree) {
                        Text(
                            text = "FREE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Forever",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${price.toInt()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
                            )
                            Text(
                                text = "/mo",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        if (cycle == BillingCycle.ANNUAL) {
                            Text(
                                text = "billed ₹${(price * 12).toInt()}/yr",
                                fontSize = 9.sp,
                                color = SuccessGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(10.dp))

            // Tier Features List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tier.getFeaturesList().forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isCurrentTier) GrowthEngineGoldDark else SuccessGreenDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSelectTier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentTier) SurfaceSubtle else if (tier.isPopular) GrowthEngineGold else DarkInk
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_select_tier_${tier.id}")
            ) {
                Text(
                    text = if (isCurrentTier) "Current Plan" else if (isFree) "Downgrade to Starter" else "Upgrade to ${tier.title}",
                    color = if (isCurrentTier) TextSecondary else if (tier.isPopular) Color(0xFF141414) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
