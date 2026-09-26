package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PartyEntity
import com.example.data.model.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun WhatsAppReminderDialog(
    reminderText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("whatsapp_reminder_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
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
                            color = ForestEmeraldContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "WhatsApp",
                                tint = ForestEmerald,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Text(
                            text = "WhatsApp Khata Notice",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = WarmIvorySurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reminderText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimaryDark,
                        modifier = Modifier
                            .padding(12.dp)
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Payment Reminder", reminderText))
                            Toast.makeText(context, "Copied reminder to clipboard!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("copy_reminder_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Text")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Ready to dispatch via WhatsApp Business API", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                        modifier = Modifier.weight(1.2f).testTag("send_whatsapp_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Notice")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateInvoiceDialog(
    customers: List<PartyEntity>,
    onDismiss: () -> Unit,
    onSave: (PartyEntity, String, Double, Boolean, String, Int, String) -> Unit
) {
    if (customers.isEmpty()) return

    var selectedCustomer by remember { mutableStateOf(customers.first()) }
    var itemsSummary by remember { mutableStateOf("Precision Bearing Units & Shafts") }
    var subtotalText by remember { mutableStateOf("150000") }
    var isInterState by remember { mutableStateOf(selectedCustomer.stateCode != "27") }
    var paymentMode by remember { mutableStateOf("CREDIT") }
    var creditDaysText by remember { mutableStateOf("30") }
    var notes by remember { mutableStateOf("Dispatched per Customer PO. Terms Net 30.") }

    val subtotal = subtotalText.toDoubleOrNull() ?: 0.0
    val totalTax = subtotal * 0.18
    val grandTotal = subtotal + totalTax

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("create_invoice_dialog")
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
                    Text(
                        text = "New GST Tax Invoice",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImperialNavy
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Select Customer (Buyer):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondaryMuted)
                customers.forEach { customer ->
                    val isChosen = selectedCustomer.id == customer.id
                    Surface(
                        onClick = {
                            selectedCustomer = customer
                            isInterState = customer.stateCode != "27"
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChosen) RoyalTeakGoldContainer else WarmIvorySurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isChosen) RoyalTeakGold else WarmIvoryBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(customer.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                Text("${customer.gstin} • State: ${customer.stateName}", fontSize = 10.sp, color = TextSecondaryMuted)
                            }
                            if (isChosen) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RoyalTeakGoldDark, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = itemsSummary,
                    onValueChange = { itemsSummary = it },
                    label = { Text("Line Items Summary (HSN 8482)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subtotalText,
                    onValueChange = { subtotalText = it },
                    label = { Text("Taxable Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tax preview card
                Surface(
                    color = WarmIvorySurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmIvoryBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isInterState) "IGST (18% Inter-state):" else "CGST (9%) + SGST (9%):", fontSize = 11.sp, color = TextSecondaryMuted)
                            Text("₹${MainViewModel.formatCurrencyPlain(totalTax)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total Payable:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("₹${MainViewModel.formatCurrencyPlain(grandTotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ImperialNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Settlement Method:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSecondaryMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("CREDIT", "UPI", "NEFT_RTGS", "CASH").forEach { mode ->
                        val selected = paymentMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { paymentMode = mode },
                            label = { Text(mode, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ImperialNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (paymentMode == "CREDIT") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = creditDaysText,
                        onValueChange = { creditDaysText = it },
                        label = { Text("Credit Period (Days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSave(
                            selectedCustomer,
                            itemsSummary,
                            subtotal,
                            isInterState,
                            paymentMode,
                            creditDaysText.toIntOrNull() ?: 30,
                            notes
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_invoice_btn")
                ) {
                    Text("Generate & Issue GST Invoice", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RecordPaymentDialog(
    party: PartyEntity,
    onDismiss: () -> Unit,
    onSave: (PartyEntity, Double, String, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf(party.outstandingBalance.toInt().toString()) }
    var mode by remember { mutableStateOf("UPI") }
    var refNo by remember { mutableStateOf("UPI/${(100000..999999).random()}") }
    var notes by remember { mutableStateOf("Payment received against outstanding ledger balance.") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            modifier = Modifier.fillMaxWidth().padding(12.dp).testTag("record_payment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Record Khata Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ImperialNavy)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Text(
                    text = "Customer: ${party.tradeName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Current Outstanding: ₹${MainViewModel.formatCurrencyPlain(party.outstandingBalance)}",
                    fontSize = 11.sp,
                    color = TerracottaRed,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Received (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("UPI", "NEFT_RTGS", "CASH", "CHEQUE").forEach { m ->
                        val isSelected = mode == m
                        FilterChip(
                            selected = isSelected,
                            onClick = { mode = m },
                            label = { Text(m, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestEmerald,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = refNo,
                    onValueChange = { refNo = it },
                    label = { Text("Bank UTR / Reference No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onSave(party, amt, mode, refNo, notes)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_payment_btn")
                ) {
                    Text("Confirm & Update Khata Ledger", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StockAdjustDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSave: (Long, Double) -> Unit
) {
    var stockText by remember { mutableStateOf(product.currentStock.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
            modifier = Modifier.fillMaxWidth().padding(14.dp).testTag("stock_adjust_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                Text("Stock Inward / Adjustment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ImperialNavy)
                Text(product.name, fontSize = 12.sp, color = TextSecondaryMuted)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("Updated Physical Stock (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val newStock = stockText.toDoubleOrNull() ?: product.currentStock
                            onSave(product.id, newStock)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                        modifier = Modifier.weight(1f).testTag("save_stock_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Update Stock", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tradeName by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("33AAACB1234K1Z9") }
    var phone by remember { mutableStateOf("+91 98400 ") }
    var address by remember { mutableStateOf("Chennai, Tamil Nadu") }
    var stateName by remember { mutableStateOf("Tamil Nadu") }
    var stateCode by remember { mutableStateOf("33") }
    var creditLimitText by remember { mutableStateOf("500000") }
    var creditDaysText by remember { mutableStateOf("30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("add_customer_dialog")
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
                    Text("Add Business Customer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary) }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tradeName,
                    onValueChange = {
                        tradeName = it
                        if (name.isBlank()) name = it
                    },
                    label = { Text("Trade Name / Shop Name") },
                    placeholder = { Text("e.g. Dezaa Enterprises") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Person / Legal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = { Text("GSTIN") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stateName,
                        onValueChange = { stateName = it },
                        label = { Text("State") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stateCode,
                        onValueChange = { stateCode = it },
                        label = { Text("Code (e.g. 33)") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = it },
                        label = { Text("Credit Limit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = creditDaysText,
                        onValueChange = { creditDaysText = it },
                        label = { Text("Net Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val limit = creditLimitText.toDoubleOrNull() ?: 500000.0
                        val days = creditDaysText.toIntOrNull() ?: 30
                        if (tradeName.isNotBlank()) {
                            onSave(name, tradeName, gstin, phone, address, stateName, stateCode, limit, days)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_customer")
                ) {
                    Text("Register Customer to Khata", color = Color(0xFF141414), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
