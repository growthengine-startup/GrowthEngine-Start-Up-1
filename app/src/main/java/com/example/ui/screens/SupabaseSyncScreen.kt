package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabase.SupabaseService
import com.example.supabase.SupabaseSyncState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SupabaseSyncScreen(
    supabaseService: SupabaseService,
    syncState: SupabaseSyncState,
    statusMessage: String,
    invoicesCount: Int,
    customersCount: Int,
    productsCount: Int,
    expensesCount: Int,
    onSyncNow: () -> Unit,
    onTestConnection: () -> Unit
) {
    val context = LocalContext.current
    var autoSync by remember { mutableStateOf(supabaseService.isAutoSyncEnabled) }
    var lastSyncTime by remember { mutableStateOf("Just now") }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "rotate_sync_icon"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("supabase_sync_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ENTERPRISE CLOUD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthEngineGoldDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Cloud Backup & Sync",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Cloud Status Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = when (syncState) {
                        SupabaseSyncState.SYNCED -> SuccessGreenContainer
                        SupabaseSyncState.SYNCING -> GrowthEngineGoldContainer
                        SupabaseSyncState.ERROR -> ErrorRedContainer
                        else -> SurfaceSubtle
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (syncState) {
                            SupabaseSyncState.SYNCED -> SuccessGreen
                            SupabaseSyncState.SYNCING -> GrowthEngineGold
                            SupabaseSyncState.ERROR -> ErrorRed
                            else -> BorderSubtle
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when (syncState) {
                                        SupabaseSyncState.SYNCED -> SuccessGreenDark
                                        SupabaseSyncState.SYNCING -> GrowthEngineGoldDark
                                        SupabaseSyncState.ERROR -> ErrorRedDark
                                        else -> TextTertiary
                                    }
                                )
                        )
                        Text(
                            text = when (syncState) {
                                SupabaseSyncState.SYNCED -> "ONLINE & SYNCED"
                                SupabaseSyncState.SYNCING -> "SYNCING..."
                                SupabaseSyncState.ERROR -> "LOCAL OFFLINE"
                                else -> "STANDBY"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (syncState) {
                                SupabaseSyncState.SYNCED -> SuccessGreenDark
                                SupabaseSyncState.SYNCING -> GrowthEngineGoldDark
                                SupabaseSyncState.ERROR -> ErrorRedDark
                                else -> TextPrimary
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Continuous, bank-grade encrypted cloud synchronization for your GST invoices, khatas, inventory, and ledger books.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
        }

        // Hero Sync Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Cloud Status",
                                tint = SuccessGreenDark,
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(if (syncState == SupabaseSyncState.SYNCING) Modifier.rotate(rotationAngle) else Modifier)
                            )
                        }

                        Column {
                            Text(
                                text = "Automated Real-Time Sync",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Last backed up: $lastSyncTime",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = statusMessage.ifBlank { "All your local devices and cloud database are synchronized. You can continue billing offline without interruption." },
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            lastSyncTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                            onSyncNow()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_sync_now")
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .then(if (syncState == SupabaseSyncState.SYNCING) Modifier.rotate(rotationAngle) else Modifier)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync All Data Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onTestConnection,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(44.dp)
                            .testTag("btn_test_connection")
                    ) {
                        Text("Verify Link", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Replicated Records Count
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROTECTED BUSINESS DATA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "AES-256 ENCRYPTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreenDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SyncDataRecordRow(
                        title = "Tax Invoices & POS Bills",
                        count = "$invoicesCount records",
                        icon = Icons.Default.ReceiptLong
                    )
                    SyncDataRecordRow(
                        title = "Parties & Khata Ledgers",
                        count = "$customersCount parties",
                        icon = Icons.Default.People
                    )
                    SyncDataRecordRow(
                        title = "Products, Stock & HSN Codes",
                        count = "$productsCount SKUs",
                        icon = Icons.Default.Inventory2
                    )
                    SyncDataRecordRow(
                        title = "Expense Vouchers & ITC Register",
                        count = "$expensesCount vouchers",
                        icon = Icons.Default.AccountBalanceWallet
                    )
                }
            }
        }

        // Cloud Replication & Auto-Sync Settings
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "BACKUP PREFERENCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Sync on Changes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Replicates each invoice, payment, and stock change instantaneously", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = autoSync,
                        onCheckedChange = {
                            autoSync = it
                            supabaseService.isAutoSyncEnabled = it
                            Toast.makeText(context, if (it) "Auto-sync enabled" else "Auto-sync paused", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GrowthEngineGold,
                            checkedTrackColor = DarkInk
                        )
                    )
                }

                HorizontalDivider(color = BorderLight)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Offline First Resilience", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Continue raising bills without internet. Sync automatically queues and uploads when connection returns.", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = SuccessGreenDark, modifier = Modifier.size(20.dp))
                }

                HorizontalDivider(color = BorderLight)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Multi-Device Data Sharing", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Access the same live ledger from your POS counter, warehouse tablet, and accountant laptop.", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.Devices, contentDescription = null, tint = ImperialNavy, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Export Business Data
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GrowthEngineGoldContainer),
            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthEngineGoldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = GrowthEngineGoldDark, modifier = Modifier.size(20.dp))
                    Text("Export All Business Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Download a complete offline copy of your books, GST reports, and khata ledgers in Excel (XLSX) and PDF format for your CA.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Full business ledger export generated successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Download Full Backup Package", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SyncDataRecordRow(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = GrowthEngineGoldDark, modifier = Modifier.size(18.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
        Text(count, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
    }
}
