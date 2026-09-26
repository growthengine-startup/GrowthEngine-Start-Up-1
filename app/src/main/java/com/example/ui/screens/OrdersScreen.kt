package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

enum class OrderFulfillmentStatus(val label: String, val color: Color, val containerColor: Color) {
    PENDING("Booking Confirmed", WarningAmber, WarningAmberContainer),
    PACKED("Packed & Ready", ElectricBlue, Color(0xFFEFF6FF)),
    DISPATCHED("In Transit", GrowthEngineGoldDark, GrowthEngineGoldContainer),
    DELIVERED("Delivered", SuccessGreenDark, SuccessGreenContainer)
}

data class SalesOrderModel(
    val id: String,
    val orderNumber: String,
    val customerName: String,
    val customerGstin: String,
    val customerPhone: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val advancePaid: Double,
    val status: OrderFulfillmentStatus,
    val deliveryDate: String,
    val dateEpoch: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    invoices: List<InvoiceEntity>,
    customers: List<PartyEntity>,
    onCreateOrderClick: () -> Unit,
    onViewOrderDetails: (SalesOrderModel) -> Unit,
    onUpdateOrderStatus: (String, OrderFulfillmentStatus) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val orders = remember(invoices, customers) {
        val baseOrders = mutableListOf<SalesOrderModel>()
        invoices.forEachIndexed { index, inv ->
            val status = when (index % 4) {
                0 -> OrderFulfillmentStatus.PENDING
                1 -> OrderFulfillmentStatus.PACKED
                2 -> OrderFulfillmentStatus.DISPATCHED
                else -> OrderFulfillmentStatus.DELIVERED
            }
            baseOrders.add(
                SalesOrderModel(
                    id = "ORD-${inv.id}",
                    orderNumber = "SO-2026-${1000 + inv.id}",
                    customerName = inv.partyName,
                    customerGstin = inv.partyGstin,
                    customerPhone = inv.partyPhone,
                    itemsSummary = inv.itemsSummary,
                    totalAmount = inv.totalAmount,
                    advancePaid = inv.amountPaid,
                    status = status,
                    deliveryDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(inv.dueDateEpoch)),
                    dateEpoch = inv.dateEpoch
                )
            )
        }
        baseOrders
    }

    val filteredOrders = orders.filter { order ->
        (searchQuery.isBlank() || order.customerName.contains(searchQuery, ignoreCase = true) || order.orderNumber.contains(searchQuery, ignoreCase = true)) &&
                (selectedFilter == "ALL" || (selectedFilter == "ACTIVE" && order.status != OrderFulfillmentStatus.DELIVERED) || (selectedFilter == "DELIVERED" && order.status == OrderFulfillmentStatus.DELIVERED))
    }

    val activeCount = orders.count { it.status != OrderFulfillmentStatus.DELIVERED }
    val totalOrderValue = orders.sumOf { it.totalAmount }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateOrderClick,
                containerColor = GrowthEngineGold,
                contentColor = DarkInk,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Book Sales Order", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_create_order")
            )
        },
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("orders_screen")
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
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "ORDER MANAGEMENT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Sales Orders & Dispatch",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$activeCount Orders Active • ₹${MainViewModel.formatCurrencyPlain(totalOrderValue)} Total Value",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(13.dp))
                                Text("Dispatch Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_order_input"),
                        placeholder = { Text("Search by customer name or order #", fontSize = 13.sp) },
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
                        listOf("ALL" to "All Orders", "ACTIVE" to "Active / In-Fulfillment", "DELIVERED" to "Delivered").forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = key },
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

            if (filteredOrders.isEmpty()) {
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
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                        }
                        Text("No Sales Orders Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        Text("Create a new sales order to track order booking, packing, dispatch, and delivery milestones.", fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        SalesOrderCard(
                            order = order,
                            onView = { onViewOrderDetails(order) },
                            onNextStatus = {
                                val next = when (order.status) {
                                    OrderFulfillmentStatus.PENDING -> OrderFulfillmentStatus.PACKED
                                    OrderFulfillmentStatus.PACKED -> OrderFulfillmentStatus.DISPATCHED
                                    OrderFulfillmentStatus.DISPATCHED -> OrderFulfillmentStatus.DELIVERED
                                    OrderFulfillmentStatus.DELIVERED -> OrderFulfillmentStatus.DELIVERED
                                }
                                onUpdateOrderStatus(order.id, next)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SalesOrderCard(
    order: SalesOrderModel,
    onView: () -> Unit,
    onNextStatus: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${order.orderNumber} • Target: ${order.deliveryDate}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = order.status.containerColor,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, order.status.color.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = order.status.label.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = order.status.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = order.itemsSummary,
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
                    Text("Order Total", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        "₹${MainViewModel.formatCurrencyPlain(order.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkInk
                    )
                }

                if (order.status != OrderFulfillmentStatus.DELIVERED) {
                    Button(
                        onClick = onNextStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (order.status) {
                                OrderFulfillmentStatus.PENDING -> "Mark Packed"
                                OrderFulfillmentStatus.PACKED -> "Mark Dispatched"
                                OrderFulfillmentStatus.DISPATCHED -> "Mark Delivered"
                                OrderFulfillmentStatus.DELIVERED -> "Completed"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreenDark, modifier = Modifier.size(16.dp))
                        Text("Fulfilled", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreenDark)
                    }
                }
            }
        }
    }
}
