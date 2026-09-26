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
import com.example.ui.theme.*

import com.example.data.model.CurrentSubscription
import com.example.data.model.SubscriptionTier

@Composable
fun SettingsScreen(
    currentSubscription: CurrentSubscription? = null,
    onNavigateToBilling: () -> Unit = {}
) {
    val context = LocalContext.current
    var companyName by remember { mutableStateOf("Kalyan Industrial Works") }
    var gstin by remember { mutableStateOf("27AABCK4829K1Z5") }
    var pan by remember { mutableStateOf("AABCK4829K") }
    var udyamNo by remember { mutableStateOf("UDYAM-MH-26-0049281") }
    var bankAccount by remember { mutableStateOf("38492049102") }
    var ifsc by remember { mutableStateOf("SBIN0004521") }
    var upiId by remember { mutableStateOf("kalyanworks@sbi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Plans & Billing Summary Section in Settings
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GrowthEngineGoldContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
            modifier = Modifier.fillMaxWidth().testTag("settings_plans_billing_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                .size(28.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(DarkInk),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GrowthEngineGoldLight, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(
                                text = "PLANS & BILLING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = currentSubscription?.tier?.title ?: "Growth Pro",
                                fontSize = 16.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Surface(
                        color = SuccessGreenContainer,
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = SuccessGreenDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Razorpay Auto-renew active · Unlimited GST Invoices · Supabase Sync",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onNavigateToBilling,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_manage_plans_settings")
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manage Plans, Billing & Invoices →", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        // Business Profile Header Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENTERPRISE PROFILE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImperialNavy,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = RoyalTeakGoldContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "UDYAM VERIFIED",
                            color = RoyalTeakGoldDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Trade & Legal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = { Text("GSTIN") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pan,
                        onValueChange = { pan = it },
                        label = { Text("PAN") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = udyamNo,
                    onValueChange = { udyamNo = it },
                    label = { Text("Udyam Registration Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Bank Settlement & UPI VPA Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "BANKING & UPI RECONCILIATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ImperialNavy,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Settlement coordinates printed on tax invoices & QR codes",
                    fontSize = 10.sp,
                    color = TextSecondaryMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bankAccount,
                    onValueChange = { bankAccount = it },
                    label = { Text("Current A/C Number (State Bank of India)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ifsc,
                        onValueChange = { ifsc = it },
                        label = { Text("IFSC Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI VPA") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // MSMED Act Compliance Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = ImperialNavy, modifier = Modifier.size(20.dp))
                    Text(
                        text = "MSMED Act Section 15 & 16 Enforcement",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImperialNavy
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "GrowthEngine monitors your 45-day debtor payment limit under the MSME Samadhaan guidelines. Automated compound interest alerts at 3x the RBI repo rate are calculated for receivables exceeding agreed credit terms.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = TextPrimaryDark
                )
            }
        }

        Button(
            onClick = {
                Toast.makeText(context, "Enterprise profile updated successfully", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_save_settings")
        ) {
            Text("Save Business Settings", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Secure Private Admin App Gateway (Administrators Only)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth().testTag("card_admin_app_gateway")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = android.content.Intent(context, com.example.admin.AdminActivity::class.java)
                        context.startActivity(intent)
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ImperialNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = GrowthEngineGold, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = "GrowthEngine Admin Console",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkInk
                        )
                        Text(
                            text = "Private App for prajindezaa142@gmail.com",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
