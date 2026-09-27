package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.supabase.AuthResult
import com.example.supabase.SupabaseAuthService
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier.size(20.dp)) {
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

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

@Composable
fun AuthModal(
    initialBusinessName: String,
    initialRegion: String,
    onDismiss: () -> Unit,
    onAuthSuccess: (email: String, fullName: String, businessName: String, region: String) -> Unit,
    onGuestContinue: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val authService = remember { SupabaseAuthService(context) }

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Form inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var fullNameInput by remember { mutableStateOf("") }
    var businessNameInput by remember { mutableStateOf(initialBusinessName.ifBlank { "" }) }
    var stateInput by remember { mutableStateOf("Maharashtra") }
    var gstinInput by remember { mutableStateOf("") }

    // UI state
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val isValidEmail = remember(emailInput) {
        emailInput.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 480.dp)
                .heightIn(max = 700.dp)
                .imePadding()
                .padding(vertical = 12.dp)
                .testTag("auth_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Row
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

                // Auth Tabs Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmIvorySurface, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        onClick = {
                            authMode = AuthMode.SIGN_IN
                            errorMessage = null
                            successMessage = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (authMode == AuthMode.SIGN_IN) BackgroundWhite else Color.Transparent,
                        tonalElevation = if (authMode == AuthMode.SIGN_IN) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Sign In",
                                fontSize = 13.sp,
                                fontWeight = if (authMode == AuthMode.SIGN_IN) FontWeight.Bold else FontWeight.Medium,
                                color = if (authMode == AuthMode.SIGN_IN) DarkInk else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        onClick = {
                            authMode = AuthMode.SIGN_UP
                            errorMessage = null
                            successMessage = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (authMode == AuthMode.SIGN_UP) BackgroundWhite else Color.Transparent,
                        tonalElevation = if (authMode == AuthMode.SIGN_UP) 2.dp else 0.dp,
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "Create Account",
                                fontSize = 13.sp,
                                fontWeight = if (authMode == AuthMode.SIGN_UP) FontWeight.Bold else FontWeight.Medium,
                                color = if (authMode == AuthMode.SIGN_UP) DarkInk else TextSecondary
                            )
                        }
                    }
                }

                // Title & Description
                Column {
                    Text(
                        text = when (authMode) {
                            AuthMode.SIGN_IN -> "Welcome Back"
                            AuthMode.SIGN_UP -> "Setup MSME Cloud Account"
                            AuthMode.FORGOT_PASSWORD -> "Reset Your Password"
                        },
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = when (authMode) {
                            AuthMode.SIGN_IN -> "Sign in to access your cloud synchronized ledger & GST invoices."
                            AuthMode.SIGN_UP -> "Join GrowthEngine for automated GST billing, stock management & AI copilot."
                            AuthMode.FORGOT_PASSWORD -> "Enter your registered email and we will send a password reset link."
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }

                // Error Message Banner
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Surface(
                            color = Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg, color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Success Message Banner
                AnimatedVisibility(visible = successMessage != null) {
                    successMessage?.let { msg ->
                        Surface(
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(msg, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // SIGN IN / SIGN UP / RESET FIELDS
                when (authMode) {
                    AuthMode.SIGN_IN -> {
                        // Email Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            label = { Text("Email Address") },
                            placeholder = { Text("e.g. owner@mybusiness.in") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )

                        // Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        // Forgot Password Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ImperialNavy,
                                modifier = Modifier
                                    .clickable {
                                        authMode = AuthMode.FORGOT_PASSWORD
                                        errorMessage = null
                                        successMessage = null
                                    }
                                    .padding(vertical = 2.dp)
                            )
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (!isValidEmail) {
                                    errorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                if (passwordInput.isBlank()) {
                                    errorMessage = "Please enter your password."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    when (val result = authService.signIn(emailInput, passwordInput)) {
                                        is AuthResult.Success -> {
                                            isLoading = false
                                            val session = result.data
                                            val bName = session.businessName.ifBlank { "My Enterprise" }
                                            val reg = if (session.gstin.isNotBlank()) "GSTIN: ${session.gstin} · ${session.state}" else "State: ${session.state}"
                                            Toast.makeText(context, "Welcome back, ${session.fullName.ifBlank { session.email }}!", Toast.LENGTH_SHORT).show()
                                            onAuthSuccess(session.email, session.fullName, bName, reg)
                                        }
                                        is AuthResult.Error -> {
                                            isLoading = false
                                            errorMessage = result.errorMessage
                                        }
                                        else -> {
                                            isLoading = false
                                            errorMessage = "Sign in failed."
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_btn")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    AuthMode.SIGN_UP -> {
                        // Full Name
                        OutlinedTextField(
                            value = fullNameInput,
                            onValueChange = { fullNameInput = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Ramesh Kumar") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Business Name
                        OutlinedTextField(
                            value = businessNameInput,
                            onValueChange = { businessNameInput = it },
                            label = { Text("Business / Firm Name") },
                            placeholder = { Text("e.g. Kumar Traders") },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Email Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            label = { Text("Email Address") },
                            placeholder = { Text("owner@company.in") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // State & GSTIN
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = stateInput,
                                onValueChange = { stateInput = it },
                                label = { Text("State") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = gstinInput,
                                onValueChange = { gstinInput = it },
                                label = { Text("GSTIN (Optional)") },
                                placeholder = { Text("27AAAAA0000A1Z5") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("Password (Min 6 chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Confirm Password Field
                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = {
                                confirmPasswordInput = it
                                errorMessage = null
                            },
                            label = { Text("Confirm Password") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = TextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle confirm password",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Register Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (fullNameInput.isBlank() || businessNameInput.isBlank()) {
                                    errorMessage = "Please enter your name and business name."
                                    return@Button
                                }
                                if (!isValidEmail) {
                                    errorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                if (passwordInput.length < 6) {
                                    errorMessage = "Password must be at least 6 characters long."
                                    return@Button
                                }
                                if (passwordInput != confirmPasswordInput) {
                                    errorMessage = "Passwords do not match."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    when (val result = authService.signUp(
                                        email = emailInput,
                                        password = passwordInput,
                                        fullName = fullNameInput,
                                        businessName = businessNameInput,
                                        state = stateInput,
                                        gstin = gstinInput
                                    )) {
                                        is AuthResult.Success -> {
                                            isLoading = false
                                            val session = result.data
                                            val bName = session.businessName.ifBlank { businessNameInput }
                                            val reg = if (session.gstin.isNotBlank()) "GSTIN: ${session.gstin} · ${session.state}" else "State: ${session.state}"
                                            Toast.makeText(context, "Account created! Welcome to GrowthEngine.", Toast.LENGTH_LONG).show()
                                            onAuthSuccess(session.email, session.fullName, bName, reg)
                                        }
                                        is AuthResult.EmailVerificationRequired -> {
                                            isLoading = false
                                            successMessage = result.message
                                            authMode = AuthMode.SIGN_IN
                                        }
                                        is AuthResult.Error -> {
                                            isLoading = false
                                            errorMessage = result.errorMessage
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthEngineGoldDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Create Account & Launch", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }

                    AuthMode.FORGOT_PASSWORD -> {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            label = { Text("Registered Email Address") },
                            placeholder = { Text("e.g. owner@mybusiness.in") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (!isValidEmail) {
                                    errorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    when (val result = authService.sendPasswordReset(emailInput)) {
                                        is AuthResult.Success -> {
                                            isLoading = false
                                            successMessage = result.message
                                        }
                                        is AuthResult.Error -> {
                                            isLoading = false
                                            errorMessage = result.errorMessage
                                        }
                                        else -> {
                                            isLoading = false
                                            errorMessage = "Unable to send recovery email."
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = ImperialNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Send Password Reset Link", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        TextButton(
                            onClick = {
                                authMode = AuthMode.SIGN_IN
                                errorMessage = null
                                successMessage = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("← Back to Sign In", color = ImperialNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BorderSubtle)

                // Guest / Offline Mode Option
                OutlinedButton(
                    onClick = {
                        onGuestContinue()
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue in Offline Demo Mode", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                // Security notice footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Encrypted JWT Session · PostgreSQL Row-Level Security",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
