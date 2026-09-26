package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.theme.*

@Composable
fun InventoryScreen(
    products: List<ProductEntity>,
    onAdjustStock: (ProductEntity) -> Unit,
    onAddNewProduct: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val totalStockValuation = products.sumOf { it.currentStock * it.purchasePrice }
    val lowStockCount = products.count { it.currentStock <= it.minReorderLevel }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.hsnCode.contains(searchQuery)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewProduct,
                containerColor = GrowthEngineGold,
                contentColor = DarkInk,
                shape = RoundedCornerShape(12.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Item") },
                text = { Text("Add New SKU", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_product")
            )
        },
        containerColor = BackgroundWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.testTag("inventory_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Inventory Valuation Metric Strip
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
                        Column {
                            Text(
                                text = "WAREHOUSE & STOCK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthEngineGoldDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Inventory & Godowns",
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            color = if (lowStockCount > 0) ErrorRedContainer else SuccessGreenContainer,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (lowStockCount > 0) ErrorRed.copy(alpha = 0.3f) else SuccessGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (lowStockCount > 0) Icons.Default.Warning else Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (lowStockCount > 0) ErrorRedDark else SuccessGreenDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lowStockCount > 0) "$lowStockCount Low Stock" else "Stock Healthy",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lowStockCount > 0) ErrorRedDark else SuccessGreenDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Stock Valuation", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    "₹${MainViewModel.formatCurrencyPlain(totalStockValuation)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkInk
                                )
                            }
                            Text("${products.size} Total Items", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by SKU, Name, HSN or Category...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_search_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkInk,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        )
                    )
                }
            }

            // Products List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredProducts.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceWhite)
                                        .border(1.dp, BorderSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("No Inventory Items Found", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text("Add new product SKUs with purchase prices, wholesale tiers, and reorder levels.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                items(filteredProducts, key = { it.id }) { product ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth().testTag("product_card_${product.sku}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = product.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "SKU: ${product.sku} • HSN: ${product.hsnCode} • ${product.category}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Surface(
                                    color = if (product.currentStock <= product.minReorderLevel) ErrorRedContainer else SuccessGreenContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, if (product.currentStock <= product.minReorderLevel) ErrorRed else SuccessGreen)
                                ) {
                                    Text(
                                        text = "${product.currentStock.toInt()} ${product.unit}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (product.currentStock <= product.minReorderLevel) ErrorRedDark else SuccessGreenDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Purchase Cost", fontSize = 10.sp, color = TextSecondary)
                                    Text("₹${MainViewModel.formatCurrencyPlain(product.purchasePrice)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }
                                Column {
                                    Text("Wholesale Rate", fontSize = 10.sp, color = TextSecondary)
                                    Text("₹${MainViewModel.formatCurrencyPlain(product.wholesalePrice)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkInk)
                                }
                                Column {
                                    Text("MRP Rate", fontSize = 10.sp, color = TextSecondary)
                                    Text("₹${MainViewModel.formatCurrencyPlain(product.mrp)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                                }
                            }

                            HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GST Rate: ${product.gstRatePercent.toInt()}% • Min Safety Stock: ${product.minReorderLevel.toInt()}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )

                                OutlinedButton(
                                    onClick = { onAdjustStock(product) },
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_adjust_stock_${product.sku}")
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = DarkInk, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Adjust Stock", fontSize = 11.sp, color = DarkInk)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
