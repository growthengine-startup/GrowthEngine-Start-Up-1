package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Brush
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
import com.example.supabase.GoogleSignInHelper
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

// ── Custom styled text field colors ──────────────────────────────────────────
@Composable
private fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GrowthEngineGold,
    unfocusedBorderColor = Color(0xFFDDE2E8),
    focusedLabelColor = GrowthEngineGoldDark,
    unfocusedLabelColor = TextSecondary,
    cursorColor = GrowthEngineGoldDark,
    focusedLeadingIconColor = GrowthEngineGoldDark,
    unfocusedLeadingIconColor = TextTertiary,
    focusedTrailingIconColor = TextSecondary,
    unfocusedTrailingIconColor = TextTertiary,
    focusedContainerColor = Color(0xFFFCFCFD),
    unfocusedContainerColor = Color(0xFFFAFBFC)
)

// ── OR Divider ───────────────────────────────────────────────────────────────
@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
        Text(
            text = "  OR  ",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextTertiary,
            letterSpacing = 1.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
    }
}

// ── Password strength indicator ──────────────────────────────────────────────
@Composable
private fun PasswordStrengthIndicator(password: String) {
    if (password.isEmpty()) return

    val strength = remember(password) {
        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        score
    }

    val (label, color) = when {
        strength <= 1 -> "Weak" to Color(0xFFEF4444)
        strength == 2 -> "Fair" to Color(0xFFF59E0B)
        strength == 3 -> "Good" to Color(0xFF3B82F6)
        else -> "Strong" to Color(0xFF10B981)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(4) { index ->
                val segmentColor by animateColorAsState(
                    targetValue = if (index < strength) color else Color(0xFFE5E7EB),
                    animationSpec = tween(300),
                    label = "strength_color_$index"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segmentColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Password strength: $label",
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
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
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val isValidEmail = remember(emailInput) {
        emailInput.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches()
    }

    val googleSignInHelper = remember { GoogleSignInHelper(context) }
    val activity = context as? Activity

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 480.dp)
                .heightIn(max = 720.dp)
                .imePadding()
                .padding(vertical = 8.dp)
                .testTag("auth_modal")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // ── Premium Header with subtle gradient ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFEF9EF),
                                    Color.White
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GrowthEngineLogo()
                        Surface(
                            onClick = onDismiss,
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // ── Scrollable Content ───────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Auth Tabs - only show for Sign In and Sign Up
                    if (authMode != AuthMode.FORGOT_PASSWORD) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                AuthMode.SIGN_IN to "Sign In",
                                AuthMode.SIGN_UP to "Create Account"
                            ).forEach { (mode, label) ->
                                val isSelected = authMode == mode
                                val bgColor by animateColorAsState(
                                    targetValue = if (isSelected) Color.White else Color.Transparent,
                                    animationSpec = tween(200),
                                    label = "tab_bg_$label"
                                )
                                val textColor by animateColorAsState(
                                    targetValue = if (isSelected) DarkInk else TextTertiary,
                                    animationSpec = tween(200),
                                    label = "tab_text_$label"
                                )

                                Surface(
                                    onClick = {
                                        authMode = mode
                                        errorMessage = null
                                        successMessage = null
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = bgColor,
                                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
                                        )
                                    }
                                }
                                if (mode == AuthMode.SIGN_IN) Spacer(modifier = Modifier.width(3.dp))
                            }
                        }
                    }

                    // Title & Description
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = when (authMode) {
                                AuthMode.SIGN_IN -> "Welcome Back"
                                AuthMode.SIGN_UP -> "Create Your Account"
                                AuthMode.FORGOT_PASSWORD -> "Reset Password"
                            },
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (authMode) {
                                AuthMode.SIGN_IN -> "Sign in to access your cloud-synced ledger & GST invoices."
                                AuthMode.SIGN_UP -> "Join GrowthEngine for automated GST billing, stock management & AI copilot."
                                AuthMode.FORGOT_PASSWORD -> "Enter your registered email and we'll send a password reset link."
                            },
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }

                    // Error Message Banner
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
                    ) {
                        errorMessage?.let { msg ->
                            Surface(
                                color = Color(0xFFFEF2F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        msg,
                                        color = Color(0xFFDC2626),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Success Message Banner
                    AnimatedVisibility(
                        visible = successMessage != null,
                        enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
                    ) {
                        successMessage?.let { msg ->
                            Surface(
                                color = Color(0xFFECFDF5),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        msg,
                                        color = Color(0xFF059669),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // ═══════════════════════════════════════════════════════════
                    // SIGN IN / SIGN UP / RESET FIELDS
                    // ═══════════════════════════════════════════════════════════
                    when (authMode) {
                        AuthMode.SIGN_IN -> {
                            // ── Continue with Google ──────────────────────────
                            OutlinedButton(
                                onClick = {
                                    if (activity == null) {
                                        errorMessage = "Unable to launch Google Sign-In from this context."
                                        return@OutlinedButton
                                    }
                                    isGoogleLoading = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        when (val result = googleSignInHelper.signInWithGoogle(activity)) {
                                            is AuthResult.Success -> {
                                                isGoogleLoading = false
                                                val session = result.data
                                                val bName = session.businessName.ifBlank { "My Enterprise" }
                                                val reg = if (session.gstin.isNotBlank()) "GSTIN: ${session.gstin} · ${session.state}" else "State: ${session.state}"
                                                Toast.makeText(context, "Welcome, ${session.fullName.ifBlank { session.email }}!", Toast.LENGTH_SHORT).show()
                                                onAuthSuccess(session.email, session.fullName, bName, reg)
                                            }
                                            is AuthResult.Error -> {
                                                isGoogleLoading = false
                                                if (result.errorCode != "USER_CANCELLED") {
                                                    errorMessage = result.errorMessage
                                                }
                                            }
                                            else -> {
                                                isGoogleLoading = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isLoading && !isGoogleLoading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_google_btn")
                            ) {
                                if (isGoogleLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFF4285F4),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        GoogleLogoIcon(modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Continue with Google",
                                            color = Color(0xFF3C4043),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            OrDivider()

                            // ── Email Field ──────────────────────────────────
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    errorMessage = null
                                },
                                label = { Text("Email Address") },
                                placeholder = { Text("e.g. owner@mybusiness.in") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors(),
                                modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                            )

                            // ── Password Field ───────────────────────────────
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    errorMessage = null
                                },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility"
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
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors(),
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
                                    color = GrowthEngineGoldDark,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            authMode = AuthMode.FORGOT_PASSWORD
                                            errorMessage = null
                                            successMessage = null
                                        }
                                        .padding(vertical = 2.dp)
                                )
                            }

                            // ── Submit Button ────────────────────────────────
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkInk,
                                    contentColor = Color.White,
                                    disabledContainerColor = DarkInk.copy(alpha = 0.5f),
                                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_submit_btn")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Sign In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        AuthMode.SIGN_UP -> {
                            // ── Continue with Google (Sign Up) ───────────────
                            OutlinedButton(
                                onClick = {
                                    if (activity == null) {
                                        errorMessage = "Unable to launch Google Sign-In from this context."
                                        return@OutlinedButton
                                    }
                                    isGoogleLoading = true
                                    errorMessage = null
                                    coroutineScope.launch {
                                        when (val result = googleSignInHelper.signInWithGoogle(activity)) {
                                            is AuthResult.Success -> {
                                                isGoogleLoading = false
                                                val session = result.data
                                                val bName = session.businessName.ifBlank { "My Enterprise" }
                                                val reg = if (session.gstin.isNotBlank()) "GSTIN: ${session.gstin} · ${session.state}" else "State: ${session.state}"
                                                Toast.makeText(context, "Welcome, ${session.fullName.ifBlank { session.email }}!", Toast.LENGTH_SHORT).show()
                                                onAuthSuccess(session.email, session.fullName, bName, reg)
                                            }
                                            is AuthResult.Error -> {
                                                isGoogleLoading = false
                                                if (result.errorCode != "USER_CANCELLED") {
                                                    errorMessage = result.errorMessage
                                                }
                                            }
                                            else -> {
                                                isGoogleLoading = false
                                            }
                                        }
                                    }
                                },
                                enabled = !isLoading && !isGoogleLoading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_google_signup_btn")
                            ) {
                                if (isGoogleLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFF4285F4),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        GoogleLogoIcon(modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Continue with Google",
                                            color = Color(0xFF3C4043),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            OrDivider()

                            // ── Full Name ────────────────────────────────────
                            OutlinedTextField(
                                value = fullNameInput,
                                onValueChange = { fullNameInput = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Ramesh Kumar") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // ── Business Name ────────────────────────────────
                            OutlinedTextField(
                                value = businessNameInput,
                                onValueChange = { businessNameInput = it },
                                label = { Text("Business / Firm Name") },
                                placeholder = { Text("e.g. Kumar Traders") },
                                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // ── Email Field ──────────────────────────────────
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    errorMessage = null
                                },
                                label = { Text("Email Address") },
                                placeholder = { Text("owner@company.in") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // ── State & GSTIN ────────────────────────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = stateInput,
                                    onValueChange = { stateInput = it },
                                    label = { Text("State") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = authTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = gstinInput,
                                    onValueChange = { gstinInput = it },
                                    label = { Text("GSTIN (Optional)") },
                                    placeholder = { Text("27AAAAA0000A1Z5") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = authTextFieldColors(),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // ── Password Field ───────────────────────────────
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    errorMessage = null
                                },
                                label = { Text("Password (Min 6 chars)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password"
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Password Strength Indicator
                            PasswordStrengthIndicator(password = passwordInput)

                            // ── Confirm Password ─────────────────────────────
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                        Icon(
                                            imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle confirm password"
                                        )
                                    }
                                },
                                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // ── Register Button ──────────────────────────────
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrowthEngineGoldDark,
                                    contentColor = Color.White,
                                    disabledContainerColor = GrowthEngineGoldDark.copy(alpha = 0.5f),
                                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_signup_submit_btn")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Create Account & Launch",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
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
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = authTextFieldColors(),
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkInk,
                                    contentColor = Color.White,
                                    disabledContainerColor = DarkInk.copy(alpha = 0.5f),
                                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Send Reset Link",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
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
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = GrowthEngineGoldDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Back to Sign In",
                                    color = GrowthEngineGoldDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ── Bottom Section (pinned outside scroll) ───────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Guest / Offline Mode Option
                    OutlinedButton(
                        onClick = { onGuestContinue() },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFAFBFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("auth_guest_btn")
                    ) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Continue in Offline Demo Mode",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Security notice footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Encrypted JWT · Row-Level Security · SOC 2 Compliant",
                            fontSize = 10.sp,
                            color = TextTertiary,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }
        }
    }
}
