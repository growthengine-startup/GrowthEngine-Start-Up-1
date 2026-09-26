package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavTab
import com.example.ui.theme.*

@Composable
fun ModuleChipBar(
    selectedTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit
) {
    val modules = listOf(
        Triple(AppNavTab.DASHBOARD, "Overview", Icons.Default.Dashboard),
        Triple(AppNavTab.COPILOT, "AI Copilot", Icons.Default.AutoAwesome),
        Triple(AppNavTab.INVOICING, "GST Invoicing", Icons.AutoMirrored.Filled.ReceiptLong),
        Triple(AppNavTab.POS, "Quick POS", Icons.Default.PointOfSale),
        Triple(AppNavTab.INVENTORY, "Inventory", Icons.Default.Inventory2),
        Triple(AppNavTab.KHATA, "Khata & B2B", Icons.Default.AccountBalanceWallet),
        Triple(AppNavTab.MANUFACTURING, "Production", Icons.Default.PrecisionManufacturing),
        Triple(AppNavTab.EXPENSES, "Expenses", Icons.Default.Receipt),
        Triple(AppNavTab.REPORTS, "Reports & Tax", Icons.Default.Assessment),
        Triple(AppNavTab.SETTINGS, "Firm Profile", Icons.Default.Business)
    )

    Surface(
        color = WarmIvorySurfaceVariant,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modules.forEach { (tab, label, icon) ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) ImperialNavy else TextSecondaryMuted
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalTeakGoldContainer,
                        selectedLabelColor = RoyalTeakGoldDark,
                        containerColor = WarmIvorySurface,
                        labelColor = TextPrimaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) RoyalTeakGold else WarmIvoryBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("module_chip_${tab.name.lowercase()}")
                )
            }
        }
    }
}
