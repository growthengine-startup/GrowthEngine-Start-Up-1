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
import androidx.compose.ui.text.style.TextOverflow
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
    var phoneActiveTab by remember { mutableStateOf(0) } // 0: Catalog, 1: Bill

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
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
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
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        "Wholesale",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isWholesale) DarkInk else TextSecondary
                    )
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
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreenDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            successMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreenDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                .padding(horizontal = 14.dp, vertical = 8.dp)
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

        // Responsive Body
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val isWideScreen = maxWidth >= 600.dp

            if (isWideScreen) {
                // Wide Screen Split View (Landscape / Tablet)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Catalog Column
                    Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                        PosCatalogList(
                            products = filteredProducts,
                            isWholesale = isWholesale,
                            onAddItem = onAddItem
                        )
                    }

                    // Cart Column
                    Surface(
                        color = SurfaceWhite,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .testTag("pos_cart_container")
                    ) {
                        PosCartContent(
                            cart = cart,
                            cartSubtotal = cartSubtotal,
                            cartGst = cartGst,
                            cartGrandTotal = cartGrandTotal,
                            selectedPaymentMode = selectedPaymentMode,
                            onPaymentModeSelect = { selectedPaymentMode = it },
                            onUpdateQuantity = onUpdateQuantity,
                            onClearCart = onClearCart,
                            onCheckout = onCheckout
                        )
                    }
                }
            } else {
                // Mobile Phone Responsive Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                ) {
                    // Mode Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { phoneActiveTab = 0 },
                            shape = RoundedCornerShape(8.dp),
                            color = if (phoneActiveTab == 0) GrowthEngineGoldContainer else SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (phoneActiveTab == 0) GrowthEngineGold else BorderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (phoneActiveTab == 0) DarkInk else TextSecondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Catalog (${filteredProducts.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (phoneActiveTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (phoneActiveTab == 0) DarkInk else TextSecondary
                                )
                            }
                        }

                        Surface(
                            onClick = { phoneActiveTab = 1 },
                            shape = RoundedCornerShape(8.dp),
                            color = if (phoneActiveTab == 1) GrowthEngineGoldContainer else SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (phoneActiveTab == 1) GrowthEngineGold else BorderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(15.dp), tint = if (phoneActiveTab == 1) DarkInk else TextSecondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bill (${cart.size}) • ₹${MainViewModel.formatCurrencyPlain(cartGrandTotal)}",
                                    fontSize = 12.sp,
                                    fontWeight = if (phoneActiveTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (phoneActiveTab == 1) DarkInk else TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Tab Body
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (phoneActiveTab == 0) {
                            // Catalog with floating sticky checkout button if cart has items
                            PosCatalogList(
                                products = filteredProducts,
                                isWholesale = isWholesale,
                                onAddItem = onAddItem,
                                contentBottomPadding = if (cart.isNotEmpty()) 60.dp else 16.dp
                            )

                            if (cart.isNotEmpty()) {
                                Surface(
                                    onClick = { phoneActiveTab = 1 },
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkInk,
                                    tonalElevation = 6.dp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                        .testTag("pos_floating_cart_bar")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 9.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = GrowthEngineGold, modifier = Modifier.size(17.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${cart.size} items • ₹${MainViewModel.formatCurrencyPlain(cartGrandTotal)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "View Bill & Pay",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GrowthEngineGold
                                            )
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrowthEngineGold, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        } else {
                            // Full-width Cart
                            Surface(
                                color = SurfaceWhite,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("pos_cart_container")
                            ) {
                                PosCartContent(
                                    cart = cart,
                                    cartSubtotal = cartSubtotal,
                                    cartGst = cartGst,
                                    cartGrandTotal = cartGrandTotal,
                                    selectedPaymentMode = selectedPaymentMode,
                                    onPaymentModeSelect = { selectedPaymentMode = it },
                                    onUpdateQuantity = onUpdateQuantity,
                                    onClearCart = onClearCart,
                                    onCheckout = onCheckout
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PosCatalogList(
    products: List<ProductEntity>,
    isWholesale: Boolean,
    onAddItem: (ProductEntity) -> Unit,
    contentBottomPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = contentBottomPadding)
    ) {
        items(products) { product ->
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "SKU: ${product.sku} • HSN: ${product.hsnCode}",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Stock: ${product.currentStock.toInt()} ${product.unit}",
                            fontSize = 10.sp,
                            color = if (product.currentStock <= product.minReorderLevel) ErrorRedDark else SuccessGreenDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

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
}

@Composable
private fun PosCartContent(
    cart: List<PosCartItem>,
    cartSubtotal: Double,
    cartGst: Double,
    cartGrandTotal: Double,
    selectedPaymentMode: String,
    onPaymentModeSelect: (String) -> Unit,
    onUpdateQuantity: (Long, Double) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: (String) -> Unit
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
                    Text("Tap items in catalog to add", fontSize = 10.sp, color = TextTertiary)
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
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                    onClick = { onPaymentModeSelect(mode) },
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
            enabled = cart.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreenDark),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("pos_checkout_button")
        ) {
            Text("Settle Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
