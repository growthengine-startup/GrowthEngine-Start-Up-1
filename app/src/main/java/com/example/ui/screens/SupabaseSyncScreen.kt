package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabase.SupabaseService
import com.example.supabase.SupabaseSyncState
import com.example.ui.theme.*

@Composable
fun SupabaseSyncScreen(
    supabaseService: SupabaseService,
    syncState: SupabaseSyncState,
    statusMessage: String,
    invoicesCount: Int,
    customersCount: Int,
    productsCount: Int,
    expensesCount: Int,
    onSyncNow: () -> Unit,
    onTestConnection: () -> Unit
) {
    val context = LocalContext.current
    var projectIdText by remember { mutableStateOf(supabaseService.projectId) }
    var urlText by remember { mutableStateOf(supabaseService.supabaseUrl) }
    var keyText by remember { mutableStateOf(supabaseService.supabaseAnonKey) }
    var autoSync by remember { mutableStateOf(supabaseService.isAutoSyncEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("supabase_sync_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Supabase Cloud",
                                tint = SuccessGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Supabase Cloud Integration",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "PostgreSQL Local-First Enterprise Sync",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (syncState) {
                            SupabaseSyncState.SYNCED -> SuccessGreenContainer
                            SupabaseSyncState.SYNCING -> GrowthEngineGoldContainer
                            SupabaseSyncState.ERROR -> ErrorRedContainer
                            else -> SurfaceSubtle
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (syncState) {
                                SupabaseSyncState.SYNCED -> SuccessGreen
                                SupabaseSyncState.SYNCING -> GrowthEngineGold
                                SupabaseSyncState.ERROR -> ErrorRed
                                else -> BorderSubtle
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (syncState) {
                                            SupabaseSyncState.SYNCED -> SuccessGreen
                                            SupabaseSyncState.SYNCING -> GrowthEngineGoldDark
                                            SupabaseSyncState.ERROR -> ErrorRed
                                            else -> TextTertiary
                                        }
                                    )
                            )
                            Text(
                                text = when (syncState) {
                                    SupabaseSyncState.SYNCED -> "ONLINE SYNCED"
                                    SupabaseSyncState.SYNCING -> "SYNCING..."
                                    SupabaseSyncState.ERROR -> "OFFLINE / LOCAL"
                                    else -> "STANDBY"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSyncNow,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("btn_sync_now")
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync All Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onTestConnection,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.weight(1f).testTag("btn_test_connection")
                    ) {
                        Text("Test Ping", color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Cloud Replicated Records Count
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REPLICATED TABLES IN SUPABASE SCHEMA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Invoices Table (invoices):", fontSize = 12.sp, color = TextPrimary)
                    Text("$invoicesCount records", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Customers & Khata (customers):", fontSize = 12.sp, color = TextPrimary)
                    Text("$customersCount parties", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Products & Inventory (products):", fontSize = 12.sp, color = TextPrimary)
                    Text("$productsCount SKUs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Expense Register (expenses):", fontSize = 12.sp, color = TextPrimary)
                    Text("$expensesCount vouchers", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkInk)
                }
            }
        }

        // Supabase Project Credentials Configuration
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SUPABASE PROJECT CREDENTIALS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Configure your cloud Supabase database URL and public anon key",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = projectIdText,
                    onValueChange = {
                        projectIdText = it
                        if (it.isNotBlank() && (urlText.isBlank() || urlText.contains("supabase.co"))) {
                            urlText = "https://${it.trim()}.supabase.co"
                        }
                    },
                    label = { Text("Supabase Project ID") },
                    placeholder = { Text("e.g. wrcuondcuuwkqcgtrigz") },
                    modifier = Modifier.fillMaxWidth().testTag("input_supabase_project_id"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    modifier = Modifier.fillMaxWidth().testTag("input_supabase_url"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Supabase Public Anon Key (apikey)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_supabase_key"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkInk,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-Sync on Changes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Background PostgREST replication", fontSize = 10.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = autoSync,
                        onCheckedChange = {
                            autoSync = it
                            supabaseService.isAutoSyncEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GrowthEngineGold,
                            checkedTrackColor = DarkInk
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        supabaseService.projectId = projectIdText
                        supabaseService.supabaseUrl = urlText
                        supabaseService.supabaseAnonKey = keyText
                        Toast.makeText(context, "Supabase project configuration saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_supabase_settings")
                ) {
                    Text("Save Supabase Configuration", color = Color(0xFF141414), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Supabase SQL Editor Script Card
        val clipboardManager = LocalClipboardManager.current
        val sqlScript = """
-- GrowthEngine MSME Cloud Database Schema
-- Supabase Project: wrcuondcuuwkqcgtrigz

-- 1. Invoices Table
CREATE TABLE IF NOT EXISTS public.invoices (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    invoice_number TEXT NOT NULL UNIQUE,
    invoice_type TEXT DEFAULT 'TAX_INVOICE',
    party_id BIGINT,
    party_name TEXT,
    party_gstin TEXT,
    party_phone TEXT,
    party_state TEXT,
    is_inter_state BOOLEAN DEFAULT false,
    items_summary TEXT,
    items_count INT DEFAULT 1,
    subtotal NUMERIC DEFAULT 0.0,
    discount NUMERIC DEFAULT 0.0,
    cgst_amount NUMERIC DEFAULT 0.0,
    sgst_amount NUMERIC DEFAULT 0.0,
    igst_amount NUMERIC DEFAULT 0.0,
    total_amount NUMERIC DEFAULT 0.0,
    amount_paid NUMERIC DEFAULT 0.0,
    balance_due NUMERIC DEFAULT 0.0,
    payment_status TEXT DEFAULT 'PAID',
    payment_mode TEXT DEFAULT 'UPI',
    e_way_bill_number TEXT,
    notes TEXT,
    created_at BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint
);

-- 2. Parties Table (Customers & Suppliers)
CREATE TABLE IF NOT EXISTS public.parties (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL,
    trade_name TEXT,
    type TEXT DEFAULT 'CUSTOMER',
    gstin TEXT,
    pan_number TEXT,
    phone TEXT,
    email TEXT,
    address TEXT,
    state_name TEXT DEFAULT 'Tamil Nadu',
    state_code TEXT DEFAULT '33',
    credit_limit NUMERIC DEFAULT 500000.0,
    outstanding_balance NUMERIC DEFAULT 0.0,
    payment_terms_days INT DEFAULT 30,
    overdue_days INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 3. Products Table
CREATE TABLE IF NOT EXISTS public.products (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL,
    sku TEXT UNIQUE,
    hsn_code TEXT,
    category TEXT,
    unit TEXT DEFAULT 'Pcs',
    purchase_price NUMERIC DEFAULT 0.0,
    wholesale_price NUMERIC DEFAULT 0.0,
    mrp NUMERIC DEFAULT 0.0,
    gst_rate_percent NUMERIC DEFAULT 18.0,
    current_stock NUMERIC DEFAULT 0.0,
    min_reorder_level NUMERIC DEFAULT 10.0,
    preferred_supplier TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 4. Expenses Table
CREATE TABLE IF NOT EXISTS public.expenses (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    title TEXT NOT NULL,
    category TEXT DEFAULT 'General',
    amount NUMERIC DEFAULT 0.0,
    is_gst_claimable BOOLEAN DEFAULT false,
    gst_amount NUMERIC DEFAULT 0.0,
    payment_mode TEXT DEFAULT 'UPI',
    vendor_name TEXT,
    date_epoch BIGINT DEFAULT (extract(epoch from now()) * 1000)::bigint
);

-- 5. Row Level Security & Anon Permissions
ALTER TABLE public.invoices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.parties ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.expenses ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow anon all on invoices" ON public.invoices FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon all on parties" ON public.parties FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon all on products" ON public.products FOR ALL TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon all on expenses" ON public.expenses FOR ALL TO anon USING (true) WITH CHECK (true);
""".trimIndent()

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSubtle),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth().testTag("supabase_sql_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SQL SCRIPT FOR SUPABASE SQL EDITOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Run this in your project SQL Editor to create tables",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(sqlScript))
                            Toast.makeText(context, "SQL Script copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkInk),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_copy_sql")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy SQL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = DarkInk,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sqlScript,
                        color = Color(0xFFF1F5F9),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
