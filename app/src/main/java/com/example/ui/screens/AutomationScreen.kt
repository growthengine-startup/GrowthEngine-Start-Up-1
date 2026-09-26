package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

data class AutomationRuleModel(
    val id: String,
    val title: String,
    val description: String,
    val triggerType: String,
    val channel: String,
    val isEnabled: Boolean,
    val lastTriggeredTime: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    onTriggerAllAutomations: () -> Unit
) {
    val context = LocalContext.current
    val rules = remember {
        mutableStateListOf(
            AutomationRuleModel(
                "rule_1",
                "Automated Overdue Payment Reminder (WhatsApp)",
                "Automatically draft and send personalized UPI payment reminders to customers 3 days before and 1 day after invoice due date.",
                "Invoice Overdue Trigger",
                "WhatsApp",
                true,
                "Today, 09:30 AM"
            ),
            AutomationRuleModel(
                "rule_2",
                "Low Inventory Re-order Alert",
                "Instantly trigger purchase PO draft when raw materials drop below minimum safe safety stock threshold.",
                "Stock Threshold < Reorder Level",
                "App Push + Email",
                true,
                "Yesterday, 04:15 PM"
            ),
            AutomationRuleModel(
                "rule_3",
                "Daily Evening Cashbook Summary (Munim Report)",
                "Compile daily sales, cash collections, pending khata, and expense balance sheet at 08:00 PM.",
                "Scheduled Daily Cron",
                "WhatsApp to Owner",
                true,
                "Yesterday, 08:00 PM"
            ),
            AutomationRuleModel(
                "rule_4",
                "Auto GSTR-1 JSON Reconciler",
                "Validate B2B GSTIN formats, HSN digits, and tax rates prior to month-end tax filing.",
                "Monthly 10th",
                "In-App Audit",
                true,
                "10 Sep 2026"
            ),
            AutomationRuleModel(
                "rule_5",
                "Instant POS Digital Receipt Dispatch",
                "Automatically send thermal receipt link and GST e-bill via SMS immediately upon counter POS checkout.",
                "POS Bill Completed",
                "SMS / WhatsApp",
                true,
                "2 hours ago"
            )
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    onTriggerAllAutomations()
                    Toast.makeText(context, "All active automation routines executed successfully.", Toast.LENGTH_SHORT).show()
                },
                containerColor = GrowthEngineGold,
                contentColor = DarkInk,
                icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                text = { Text("Run Workflows Now", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_run_automation")
            )
        },
        containerColor = BackgroundWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.testTag("automation_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Surface(
                color = BackgroundWhite,
                tonalElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUTOMATED WORKFLOWS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Business Automations",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${rules.count { it.isEnabled }} of ${rules.size} Automated Rules Active",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = SuccessGreenContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "ACTIVE ENGINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    var isEnabled by remember { mutableStateOf(rule.isEnabled) }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (isEnabled) GrowthEngineGoldDark else TextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = rule.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }

                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = {
                                        isEnabled = it
                                        Toast.makeText(context, "${rule.title} ${if (it) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GrowthEngineGold,
                                        checkedTrackColor = DarkInk
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = rule.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BorderLight)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = "Last triggered: ${rule.lastTriggeredTime}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                Surface(
                                    color = SurfaceSubtle,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                                ) {
                                    Text(
                                        text = rule.channel,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkInk,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
