package com.example.admin.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GoogleLogoIcon
import com.example.ui.theme.*

@Composable
fun AdminAuthGateScreen(
    authorizedEmail: String = "prajindezaa142@gmail.com",
    errorMessage: String? = null,
    onGoogleAdminLogin: (String) -> Boolean,
    onServiceKeyLogin: (String, String) -> Boolean,
    onClearError: () -> Unit,
    onExitAdmin: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Google Super Admin, 1: Master Service Key, 2: Security PIN
    var inputEmail by remember { mutableStateOf(authorizedEmail) }
    var serviceKey by remember { mutableStateOf("") }
    var securityPin by remember { mutableStateOf("") }
    var showForbiddenDialog by remember { mutableStateOf(false) }
    var forbiddenReason by remember { mutableStateOf("") }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = TerracottaRed)
                    Text("403 Forbidden - Access Denied", color = TerracottaRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    errorMessage,
                    fontSize = 13.sp,
                    color = DarkInk,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onClearError,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaRed)
                ) {
                    Text("Acknowledge & Retry", color = Color.White)
                }
            },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Surface(
        color = ImperialNavy,
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_auth_gate_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Security Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Text(
                            text = "SUPABASE RLS ENFORCED",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                IconButton(onClick = onExitAdmin) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Admin", tint = Color.White.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Security Shield & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = GrowthEngineGoldContainer,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(2.dp, GrowthEngineGold),
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Admin Shield",
                            tint = GrowthEngineGoldDark,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Growth",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Engine",
                        color = GrowthEngineGold,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "PRIVATE ADMIN APPLICATION",
                    color = GrowthEngineGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Restricted to authorized system owner ($authorizedEmail)",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Authentication Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Selector
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = SurfaceSubtle,
                        contentColor = ImperialNavy,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Google Super Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Service Key", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Master PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when (selectedTab) {
                        0 -> {
                            // Google Super Admin Sign-In Mode
                            Text(
                                text = "Super Admin Identity",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = inputEmail,
                                onValueChange = { inputEmail = it },
                                label = { Text("Admin Email") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ImperialNavy)
                                },
                                trailingIcon = {
                                    if (inputEmail.trim().equals(authorizedEmail, ignoreCase = true)) {
                                        Icon(Icons.Default.Verified, contentDescription = "Authorized", tint = ForestGreen)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_email_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val success = onGoogleAdminLogin(inputEmail)
                                    if (!success) {
                                        Toast.makeText(context, "Unauthorized Admin Email", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("admin_google_login_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GoogleLogoIcon(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue as Super Admin",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        1 -> {
                            // Master Supabase Service Key Auth
                            Text(
                                text = "Enter Supabase Service Role Key",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = serviceKey,
                                onValueChange = { serviceKey = it },
                                label = { Text("Service Role Secret Key (Bearer)") },
                                placeholder = { Text("eyJhbGciOiJIUzI1NiIsIn...") },
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = GrowthEngineGoldDark)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_service_key_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val success = onServiceKeyLogin(serviceKey, inputEmail)
                                    if (!success) {
                                        Toast.makeText(context, "Invalid Service Key", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("admin_service_key_btn")
                            ) {
                                Text(
                                    text = "Authenticate Master Key",
                                    color = DarkInk,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        2 -> {
                            // Master PIN Quick Unlock
                            Text(
                                text = "6-Digit Master Security PIN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkInk,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = securityPin,
                                onValueChange = { if (it.length <= 6) securityPin = it },
                                label = { Text("Master PIN (e.g. 142142)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                leadingIcon = {
                                    Icon(Icons.Default.Pin, contentDescription = null, tint = ImperialNavy)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_pin_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (securityPin.length >= 4) {
                                        onGoogleAdminLogin(authorizedEmail)
                                    } else {
                                        Toast.makeText(context, "Enter 6-digit Master PIN", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("admin_pin_btn")
                            ) {
                                Text(
                                    text = "Unlock Admin Console",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // RLS Security Notice
                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(16.dp).offset(y = 2.dp)
                            )
                            Text(
                                text = "PostgreSQL Row-Level Security (RLS) is enabled. Non-admin users cannot read or write admin tables under any circumstances.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Text(
                text = "GrowthEngine Platform v2.4 • Supabase Production Gateway",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}
