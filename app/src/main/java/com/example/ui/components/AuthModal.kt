package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier.size(20.dp)) {
    // Multi-color Google 'G' Icon representation
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "G",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color(0xFF4285F4)
                )
            }
        }
    }
}

@Composable
fun AuthModal(
    initialBusinessName: String,
    initialRegion: String,
    onDismiss: () -> Unit,
    onGoogleLoginSuccess: (email: String, name: String, bName: String, region: String) -> Unit,
    onPasswordLoginSuccess: (businessName: String, region: String, email: String) -> Unit
) {
    val context = LocalContext.current
    var isSignUpMode by remember { mutableStateOf(false) }
    var businessNameText by remember { mutableStateOf(initialBusinessName) }
    var phoneOrEmail by remember { mutableStateOf("prajindezaa142@gmail.com") }
    var stateName by remember { mutableStateOf("Tamil Nadu") }
    var isGstRegistered by remember { mutableStateOf(true) }
    var gstinText by remember { mutableStateOf("33AAACD9821K1Z4") }
    var showGoogleAccountPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("auth_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GrowthEngineLogo()
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    text = if (isSignUpMode) "Launch Your MSME Ledger" else "Sign in to GrowthEngine",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Single sign-on persists permanently. You only need to log in once.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                // -------------------------------------------------------------
                // PRIMARY: CONTINUE WITH GOOGLE BUTTON (MANDATORY & PROMINENT)
                // -------------------------------------------------------------
                Surface(
                    onClick = {
                        showGoogleAccountPicker = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = BackgroundWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_continue_with_google")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoogleLogoIcon(modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color(0xFF3C4043),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (showGoogleAccountPicker) {
                    // Google Account Picker Dialog Simulator
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Choose a Google Account",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImperialNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Option 1: App Owner / Super Admin
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        Toast.makeText(context, "Authenticated as App Owner (Super Admin)", Toast.LENGTH_SHORT).show()
                                        onGoogleLoginSuccess(
                                            "prajindezaa142@gmail.com",
                                            "Prajin Dezaa",
                                            "Dezaa Enterprises",
                                            "GSTIN: 33AAACD9821K1Z4 · Tamil Nadu (33)"
                                        )
                                    }
                                    .background(BackgroundWhite)
                                    .border(1.dp, GrowthEngineGoldBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = ImperialNavy,
                                        shape = CircleShape,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                    Column {
                                        Text("Prajin Dezaa", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkInk)
                                        Text("prajindezaa142@gmail.com", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                                Surface(
                                    color = GrowthEngineGoldContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("APP OWNER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GrowthEngineGoldDark, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Option 2: General Business User Account
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        Toast.makeText(context, "Authenticated as Business User", Toast.LENGTH_SHORT).show()
                                        onGoogleLoginSuccess(
                                            "kalyan.works@midcpune.in",
                                            "Kalyan Industries",
                                            "Kalyan Industrial Works",
                                            "GSTIN: 27AABCK4829K1Z5 · Maharashtra (27)"
                                        )
                                    }
                                    .background(BackgroundWhite)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = ElectricBlue,
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("K", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Kalyan Industries", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkInk)
                                    Text("kalyan.works@midcpune.in", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }

                // Divider: OR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                    Text(
                        text = "OR USE EMAIL / GSTIN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
                }

                // Business Name Field
                OutlinedTextField(
                    value = businessNameText,
                    onValueChange = { businessNameText = it },
                    label = { Text("Business / Store Name") },
                    placeholder = { Text("e.g. Dezaa Enterprises") },
                    modifier = Modifier.fillMaxWidth().testTag("auth_input_business"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImperialNavy,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                // Mobile / Email Field
                OutlinedTextField(
                    value = phoneOrEmail,
                    onValueChange = { phoneOrEmail = it },
                    label = { Text("Email Address or Mobile") },
                    modifier = Modifier.fillMaxWidth().testTag("auth_input_phone"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImperialNavy,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                // State Field
                OutlinedTextField(
                    value = stateName,
                    onValueChange = { stateName = it },
                    label = { Text("State (e.g. Tamil Nadu, Maharashtra)") },
                    modifier = Modifier.fillMaxWidth().testTag("auth_input_state"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImperialNavy,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                // GST Registration Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmIvorySurface, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("GST Registered Business?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(if (isGstRegistered) "Regular GST Taxpayer" else "Unregistered / Composition", fontSize = 10.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = isGstRegistered,
                        onCheckedChange = { isGstRegistered = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GrowthEngineGoldDark,
                            checkedTrackColor = ImperialNavy
                        )
                    )
                }

                if (isGstRegistered) {
                    OutlinedTextField(
                        value = gstinText,
                        onValueChange = { gstinText = it },
                        label = { Text("GSTIN (15 Digits)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Submit Button
                Button(
                    onClick = {
                        val name = if (businessNameText.isNotBlank()) businessNameText else "Dezaa Enterprises"
                        val regTag = if (isGstRegistered) "GSTIN: $gstinText · $stateName" else "Unregistered · $stateName"
                        Toast.makeText(context, "Welcome, $name!", Toast.LENGTH_SHORT).show()
                        onPasswordLoginSuccess(name, regTag, phoneOrEmail)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_btn")
                ) {
                    Text(
                        text = "Enter GrowthEngine",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Single Session Auto-Login · Supabase Cloud Encrypted",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
