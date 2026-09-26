package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ManufacturingOrderEntity
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun ManufacturingScreen(
    batches: List<ManufacturingOrderEntity>,
    onUpdateBatchStatus: (Long, String) -> Unit,
    onNewBatchClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewBatchClick,
                containerColor = ImperialNavy,
                contentColor = RoyalTeakGoldLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_batch")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Batch")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Batch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        containerColor = WarmIvoryBackground,
        modifier = Modifier.testTag("manufacturing_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header summary
            Surface(
                color = WarmIvorySurface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Production & Bill of Materials (BOM)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                        Text(
                            text = "${batches.size} Production Batches Tracked",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted
                        )
                    }

                    Surface(
                        color = RoyalTeakGoldContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "JOB WORK & CNC",
                            color = RoyalTeakGoldDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Batches List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(batches) { batch ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
                        modifier = Modifier.fillMaxWidth().testTag("batch_card_${batch.batchCode}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = batch.batchCode,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ImperialNavy
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${batch.targetQuantity.toInt()} ${batch.unit}",
                                        fontSize = 12.sp,
                                        color = TextSecondaryMuted
                                    )
                                }
                                StatusBadge(status = batch.status)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = batch.finishedGoodName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                color = WarmIvorySurfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Raw Materials & Recipe Used:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondaryMuted
                                    )
                                    Text(
                                        text = batch.rawMaterialsUsedSummary,
                                        fontSize = 11.sp,
                                        color = TextPrimaryDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Est. Unit Cost: ₹${MainViewModel.formatCurrencyPlain(batch.estimatedCostPerUnit)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ImperialNavy
                                    )
                                    Text(
                                        text = "Start: ${MainViewModel.formatDate(batch.startDateEpoch)}",
                                        fontSize = 9.sp,
                                        color = TextSecondaryMuted
                                    )
                                }

                                if (batch.status == "IN_PRODUCTION") {
                                    Button(
                                        onClick = { onUpdateBatchStatus(batch.id, "QUALITY_CHECK") },
                                        colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Move to QC", fontSize = 10.sp)
                                    }
                                } else if (batch.status == "QUALITY_CHECK") {
                                    Button(
                                        onClick = { onUpdateBatchStatus(batch.id, "COMPLETED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Mark Ready", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
