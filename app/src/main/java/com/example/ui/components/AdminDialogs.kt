package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.supabase.SupabaseSchema
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun EditBusinessPlanDialog(
    business: BusinessAccount,
    onDismiss: () -> Unit,
    onSave: (BusinessAccount) -> Unit
) {
    var selectedTier by remember { mutableStateOf(business.planTier) }
    var selectedStatus by remember { mutableStateOf(business.subscriptionStatus) }
    var isSuspended by remember { mutableStateOf(business.status == "SUSPENDED") }
    var suspensionReason by remember { mutableStateOf("Overdue billing or TOS compliance review") }
    var aiQuotaText by remember { mutableStateOf(business.aiMonthlyQuota.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_edit_business_plan")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Business & Plan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    text = "${business.name} (${business.ownerEmail})",
                    fontSize = 12.sp,
                    color = GrowthEngineGoldDark,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Assigned Subscription Plan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                SubscriptionTier.values().forEach { tier ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTier == tier) GrowthEngineGoldContainer else SurfaceSubtle)
                            .border(
                                1.dp,
                                if (selectedTier == tier) GrowthEngineGoldBorder else BorderSubtle,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTier = tier }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(tier.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (tier.monthlyPrice == 0.0) "Free forever" else "₹${tier.monthlyPrice.toInt()}/mo",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        RadioButton(
                            selected = (selectedTier == tier),
                            onClick = { selectedTier = tier }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Subscription State", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SubscriptionStatus.values().take(4).forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status.label, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SubscriptionStatus.values().drop(4).forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status.label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = aiQuotaText,
                    onValueChange = { aiQuotaText = it },
                    label = { Text("Monthly AI Token Quota") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Suspend Account Access", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ErrorRed)
                        Text("Disables API, POS and multi-user login", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = isSuspended,
                        onCheckedChange = { isSuspended = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ErrorRed, checkedTrackColor = ErrorRed.copy(alpha = 0.2f))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newQuota = aiQuotaText.toLongOrNull() ?: business.aiMonthlyQuota
                            onSave(
                                business.copy(
                                    planTier = selectedTier,
                                    subscriptionStatus = selectedStatus,
                                    status = if (isSuspended) "SUSPENDED" else "ACTIVE",
                                    aiMonthlyQuota = newQuota
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark)
                    ) {
                        Text("Apply Changes", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustAiQuotaDialog(
    business: BusinessAccount,
    onDismiss: () -> Unit,
    onSave: (Long, Long, Boolean) -> Unit
) {
    var quotaInput by remember { mutableStateOf(business.aiMonthlyQuota.toString()) }
    var bonusInput by remember { mutableStateOf("50000") }
    var isThrottled by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "AI Quota & Token Metering",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Business: ${business.name}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = quotaInput,
                    onValueChange = { quotaInput = it },
                    label = { Text("Base Monthly Token Quota") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bonusInput,
                    onValueChange = { bonusInput = it },
                    label = { Text("Allocate Bonus One-Time Tokens") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Throttle on Quota Exhaustion", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Switch(checked = isThrottled, onCheckedChange = { isThrottled = it })
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val q = quotaInput.toLongOrNull() ?: business.aiMonthlyQuota
                            val b = bonusInput.toLongOrNull() ?: 0L
                            onSave(q, b, isThrottled)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark)
                    ) {
                        Text("Save Quota", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BroadcastNotificationDialog(
    onDismiss: () -> Unit,
    onSend: (NotificationBroadcast) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("ALL_USERS") }
    var channel by remember { mutableStateOf("IN_APP_PUSH") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Dispatch Broadcast Notification",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title") },
                    placeholder = { Text("e.g. Important GST Return Deadline Update") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Body") },
                    placeholder = { Text("Enter detailed notification message for business owners...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Target Audience Segment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                val audiences = listOf("ALL_USERS" to "All Registered Users", "FREE_TIER" to "Free Starter Businesses", "PRO_TIER" to "Growth Pro & Enterprise", "OVERDUE_USERS" to "Businesses with Overdue Subs")
                audiences.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { audience = key }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = audience == key, onClick = { audience = key })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(label, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                onSend(
                                    NotificationBroadcast(
                                        title = title,
                                        message = message,
                                        targetAudience = audience,
                                        channel = channel
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark)
                    ) {
                        Text("Send Broadcast", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionDetailDialog(
    transaction: PaymentTransactionRecord,
    onDismiss: () -> Unit,
    onRefund: (PaymentTransactionRecord) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payment Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = GrowthEngineGoldContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Paid: ₹${MainViewModel.formatCurrencyPlain(transaction.totalWithGstInr)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                        Text("Base: ₹${MainViewModel.formatCurrencyPlain(transaction.amountInr)} + 18% GST (₹${MainViewModel.formatCurrencyPlain(transaction.gstAmountInr)})", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow("Business Name", transaction.businessName)
                DetailRow("User Email", transaction.userEmail)
                DetailRow("Razorpay Payment ID", transaction.razorpayPaymentId)
                DetailRow("Razorpay Order ID", transaction.razorpayOrderId)
                DetailRow("Tier & Cycle", "${transaction.tier.title} (${transaction.billingCycle.name})")
                DetailRow("Payment Mode", transaction.paymentMode)
                DetailRow("Date", MainViewModel.formatDate(transaction.timestamp))
                DetailRow("Status", transaction.status)

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(transaction.razorpayPaymentId))
                            Toast.makeText(context, "Razorpay ID Copied", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy ID", fontSize = 12.sp)
                    }

                    if (transaction.status == "CAPTURED") {
                        Button(
                            onClick = { onRefund(transaction) },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("Initiate Refund", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportTicketDialog(
    ticket: SupportTicket,
    onDismiss: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ticket ${ticket.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(ticket.subject, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkInk)
                Text("${ticket.businessName} • ${ticket.userEmail}", fontSize = 11.sp, color = TextSecondary)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Ticket Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED").forEach { st ->
                        FilterChip(
                            selected = ticket.status == st,
                            onClick = { onUpdateStatus(st) },
                            label = { Text(st, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Admin Reply / Resolution Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Reply dispatched to ${ticket.userEmail}", Toast.LENGTH_SHORT).show()
                        onUpdateStatus("RESOLVED")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark)
                ) {
                    Text("Dispatch Reply & Resolve", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SupabaseSqlViewerDialog(
    onDismiss: () -> Unit
) {
    var selectedSection by remember { mutableStateOf(0) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val titles = listOf("Complete SQL Script", "Auth & Storage Setup", "Edge Functions & Razorpay")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Supabase Architecture Center", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = SurfaceSubtle,
                    contentColor = GrowthEngineGoldDark
                ) {
                    titles.forEachIndexed { index, t ->
                        Tab(
                            selected = selectedSection == index,
                            onClick = { selectedSection = index },
                            text = { Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val contentToShow = when (selectedSection) {
                    0 -> SupabaseSchema.SQL_SCRIPT
                    1 -> SupabaseSchema.AUTH_CONFIG_DOC
                    else -> SupabaseSchema.AUTH_CONFIG_DOC
                }

                Surface(
                    color = SurfaceSubtle,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            text = contentToShow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = DarkInk,
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedSection == 0) "Ready to paste into Supabase SQL Editor" else "Configuration guide",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(contentToShow))
                            Toast.makeText(context, "Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Script", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkInk)
    }
}
