package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Double, Double, Double, Double, Double, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var hsn by remember { mutableStateOf("8482") }
    var category by remember { mutableStateOf("Mechanical Spares") }
    var unit by remember { mutableStateOf("Pcs") }
    var purchasePrice by remember { mutableStateOf("1200") }
    var wholesalePrice by remember { mutableStateOf("1650") }
    var mrp by remember { mutableStateOf("1950") }
    var gstRate by remember { mutableStateOf("18.0") }
    var stock by remember { mutableStateOf("25") }
    var minReorder by remember { mutableStateOf("10") }
    var supplier by remember { mutableStateOf("SKF Bearings India Ltd") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 540.dp)
                .heightIn(max = 680.dp)
                .imePadding()
                .padding(vertical = 10.dp)
                .testTag("add_product_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Inventory SKU", fontSize = 16.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary) }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product / Component Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    OutlinedTextField(
                        value = hsn,
                        onValueChange = { hsn = it },
                        label = { Text("HSN Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (Pcs/Kg/Mtr)") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wholesalePrice,
                        onValueChange = { wholesalePrice = it },
                        label = { Text("Wholesale Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("Purchase Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Initial Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    OutlinedTextField(
                        value = minReorder,
                        onValueChange = { minReorder = it },
                        label = { Text("Reorder Trigger") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && sku.isNotBlank()) {
                            onSave(
                                name,
                                sku,
                                hsn,
                                category,
                                unit,
                                purchasePrice.toDoubleOrNull() ?: 0.0,
                                wholesalePrice.toDoubleOrNull() ?: 0.0,
                                mrp.toDoubleOrNull() ?: 0.0,
                                gstRate.toDoubleOrNull() ?: 18.0,
                                stock.toDoubleOrNull() ?: 0.0,
                                minReorder.toDoubleOrNull() ?: 0.0,
                                supplier
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold, contentColor = DarkInk),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_add_product")
                ) {
                    Text("Register Item to Catalog", color = DarkInk, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Boolean, Double, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Freight & Logistics") }
    var amountText by remember { mutableStateOf("12500") }
    var isGstClaimable by remember { mutableStateOf(true) }
    var paymentMode by remember { mutableStateOf("UPI") }
    var vendor by remember { mutableStateOf("Safexpress Logistics") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val gstAmount = if (isGstClaimable) amount * 0.18 else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 540.dp)
                .heightIn(max = 680.dp)
                .imePadding()
                .padding(vertical = 10.dp)
                .testTag("add_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Log Enterprise Expense", fontSize = 16.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary) }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title / Voucher") },
                    placeholder = { Text("e.g. CNC Coolant Refill") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    label = { Text("Vendor / Service Provider") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("GST Claimable (ITC)?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Deduct ₹${MainViewModel.formatCurrencyPlain(gstAmount)} in GSTR-3B", fontSize = 10.sp, color = SuccessGreenDark)
                    }
                    Switch(
                        checked = isGstClaimable,
                        onCheckedChange = { isGstClaimable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GrowthEngineGold, checkedTrackColor = DarkInk)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && amount > 0) {
                            onSave(title, category, amount, isGstClaimable, gstAmount, paymentMode, vendor)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_add_expense")
                ) {
                    Text("Save Expense Voucher", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NewBatchDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, Double) -> Unit
) {
    var goodName by remember { mutableStateOf("Hydraulic Piston Cylinder 100 Ton") }
    var targetQtyText by remember { mutableStateOf("20") }
    var unit by remember { mutableStateOf("Pcs") }
    var rawMaterials by remember { mutableStateOf("Chrome Shaft 25mm (40m), Seal Kits (20 sets)") }
    var costPerUnitText by remember { mutableStateOf("13800") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 540.dp)
                .heightIn(max = 680.dp)
                .imePadding()
                .padding(vertical = 10.dp)
                .testTag("new_batch_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Schedule Production Batch", fontSize = 16.sp, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary) }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = goodName,
                    onValueChange = { goodName = it },
                    label = { Text("Finished Good Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetQtyText,
                        onValueChange = { targetQtyText = it },
                        label = { Text("Target Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawMaterials,
                    onValueChange = { rawMaterials = it },
                    label = { Text("BOM Recipe & Raw Materials") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = costPerUnitText,
                    onValueChange = { costPerUnitText = it },
                    label = { Text("Estimated Unit Cost (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val qty = targetQtyText.toDoubleOrNull() ?: 1.0
                        val cost = costPerUnitText.toDoubleOrNull() ?: 0.0
                        if (goodName.isNotBlank()) {
                            onSave(goodName, qty, unit, rawMaterials, cost)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold, contentColor = DarkInk),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_new_batch")
                ) {
                    Text("Issue Batch to Shopfloor", color = DarkInk, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
