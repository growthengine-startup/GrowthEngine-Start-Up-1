package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavTab
import com.example.ui.theme.*

@Composable
fun GrowthEngineLogo(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        // Gold squircle with cursive 'g'
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(GrowthEngineGold)
                .border(0.5.dp, GrowthEngineGoldDark, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "g",
                color = Color(0xFF141414),
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // GrowthEngine Typography
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Growth",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = "Engine",
                color = GrowthEngineGold,
                fontSize = 17.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    businessName: String,
    businessRegion: String = "Maharashtra (27)",
    planTier: com.example.data.model.SubscriptionTier = com.example.data.model.SubscriptionTier.GROWTH_PRO,
    onOpenDrawer: () -> Unit,
    onOpenCopilot: () -> Unit,
    onOpenSupabase: () -> Unit,
    onOpenBilling: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    var showProfileMenu by remember { mutableStateOf(false) }

    Surface(
        color = BackgroundWhite,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger Menu & Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("topbar_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = ImperialNavy
                    )
                }

                GrowthEngineLogo(onClick = onOpenDrawer)
            }

            // Right: Cloud Sync, Plan Pill & Interactive Profile Avatar Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                // Cloud Sync Quick Indicator
                Surface(
                    onClick = onOpenSupabase,
                    shape = RoundedCornerShape(14.dp),
                    color = ForestGreen.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.testTag("topbar_cloud_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        )
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Synced",
                            tint = ForestGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Subscription Plan Pill
                Surface(
                    onClick = onOpenBilling,
                    shape = RoundedCornerShape(14.dp),
                    color = GrowthEngineGoldContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
                    modifier = Modifier.testTag("topbar_plan_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = GrowthEngineGoldDark,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = when (planTier) {
                                com.example.data.model.SubscriptionTier.STARTER_FREE -> "FREE"
                                com.example.data.model.SubscriptionTier.GROWTH_PRO -> "PRO"
                                com.example.data.model.SubscriptionTier.ENTERPRISE_MUNIM -> "ENTERPRISE"
                            },
                            color = DarkInk,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Profile Avatar with Dropdown Menu
                Box {
                    Surface(
                        onClick = { showProfileMenu = !showProfileMenu },
                        shape = CircleShape,
                        color = ImperialNavy,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGold),
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("topbar_profile_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val initial = businessName.trim().firstOrNull()?.uppercase() ?: "G"
                            Text(
                                text = initial,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Profile Dropdown Menu
                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false },
                        modifier = Modifier
                            .widthIn(min = 230.dp, max = 280.dp)
                            .background(BackgroundWhite)
                    ) {
                        // Header Item
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(WarmIvorySurface)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = businessName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DarkInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = businessRegion,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = ForestGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Active Subscription",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = BorderSubtle)

                        DropdownMenuItem(
                            text = { Text("Business Profile & Settings", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = ImperialNavy, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showProfileMenu = false
                                onOpenSettings()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Plans & Billing", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = GrowthEngineGoldDark, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showProfileMenu = false
                                onOpenBilling()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Cloud Backup & Sync", fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showProfileMenu = false
                                onOpenSupabase()
                            }
                        )

                        HorizontalDivider(color = BorderSubtle)

                        DropdownMenuItem(
                            text = { Text("Sign Out / Switch Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TerracottaRed) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = TerracottaRed, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showProfileMenu = false
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
fun AppBottomNav(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit,
    onOpenDrawer: () -> Unit
) {
    Surface(
        color = BackgroundWhite,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(AppNavTab.DASHBOARD, "Home", Icons.Default.Dashboard),
                Triple(AppNavTab.INVOICING, "Bill", Icons.AutoMirrored.Filled.ReceiptLong),
                Triple(AppNavTab.POS, "POS", Icons.Default.PointOfSale),
                Triple(AppNavTab.COPILOT, "Copilot", Icons.Default.AutoAwesome)
            )

            tabs.forEach { (tab, label, icon) ->
                val isSelected = currentTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) GrowthEngineGoldDark else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) GrowthEngineGoldDark else TextSecondary
                    )
                }
            }

            // Menu Drawer trigger
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onOpenDrawer() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("nav_tab_menu")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Menu",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }
    }
}
