package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GrowthEngineLogo
import com.example.ui.theme.*

@Composable
fun LandingScreen(
    onStartFreeClick: () -> Unit,
    onSignInClick: () -> Unit,
    onDirectDashboardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .testTag("landing_screen")
    ) {
        // Landing Sticky Topbar (Matching Screenshot 4)
        Surface(
            color = BackgroundWhite,
            tonalElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GrowthEngineLogo(onClick = onDirectDashboardClick)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = onSignInClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("landing_signin_btn")
                    ) {
                        Text(
                            text = "Sign in",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onStartFreeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("landing_start_free_btn")
                    ) {
                        Text(
                            text = "Start free",
                            color = Color(0xFF141414),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Scrollable Landing Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Tag Pill (Matching Screenshot 4)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfaceSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(GrowthEngineGold)
                    )
                    Text(
                        text = "BUSINESS OPERATING SYSTEM FOR INDIAN MSMES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Headline (Matching Screenshot 4)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Run your entire business from ",
                    fontSize = 34.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary,
                    lineHeight = 42.sp
                )
                Text(
                    text = "one trusted ledger.",
                    fontSize = 34.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = GrowthEngineGoldDark,
                    lineHeight = 42.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "GST billing, stock, udhaar and collections, purchases and production — with an AI copilot that reads your numbers and tells you what to do next.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // CTA Button (Matching Screenshot 4)
                Button(
                    onClick = onStartFreeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("landing_setup_business_btn")
                ) {
                    Text(
                        text = "Set up your business — free",
                        color = Color(0xFF141414),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Continue with Google Secondary Button
                OutlinedButton(
                    onClick = onSignInClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = BackgroundWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("landing_google_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        com.example.ui.components.GoogleLogoIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color(0xFF3C4043),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreenDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "No card required · Instant setup in 2 minutes",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // 3 Stat Badges (Matching Screenshot 5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceSubtle, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("2 min", fontSize = 18.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("to raise a GST\ninvoice", fontSize = 10.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("18", fontSize = 18.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("modules, one\nledger", fontSize = 10.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("UPI", fontSize = 18.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("collections\nbuilt in", fontSize = 10.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }

            // Hero Card with Indian Business Illustration & Copilot Overlay (Matching Screenshot 5)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.executive_banner_1790339116166),
                        contentDescription = "Indian MSME Enterprise",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    // Copilot Overlay Floating Box (Matching Screenshot 5)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkInk,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldDark.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "COPILOT INSIGHT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldLight,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹3,42,800 is overdue from 6 retailers. Sharma Traders alone owes ₹1.1L for 48 days.",
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Send UPI payment reminders →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldLight
                            )
                        }
                    }
                }
            }

            // Everything Connected Header (Matching Screenshot 5)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "EVERYTHING, CONNECTED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "One system from counter to balance sheet",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary,
                    lineHeight = 30.sp
                )
            }

            // Feature List: Sell (Matching Screenshot 6)
            FeatureSection(
                sectionTitle = "Sell",
                items = listOf(
                    Triple("Billing & GST", "Create GST-compliant tax invoices with CGST, SGST and IGST", Icons.AutoMirrored.Filled.ReceiptLong),
                    Triple("POS", "Fast counter billing with barcode and UPI", Icons.Default.PointOfSale),
                    Triple("Quotations", "Send estimates and convert them to invoices", Icons.Default.EditNote),
                    Triple("Orders", "Track sales orders from booking to dispatch", Icons.AutoMirrored.Filled.Assignment),
                    Triple("Customers", "Parties, credit limits and ledgers", Icons.Default.People)
                )
            )

            // Feature List: Buy & Stock (Matching Screenshot 6)
            FeatureSection(
                sectionTitle = "Buy & Stock",
                items = listOf(
                    Triple("Purchases", "Purchase bills and input tax credit", Icons.Default.ShoppingBag),
                    Triple("Suppliers", "Vendors, payables and purchase history", Icons.Default.LocalShipping)
                )
            )

            // Copilot Showcase (Matching Screenshot 7 & 8)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "AI BUSINESS COPILOT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthEngineGoldDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Like a seasoned munim who never sleeps.",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The copilot understands your sales, stock, receivables and expenses. Ask in plain Hindi or English — it answers with numbers and a next step you can take in one tap.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated Copilot Chat Bubble (Matching Screenshot 7)
                    Surface(
                        color = BackgroundWhite,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // User Question
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Surface(
                                    color = SurfaceSubtle,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Which items should I reorder this week?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Munim Answer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GrowthEngineGoldContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = GrowthEngineGoldDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = "Reorder 3 items before Diwali demand: Cotton Kurta M (8 left, sells 4/day), Silk Dupatta (12 left), Gift Box Large (out in 5 days). Suggested PO to Arvind Textiles: ₹86,400.",
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // GST & Compliance Section (Matching Screenshot 8 & 9)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "GST & COMPLIANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Invoices your CA will thank you for.",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                val checklist = listOf(
                    "Automatic CGST + SGST or IGST based on place of supply",
                    "HSN / SAC codes and tax rates on every line item",
                    "GSTR-1 and GSTR-3B ready summaries",
                    "E-way bill and e-invoice ready invoice structure",
                    "UPI QR printed on every invoice for faster collection"
                )

                checklist.forEach { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = GrowthEngineGoldDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = point,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    HorizontalDivider(color = BorderLight)
                }
            }

            // Bottom CTA Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GrowthEngineGoldContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ready to elevate your enterprise?",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Join thousands of Indian businesses using GrowthEngine.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onStartFreeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Get Started Now — It's Free", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Footer (Matching Screenshot 9 & 10)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GrowthEngineLogo(onClick = onDirectDashboardClick)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "The business operating system for India's growing enterprises.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "© 2026 GrowthEngine. Made in India for Bharat's MSMEs.",
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun FeatureSection(
    sectionTitle: String,
    items: List<Triple<String, String, ImageVector>>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = sectionTitle,
            fontSize = 16.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = GrowthEngineGoldDark
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                items.forEachIndexed { index, (title, desc, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(color = BorderLight)
                    }
                }
            }
        }
    }
}
