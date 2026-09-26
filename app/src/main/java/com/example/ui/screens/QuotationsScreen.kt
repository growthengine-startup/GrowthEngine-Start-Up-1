package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
fun QuotationsScreen(
    invoices: List<InvoiceEntity>,
    customers: List<PartyEntity>,
    onCreateQuotationClick: () -> Unit,
    onConvertToInvoice: (InvoiceEntity) -> Unit,
    onViewQuotation: (InvoiceEntity) -> Unit,
    onShareQuotation: (InvoiceEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }

    // Quotations / Estimates list
    val quotations = invoices.filter {
        it.invoiceType == "QUOTATION" || it.invoiceType == "PROFORMA" || it.invoiceNumber.startsWith("EST-") || it.invoiceNumber.startsWith("QT-")
    }.ifEmpty {
        invoices.take(3).map {
            it.copy(
                invoiceNumber = it.invoiceNumber.replace("INV-", "EST-"),
                invoiceType = "QUOTATION"
            )
        }
    }

    val filteredQuotations = quotations.filter { q ->
        (searchQuery.isBlank() || q.partyName.contains(searchQuery, ignoreCase = true) || q.invoiceNumber.contains(searchQuery, ignoreCase = true)) &&
                (statusFilter == "ALL" || (statusFilter == "OPEN" && q.paymentStatus != "PAID") || (statusFilter == "CONVERTED" && q.paymentStatus == "PAID"))
    }

    val totalQuotationValue = quotations.sumOf { it.totalAmount }
    val openQuotationsCount = quotations.count { it.paymentStatus != "PAID" }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateQuotationClick,
                containerColor = GrowthEngineGold,
                contentColor = DarkInk,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Estimate / Quote", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_create_quotation")
            )
        },
        containerColor = BackgroundWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.testTag("quotations_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Card
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
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "SALES PIPELINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Quotations & Estimates",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$openQuotationsCount Open • ₹${MainViewModel.formatCurrencyPlain(totalQuotationValue)} Pipeline",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            color = SuccessGreenContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreenDark, modifier = Modifier.size(13.dp))
                                Text("1-Tap Convert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SuccessGreenDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search and Filter
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_quotation_input"),
                        placeholder = { Text("Search by customer name or estimate #", fontSize = 13.sp) },
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
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("ALL" to "All Quotes", "OPEN" to "Open / Pending", "CONVERTED" to "Converted to Invoice").forEach { (key, label) ->
                            val isSelected = statusFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { statusFilter = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkInk,
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceSubtle,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) DarkInk else BorderSubtle
                                )
                            )
                        }
                    }
                }
            }

            if (filteredQuotations.isEmpty()) {
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
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SurfaceSubtle)
                                .border(1.dp, BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                        }
                        Text("No Quotations Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Create a new quotation or estimate to send professional price quotes to clients via WhatsApp or PDF.", fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredQuotations, key = { it.id }) { quote ->
                        QuotationCard(
                            quotation = quote,
                            onView = { onViewQuotation(quote) },
                            onConvert = { onConvertToInvoice(quote) },
                            onShare = { onShareQuotation(quote) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuotationCard(
    quotation: InvoiceEntity,
    onView: () -> Unit,
    onConvert: () -> Unit,
    onShare: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(quotation.dateEpoch))
    val isConverted = quotation.paymentStatus == "PAID"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("quotation_card_${quotation.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quotation.partyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${quotation.invoiceNumber} • $dateStr",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = if (isConverted) SuccessGreenContainer else GrowthEngineGoldContainer,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isConverted) SuccessGreen else GrowthEngineGoldBorder)
                ) {
                    Text(
                        text = if (isConverted) "CONVERTED" else "VALID 15 DAYS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConverted) SuccessGreenDark else GrowthEngineGoldDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = quotation.itemsSummary,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Estimate", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        "₹${MainViewModel.formatCurrencyPlain(quotation.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkInk
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onShare,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = DarkInk, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp, color = DarkInk)
                    }

                    if (!isConverted) {
                        Button(
                            onClick = onConvert,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenDark, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Convert to Invoice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
