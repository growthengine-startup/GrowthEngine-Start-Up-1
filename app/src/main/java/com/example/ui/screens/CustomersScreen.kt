package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.example.data.model.InvoiceEntity
import com.example.data.model.PartyEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    customers: List<PartyEntity>,
    invoices: List<InvoiceEntity>,
    onAddNewCustomer: () -> Unit,
    onRecordPayment: (PartyEntity) -> Unit,
    onSendWhatsAppReminder: (PartyEntity) -> Unit,
    onCreateInvoiceForCustomer: (PartyEntity) -> Unit,
    onViewInvoice: (InvoiceEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") }
    var selectedCustomerProfile by remember { mutableStateOf<PartyEntity?>(null) }
    val context = LocalContext.current

    // If a customer is opened, render the Isolated Customer Profile & Ledger screen
    if (selectedCustomerProfile != null) {
        val activeCustomer = selectedCustomerProfile!!
        // Re-read active customer from source list in case balance was updated
        val currentCustomer = customers.find { it.id == activeCustomer.id } ?: activeCustomer

        IsolatedCustomerProfileScreen(
            customer = currentCustomer,
            customerInvoices = invoices.filter {
                it.partyId == currentCustomer.id || it.partyName.equals(currentCustomer.name, ignoreCase = true) || it.partyGstin == currentCustomer.gstin
            },
            onBack = { selectedCustomerProfile = null },
            onRecordPayment = { onRecordPayment(currentCustomer) },
            onSendWhatsApp = { onSendWhatsAppReminder(currentCustomer) },
            onCreateInvoice = { onCreateInvoiceForCustomer(currentCustomer) },
            onViewInvoice = onViewInvoice
        )
        return
    }

    val totalReceivable = customers.sumOf { it.outstandingBalance }
    val overdueCustomersCount = customers.count { it.overdueDays > 0 }

    val filteredCustomers = customers.filter { cust ->
        val matchesQuery = searchQuery.isBlank() ||
                cust.name.contains(searchQuery, ignoreCase = true) ||
                cust.tradeName.contains(searchQuery, ignoreCase = true) ||
                cust.gstin.contains(searchQuery, ignoreCase = true) ||
                cust.phone.contains(searchQuery)

        val matchesFilter = when (filterType) {
            "OVERDUE" -> cust.overdueDays > 0
            "PENDING" -> cust.outstandingBalance > 0
            "CLEAR" -> cust.outstandingBalance <= 0
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewCustomer,
                containerColor = ImperialNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Customer", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_customer")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmIvoryBackground)
                .padding(paddingValues)
                .testTag("customers_screen")
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
                                text = "Customers & Receivables",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialNavy
                            )
                            Text(
                                text = "${customers.size} Total Accounts • $overdueCustomersCount Overdue",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = TerracottaRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Total Pending", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalReceivable)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_customer_input"),
                        placeholder = { Text("Search by name, trade name, GSTIN, phone...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImperialNavy,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = BackgroundWhite,
                            unfocusedContainerColor = BackgroundWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("ALL" to "All (${customers.size})", "OVERDUE" to "Overdue ($overdueCustomersCount)", "PENDING" to "Has Balance", "CLEAR" to "Settled").forEach { (key, label) ->
                            FilterChip(
                                selected = filterType == key,
                                onClick = { filterType = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ImperialNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(56.dp))
                        Text("No Customers Found", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkInk)
                        Text("Add customer profiles to track credit limits, generate GST invoices, and maintain digital khata ledgers.", fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerListItemCard(
                            customer = customer,
                            onClick = { selectedCustomerProfile = customer },
                            onSendWhatsApp = { onSendWhatsAppReminder(customer) },
                            onRecordPayment = { onRecordPayment(customer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerListItemCard(
    customer: PartyEntity,
    onClick: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onRecordPayment: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = ImperialNavy.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = customer.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = ImperialNavy,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkInk
                        )
                        if (customer.tradeName.isNotBlank() && customer.tradeName != customer.name) {
                            Text(
                                text = customer.tradeName,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = "GSTIN: ${if (customer.gstin.isNotBlank()) customer.gstin else "Unregistered"} • ${customer.phone}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${MainViewModel.formatCurrencyPlain(customer.outstandingBalance)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (customer.outstandingBalance > 0) TerracottaRed else ForestGreen
                    )
                    Text(
                        text = if (customer.outstandingBalance > 0) "Pending" else "Settled",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            if (customer.overdueDays > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ErrorRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(13.dp))
                            Text(
                                text = "Overdue by ${customer.overdueDays} days",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ErrorRed
                            )
                        }
                        Text(
                            text = "Terms: ${customer.paymentTermsDays} Days",
                            fontSize = 10.sp,
                            color = DarkInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Credit Limit: ₹${MainViewModel.formatCurrencyPlain(customer.creditLimit)}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (customer.outstandingBalance > 0) {
                        OutlinedButton(
                            onClick = onSendWhatsApp,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reminder", fontSize = 11.sp, color = ForestGreen)
                        }

                        Button(
                            onClick = onRecordPayment,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Collect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("View Khata", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * STRICT ISOLATED CUSTOMER PROFILE & LEDGER VIEW
 * Guarantees zero cross-contamination: Shows ONLY this customer's data and invoices.
 */
@Composable
fun IsolatedCustomerProfileScreen(
    customer: PartyEntity,
    customerInvoices: List<InvoiceEntity>,
    onBack: () -> Unit,
    onRecordPayment: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onCreateInvoice: () -> Unit,
    onViewInvoice: (InvoiceEntity) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .testTag("isolated_customer_profile_${customer.id}")
    ) {
        // Top App Bar for Profile
        Surface(
            color = ImperialNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = customer.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (customer.tradeName.isNotBlank()) customer.tradeName else "Customer Account",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "ISOLATED LEDGER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer Financial Header Card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Outstanding", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(customer.outstandingBalance)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (customer.outstandingBalance > 0) TerracottaRed else ForestGreen
                            )
                            if (customer.overdueDays > 0) {
                                Text(
                                    "Overdue by ${customer.overdueDays} days",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ErrorRed
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Credit Limit", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                "₹${MainViewModel.formatCurrencyPlain(customer.creditLimit)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk
                            )
                            Text(
                                "Terms: ${customer.paymentTermsDays} Days",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRecordPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldContainer, contentColor = DarkInk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCreateInvoice,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onSendWhatsApp,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "WhatsApp", tint = Color.White)
                    }
                }
            }
        }

        // Profile Details & Isolated Invoice History
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Customer Info Details Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PROFILE & COMPLIANCE DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRow("GSTIN", if (customer.gstin.isNotBlank()) customer.gstin else "Unregistered Regular")
                        InfoRow("PAN Number", if (customer.panNumber.isNotBlank()) customer.panNumber else "N/A")
                        InfoRow("Phone", customer.phone)
                        InfoRow("Email", if (customer.email.isNotBlank()) customer.email else "N/A")
                        InfoRow("Billing Address", "${customer.address}, ${customer.stateName} (Code: ${customer.stateCode})")
                    }
                }
            }

            // Customer Statement & Invoices Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer Ledger & Invoices (${customerInvoices.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkInk
                    )

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Exported Statement PDF for ${customer.name}", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF", fontSize = 11.sp)
                    }
                }
            }

            if (customerInvoices.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                Text("No Invoices for this Customer", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkInk)
                                Text("Tap 'New Invoice' above to issue the first tax invoice for ${customer.name}.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            } else {
                items(customerInvoices, key = { it.id }) { inv ->
                    CustomerIsolatedInvoiceCard(
                        invoice = inv,
                        onView = { onViewInvoice(inv) }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DarkInk)
    }
}

@Composable
fun CustomerIsolatedInvoiceCard(
    invoice: InvoiceEntity,
    onView: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(invoice.dateEpoch))
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("isolated_inv_${invoice.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.invoiceNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ImperialNavy
                )
                Text(
                    text = "$dateStr • ${invoice.itemsCount} Items",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = invoice.itemsSummary,
                    fontSize = 11.sp,
                    color = DarkInk.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${MainViewModel.formatCurrencyPlain(invoice.totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DarkInk
                )
                Surface(
                    color = when (invoice.paymentStatus) {
                        "PAID" -> ForestGreen.copy(alpha = 0.15f)
                        "PARTIAL" -> WarningAmber.copy(alpha = 0.15f)
                        else -> TerracottaRed.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = invoice.paymentStatus,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (invoice.paymentStatus) {
                            "PAID" -> ForestGreen
                            "PARTIAL" -> WarningAmber
                            else -> TerracottaRed
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
