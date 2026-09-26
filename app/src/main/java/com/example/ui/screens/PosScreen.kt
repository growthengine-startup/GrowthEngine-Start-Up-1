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
import com.example.data.model.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.PosCartItem
import com.example.ui.theme.*

@Composable
fun PosScreen(
    products: List<ProductEntity>,
    cart: List<PosCartItem>,
    isWholesale: Boolean,
    successMessage: String?,
    onAddItem: (ProductEntity) -> Unit,
    onUpdateQuantity: (Long, Double) -> Unit,
    onTogglePriceTier: () -> Unit,
    onClearCart: () -> Unit,
    onCheckout: (String) -> Unit,
    onDismissSuccess: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf("UPI") }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    val cartSubtotal = cart.sumOf { it.unitPrice * it.quantity }
    val cartGst = cartSubtotal * 0.18
    val cartGrandTotal = cartSubtotal + cartGst

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .testTag("pos_screen")
    ) {
        // POS Header bar with Wholesale Toggle
        Surface(
            color = BackgroundWhite,
            tonalElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "POINT OF SALE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthEngineGoldDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isWholesale) "B2B Wholesale POS" else "Retail Counter POS",
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Wholesale Rate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (isWholesale) DarkInk else TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isWholesale,
                        onCheckedChange = { onTogglePriceTier() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GrowthEngineGold,
                            checkedTrackColor = DarkInk
                        ),
                        modifier = Modifier.testTag("pos_wholesale_toggle")
                    )
                }
            }
        }

        // Success Notification Banner
        if (successMessage != null) {
            Surface(
                color = SuccessGreenContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreenDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successMessage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreenDark)
                    }
                    IconButton(onClick = onDismissSuccess) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = SuccessGreenDark, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Search item input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search catalog or scan barcode...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("pos_search_input"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkInk,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite
            )
        )

        // Split view: Catalog + Live Cart
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Product Catalog
            LazyColumn(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredProducts) { product ->
                    val price = if (isWholesale) product.wholesalePrice else product.mrp
                    Card(
                        onClick = { onAddItem(product) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth().testTag("pos_product_${product.sku}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = product.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "SKU: ${product.sku} • HSN: ${product.hsnCode}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Stock: ${product.currentStock.toInt()} ${product.unit}",
                                    fontSize = 10.sp,
                                    color = if (product.currentStock <= product.minReorderLevel) ErrorRedDark else SuccessGreenDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${MainViewModel.formatCurrencyPlain(price)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkInk
                                )
                                Text(
                                    text = "+18% GST",
                                    fontSize = 9.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = GrowthEngineGoldContainer,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GrowthEngineGoldBorder)
                                ) {
                                    Text(
                                        text = "+ ADD",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrowthEngineGoldDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live POS Cart Column
            Surface(
                color = SurfaceWhite,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .testTag("pos_cart_container")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Bill (${cart.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (cart.isNotEmpty()) {
                            TextButton(
                                onClick = onClearCart,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Clear", fontSize = 11.sp, color = ErrorRedDark)
                            }
                        }
                    }

                    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))

                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Cart is empty", fontSize = 12.sp, color = TextSecondary)
                                Text("Tap items on left to add", fontSize = 10.sp, color = TextTertiary)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(cart) { item ->
                                Surface(
                                    color = SurfaceSubtle,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = item.product.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "₹${MainViewModel.formatCurrencyPlain(item.unitPrice * item.quantity)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkInk
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { onUpdateQuantity(item.product.id, -1.0) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                                }
                                                Text(
                                                    text = "${item.quantity.toInt()}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                IconButton(
                                                    onClick = { onUpdateQuantity(item.product.id, 1.0) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 4.dp))

                        // Bill totals breakdown
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", fontSize = 10.sp, color = TextSecondary)
                                Text("₹${MainViewModel.formatCurrencyPlain(cartSubtotal)}", fontSize = 10.sp, color = TextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("GST (18%):", fontSize = 10.sp, color = TextSecondary)
                                Text("₹${MainViewModel.formatCurrencyPlain(cartGst)}", fontSize = 10.sp, color = TextPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("₹${MainViewModel.formatCurrencyPlain(cartGrandTotal)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Payment mode selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("UPI", "CASH", "CREDIT").forEach { mode ->
                                val isSelected = selectedPaymentMode == mode
                                Surface(
                                    onClick = { selectedPaymentMode = mode },
                                    color = if (isSelected) DarkInk else SurfaceSubtle,
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) DarkInk else BorderSubtle),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier.padding(vertical = 5.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onCheckout(selectedPaymentMode) },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pos_checkout_button")
                        ) {
                            Text("Settle Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
