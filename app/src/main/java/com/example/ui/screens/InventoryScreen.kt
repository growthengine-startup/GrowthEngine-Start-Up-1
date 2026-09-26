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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
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
            FloatingActionButton(
                onClick = onAddNewProduct,
                containerColor = ImperialNavy,
                contentColor = RoyalTeakGoldLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_product")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add SKU", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        },
        containerColor = WarmIvoryBackground,
        modifier = Modifier.testTag("inventory_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Inventory Valuation Metric Strip
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
                            text = "TOTAL STOCK VALUATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "₹ ${MainViewModel.formatCurrencyPlain(totalStockValuation)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ImperialNavy
                        )
                    }

                    Surface(
                        color = if (lowStockCount > 0) TerracottaRedContainer else ForestEmeraldContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (lowStockCount > 0) Icons.Default.Warning else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (lowStockCount > 0) TerracottaRed else ForestEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$lowStockCount Low Stock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (lowStockCount > 0) TerracottaRed else ForestEmerald
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by SKU, Name, HSN or Category...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondaryMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("inventory_search_input"),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImperialNavy,
                    unfocusedBorderColor = WarmIvoryBorder,
                    focusedContainerColor = WarmIvorySurface,
                    unfocusedContainerColor = WarmIvorySurface
                )
            )

            // Stock Items Register
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { product ->
                    val isLowStock = product.currentStock <= product.minReorderLevel
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLowStock) TerracottaRed.copy(alpha = 0.5f) else WarmIvoryBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("product_card_${product.sku}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = product.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                    Text(
                                        text = "SKU: ${product.sku} • HSN: ${product.hsnCode} • Cat: ${product.category}",
                                        fontSize = 10.sp,
                                        color = TextSecondaryMuted
                                    )
                                }

                                StatusBadge(status = if (isLowStock) "LOW_STOCK" else "IN_STOCK")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(WarmIvorySurfaceVariant, RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Current Balance:", fontSize = 9.sp, color = TextSecondaryMuted)
                                    Text(
                                        text = "${product.currentStock.toInt()} ${product.unit}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLowStock) TerracottaRed else TextPrimaryDark
                                    )
                                    Text("Min Reorder: ${product.minReorderLevel.toInt()}", fontSize = 9.sp, color = TextSecondaryMuted)
                                }

                                Column {
                                    Text("Wholesale Rate:", fontSize = 9.sp, color = TextSecondaryMuted)
                                    Text("₹${MainViewModel.formatCurrencyPlain(product.wholesalePrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ImperialNavy)
                                    Text("Cost: ₹${MainViewModel.formatCurrencyPlain(product.purchasePrice)}", fontSize = 9.sp, color = TextSecondaryMuted)
                                }

                                Button(
                                    onClick = { onAdjustStock(product) },
                                    colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("btn_adjust_stock_${product.sku}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Adjust / GRN", fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Preferred Vendor: ${product.preferredSupplier}",
                                fontSize = 9.sp,
                                color = TextSecondaryMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
