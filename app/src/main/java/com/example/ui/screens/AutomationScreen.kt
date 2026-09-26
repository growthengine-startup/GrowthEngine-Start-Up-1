package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    val channel: String, // "WhatsApp", "SMS", "Email", "App Push"
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
                containerColor = ImperialNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Bolt, contentDescription = null) },
                text = { Text("Run Workflows Now", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_run_automation")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvoryBackground)
                .padding(paddingValues)
                .testTag("automation_screen")
        ) {
            // Header
            Surface(
                color = WarmIvorySurface,
                tonalElevation = 2.dp,
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
                                text = "Automations & Workflows",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialNavy
                            )
                            Text(
                                text = "${rules.count { it.isEnabled }} of ${rules.size} Automated Rules Active",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = ForestGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                                Text("Autopilot ON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    AutomationRuleCard(
                        rule = rule,
                        onToggle = {
                            val idx = rules.indexOfFirst { it.id == rule.id }
                            if (idx != -1) {
                                rules[idx] = rule.copy(isEnabled = !rule.isEnabled)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AutomationRuleCard(
    rule: AutomationRuleModel,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkInk
                    )
                    Text(
                        text = "Trigger: ${rule.triggerType} • Via: ${rule.channel}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ImperialNavy
                    )
                }

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ForestGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = rule.description,
                fontSize = 12.sp,
                color = DarkInk.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
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
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                    Text("Last executed: ${rule.lastTriggeredTime}", fontSize = 11.sp, color = TextSecondary)
                }

                Surface(
                    color = if (rule.isEnabled) ForestGreen.copy(alpha = 0.1f) else BorderSubtle,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (rule.isEnabled) "ACTIVE" else "PAUSED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rule.isEnabled) ForestGreen else TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
