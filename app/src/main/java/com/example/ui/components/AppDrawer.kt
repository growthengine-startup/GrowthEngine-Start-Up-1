package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavTab
import com.example.ui.theme.*

data class DrawerItemData(
    val tab: AppNavTab,
    val title: String,
    val description: String?,
    val icon: ImageVector
)

@Composable
fun AppDrawerContent(
    currentTab: AppNavTab,
    businessName: String,
    businessRegion: String,
    planTier: com.example.data.model.SubscriptionTier = com.example.data.model.SubscriptionTier.GROWTH_PRO,
    onSelectTab: (AppNavTab) -> Unit,
    onCloseDrawer: () -> Unit,
    onSignOut: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = BackgroundWhite,
        drawerContentColor = TextPrimary,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .widthIn(min = 260.dp, max = 320.dp)
            .fillMaxHeight()
            .testTag("app_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header Logo & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GrowthEngineLogo()
                IconButton(onClick = onCloseDrawer) {
                    Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User / Business Card with Subscription Indicator
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelectTab(AppNavTab.PLANS_BILLING)
                        onCloseDrawer()
                    }
                    .testTag("drawer_business_profile_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = businessName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Surface(
                            color = GrowthEngineGoldContainer,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, GrowthEngineGoldBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GrowthEngineGoldDark, modifier = Modifier.size(11.dp))
                                Text(
                                    text = when (planTier) {
                                        com.example.data.model.SubscriptionTier.STARTER_FREE -> "STARTER"
                                        com.example.data.model.SubscriptionTier.GROWTH_PRO -> "PRO"
                                        com.example.data.model.SubscriptionTier.ENTERPRISE_MUNIM -> "ENTERPRISE"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkInk
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = businessRegion,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Plans & Billing",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GrowthEngineGoldDark
                        )
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = GrowthEngineGoldDark,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Group: OVERVIEW
            DrawerSectionHeader("OVERVIEW")
            DrawerNavRow(
                title = "Dashboard",
                description = null,
                icon = Icons.Default.Dashboard,
                isSelected = currentTab == AppNavTab.DASHBOARD,
                onClick = { onSelectTab(AppNavTab.DASHBOARD); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "AI Assistant",
                description = null,
                icon = Icons.Default.AutoAwesome,
                isSelected = currentTab == AppNavTab.COPILOT,
                onClick = { onSelectTab(AppNavTab.COPILOT); onCloseDrawer() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Group: SELL
            DrawerSectionHeader("SELL")
            DrawerNavRow(
                title = "Billing & GST",
                description = "Create GST-compliant tax invoices with CGST, SGST and IGST",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                isSelected = currentTab == AppNavTab.INVOICING,
                onClick = { onSelectTab(AppNavTab.INVOICING); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "POS",
                description = "Fast counter billing with barcode and UPI",
                icon = Icons.Default.PointOfSale,
                isSelected = currentTab == AppNavTab.POS,
                onClick = { onSelectTab(AppNavTab.POS); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Quotations",
                description = "Send estimates and convert them to invoices",
                icon = Icons.Default.EditNote,
                isSelected = currentTab == AppNavTab.QUOTATIONS,
                onClick = { onSelectTab(AppNavTab.QUOTATIONS); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Orders",
                description = "Track sales orders from booking to dispatch",
                icon = Icons.AutoMirrored.Filled.Assignment,
                isSelected = currentTab == AppNavTab.ORDERS,
                onClick = { onSelectTab(AppNavTab.ORDERS); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Customers",
                description = "Parties, credit limits and ledgers",
                icon = Icons.Default.People,
                isSelected = currentTab == AppNavTab.CUSTOMERS,
                onClick = { onSelectTab(AppNavTab.CUSTOMERS); onCloseDrawer() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Group: BUY & STOCK
            DrawerSectionHeader("BUY & STOCK")
            DrawerNavRow(
                title = "Purchases",
                description = "Purchase bills and input tax credit",
                icon = Icons.Default.ShoppingBag,
                isSelected = currentTab == AppNavTab.PURCHASES,
                onClick = { onSelectTab(AppNavTab.PURCHASES); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Suppliers",
                description = "Vendors, payables and purchase history",
                icon = Icons.Default.LocalShipping,
                isSelected = currentTab == AppNavTab.SUPPLIERS,
                onClick = { onSelectTab(AppNavTab.SUPPLIERS); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Inventory",
                description = "Stock levels, reorder points, low stock alerts",
                icon = Icons.Default.Inventory2,
                isSelected = currentTab == AppNavTab.INVENTORY,
                onClick = { onSelectTab(AppNavTab.INVENTORY); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Manufacturing",
                description = "Production runs, raw material tracking",
                icon = Icons.Default.PrecisionManufacturing,
                isSelected = currentTab == AppNavTab.MANUFACTURING,
                onClick = { onSelectTab(AppNavTab.MANUFACTURING); onCloseDrawer() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Group: MONEY
            DrawerSectionHeader("MONEY")
            DrawerNavRow(
                title = "Payments & Outstanding",
                description = "Receivables, payables, UPI collections and reminders",
                icon = Icons.Default.AccountBalanceWallet,
                isSelected = currentTab == AppNavTab.KHATA,
                onClick = { onSelectTab(AppNavTab.KHATA); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Expenses",
                description = "Rent, salaries, freight and daily expenses",
                icon = Icons.Default.Receipt,
                isSelected = currentTab == AppNavTab.EXPENSES,
                onClick = { onSelectTab(AppNavTab.EXPENSES); onCloseDrawer() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Group: INSIGHTS
            DrawerSectionHeader("INSIGHTS")
            DrawerNavRow(
                title = "Reports",
                description = "GSTR-1, GSTR-3B, P&L, stock and party reports",
                icon = Icons.Default.Assessment,
                isSelected = currentTab == AppNavTab.REPORTS,
                onClick = { onSelectTab(AppNavTab.REPORTS); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Analytics",
                description = "Trends, margins and customer insights",
                icon = Icons.Default.QueryStats,
                isSelected = currentTab == AppNavTab.ANALYTICS,
                onClick = { onSelectTab(AppNavTab.ANALYTICS); onCloseDrawer() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Group: ORGANISATION
            DrawerSectionHeader("ORGANISATION")
            DrawerNavRow(
                title = "Employees",
                description = "Staff, attendance, salary and access",
                icon = Icons.Default.Badge,
                isSelected = currentTab == AppNavTab.EMPLOYEES,
                onClick = { onSelectTab(AppNavTab.EMPLOYEES); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Automation",
                description = "Payment reminders, low stock alerts and scheduled reports",
                icon = Icons.Default.Tune,
                isSelected = currentTab == AppNavTab.AUTOMATION,
                onClick = { onSelectTab(AppNavTab.AUTOMATION); onCloseDrawer() }
            )
            // Group: ADMINISTRATION & CLOUD
            DrawerSectionHeader("ACCOUNT & CLOUD")
            DrawerNavRow(
                title = "Supabase Cloud Sync",
                description = "Real-time cloud database backup & sync",
                icon = Icons.Default.CloudSync,
                isSelected = currentTab == AppNavTab.SUPABASE_SYNC,
                onClick = { onSelectTab(AppNavTab.SUPABASE_SYNC); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Plans & Billing",
                description = "Razorpay subscription, quotas & invoices",
                icon = Icons.Default.CreditCard,
                isSelected = currentTab == AppNavTab.PLANS_BILLING,
                onClick = { onSelectTab(AppNavTab.PLANS_BILLING); onCloseDrawer() }
            )
            DrawerNavRow(
                title = "Settings",
                description = "Company profile, GSTIN & bank coordinates",
                icon = Icons.Default.Settings,
                isSelected = currentTab == AppNavTab.SETTINGS,
                onClick = { onSelectTab(AppNavTab.SETTINGS); onCloseDrawer() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Sign Out / Switch User Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onCloseDrawer()
                        onSignOut()
                    }
                    .padding(vertical = 10.dp, horizontal = 8.dp)
                    .testTag("drawer_sign_out_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Switch Business / Sign Out", fontSize = 13.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = GrowthEngineGoldDark,
        fontStyle = FontStyle.Italic,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
    )
}

@Composable
private fun DrawerNavRow(
    title: String,
    description: String?,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) GrowthEngineGoldContainer else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder) else null,
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
                tint = if (isSelected) GrowthEngineGoldDark else TextSecondary,
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
                    color = if (isSelected) GrowthEngineGoldDark else TextPrimary
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 13.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
