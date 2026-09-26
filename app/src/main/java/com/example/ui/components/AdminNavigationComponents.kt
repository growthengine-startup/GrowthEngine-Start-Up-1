package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavTab
import com.example.ui.theme.*

@Composable
fun AdminTopBar(
    adminEmail: String = "prajindezaa142@gmail.com",
    onOpenDrawer: () -> Unit,
    onSwitchToBusinessView: () -> Unit,
    onOpenSupabase: () -> Unit,
    onSignOut: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = ImperialNavy,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger & Master Admin Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("admin_topbar_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Admin Navigation",
                        tint = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(GrowthEngineGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = ImperialNavy,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GrowthEngine",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = GrowthEngineGoldContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SUPER ADMIN",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GrowthEngineGoldDark,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "App Owner Console",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Right: Switcher to Business Mode & Profile Dropdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Switch to Business ERP button
                Surface(
                    onClick = onSwitchToBusinessView,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("admin_switch_to_business_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text("Business ERP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                // Admin Avatar Menu
                Box {
                    Surface(
                        onClick = { showMenu = !showMenu },
                        shape = CircleShape,
                        color = GrowthEngineGold,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("admin_avatar_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("P", color = ImperialNavy, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .widthIn(min = 250.dp)
                            .background(BackgroundWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarmIvorySurface)
                                .padding(12.dp)
                        ) {
                            Text("Prajin Dezaa", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkInk)
                            Text(adminEmail, fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = ForestGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "System Owner • All Permissions",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = BorderSubtle)

                        DropdownMenuItem(
                            text = { Text("Open Business ERP View", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = ImperialNavy) },
                            onClick = {
                                showMenu = false
                                onSwitchToBusinessView()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Supabase Cloud Database", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = ForestGreen) },
                            onClick = {
                                showMenu = false
                                onOpenSupabase()
                            }
                        )

                        HorizontalDivider(color = BorderSubtle)

                        DropdownMenuItem(
                            text = { Text("Sign Out", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TerracottaRed) },
                            leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = TerracottaRed) },
                            onClick = {
                                showMenu = false
                                onSignOut()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDrawerContent(
    adminEmail: String = "prajindezaa142@gmail.com",
    activeTab: String = "OVERVIEW",
    onSelectAdminSection: (String) -> Unit,
    onSwitchToBusinessView: () -> Unit,
    onCloseDrawer: () -> Unit,
    onSignOut: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = BackgroundWhite,
        drawerContentColor = TextPrimary,
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("admin_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = ImperialNavy,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = GrowthEngineGold, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text("Super Admin", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ImperialNavy)
                        Text("GrowthEngine Master Console", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                IconButton(onClick = onCloseDrawer) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Owner Identity Card
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("App Owner", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Surface(
                            color = ForestGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("SUPER ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ForestGreen, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(adminEmail, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch to Business View Action Card
            Surface(
                onClick = {
                    onCloseDrawer()
                    onSwitchToBusinessView()
                },
                shape = RoundedCornerShape(10.dp),
                color = GrowthEngineGoldContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = GrowthEngineGoldDark, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Switch to Business View", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                            Text("Manage Dezaa Enterprises ERP", fontSize = 11.sp, color = DarkInk.copy(alpha = 0.7f))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrowthEngineGoldDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Modules Navigation
            AdminSectionHeader("GLOBAL PLATFORM & ANALYTICS")
            AdminNavRow("Overview & KPIs", "Real-time metrics, MRR, GMV & active businesses", Icons.Default.Dashboard, activeTab == "OVERVIEW") {
                onSelectAdminSection("OVERVIEW"); onCloseDrawer()
            }
            AdminNavRow("Businesses & Tenants", "Manage accounts, plans, limits & suspensions", Icons.Default.Business, activeTab == "BUSINESSES") {
                onSelectAdminSection("BUSINESSES"); onCloseDrawer()
            }
            AdminNavRow("ERP Telemetry & Usage", "Feature adoption and activity analytics", Icons.Default.Analytics, activeTab == "FEATURE_USAGE") {
                onSelectAdminSection("FEATURE_USAGE"); onCloseDrawer()
            }

            Spacer(modifier = Modifier.height(12.dp))

            AdminSectionHeader("BILLING & MONETIZATION")
            AdminNavRow("Subscriptions", "Lifecycle states, renewals & accounts", Icons.Default.CardMembership, activeTab == "SUBSCRIPTIONS") {
                onSelectAdminSection("SUBSCRIPTIONS"); onCloseDrawer()
            }
            AdminNavRow("Razorpay & Payments", "Transaction history, refunds & webhook logs", Icons.Default.Payments, activeTab == "PAYMENTS") {
                onSelectAdminSection("PAYMENTS"); onCloseDrawer()
            }
            AdminNavRow("Plans & Pricing", "Configure tier tiers, features & limits", Icons.Default.Tune, activeTab == "PLANS") {
                onSelectAdminSection("PLANS"); onCloseDrawer()
            }

            Spacer(modifier = Modifier.height(12.dp))

            AdminSectionHeader("INTELLIGENCE & ENGAGEMENT")
            AdminNavRow("AI Usage & Quotas", "Per-tenant token telemetry, costs & throttling", Icons.Default.AutoAwesome, activeTab == "AI_METERING") {
                onSelectAdminSection("AI_METERING"); onCloseDrawer()
            }
            AdminNavRow("Broadcast Notifications", "Push announcements and billing reminders", Icons.Default.Campaign, activeTab == "NOTIFICATIONS") {
                onSelectAdminSection("NOTIFICATIONS"); onCloseDrawer()
            }

            Spacer(modifier = Modifier.height(12.dp))

            AdminSectionHeader("SECURITY & CONTROLS")
            AdminNavRow("Support & Audit Logs", "Customer tickets and security audit trail", Icons.Default.Security, activeTab == "SUPPORT_AUDIT") {
                onSelectAdminSection("SUPPORT_AUDIT"); onCloseDrawer()
            }
            AdminNavRow("System Settings & Hub", "Maintenance toggle, Supabase schema & controls", Icons.Default.Storage, activeTab == "SETTINGS") {
                onSelectAdminSection("SETTINGS"); onCloseDrawer()
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sign Out Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onCloseDrawer()
                        onSignOut()
                    }
                    .padding(vertical = 10.dp, horizontal = 8.dp)
                    .testTag("admin_drawer_sign_out_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = TerracottaRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sign Out Admin Session", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TerracottaRed)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdminSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = ImperialNavy,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
    )
}

@Composable
private fun AdminNavRow(
    title: String,
    description: String?,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) ImperialNavy.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ImperialNavy.copy(alpha = 0.3f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) ImperialNavy else TextSecondary,
                modifier = Modifier
                    .size(18.dp)
                    .offset(y = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) ImperialNavy else TextPrimary
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 13.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
