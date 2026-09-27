package com.example.supabase

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SupabaseUserSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val userId: String,
    val email: String,
    val fullName: String = "",
    val businessName: String = "",
    val state: String = "",
    val gstin: String = "",
    val role: String = "business_owner"
)

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T, val message: String = "") : AuthResult<T>()
    data class Error(val errorMessage: String, val errorCode: String? = null) : AuthResult<Nothing>()
    data class EmailVerificationRequired(val email: String, val message: String) : AuthResult<Nothing>()
}

class SupabaseAuthService(private val context: Context) {

    private val prefs: SharedPreferences = createEncryptedPreferences(context)

    private fun createEncryptedPreferences(ctx: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                ctx,
                "growth_engine_supabase_auth_secure",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (_: Exception) {
            ctx.getSharedPreferences("growth_engine_supabase_auth", Context.MODE_PRIVATE)
        }
    }
    private val supabaseService = SupabaseService(context)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val supabaseUrl: String
        get() = supabaseService.supabaseUrl

    val supabaseAnonKey: String
        get() = supabaseService.supabaseAnonKey

    fun getStoredSession(): SupabaseUserSession? {
        val accessToken = prefs.getString("access_token", null) ?: return null
        val refreshToken = prefs.getString("refresh_token", null) ?: return null
        val expiresAt = prefs.getLong("expires_at", 0L)
        val userId = prefs.getString("user_id", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val fullName = prefs.getString("full_name", "") ?: ""
        val businessName = prefs.getString("business_name", "") ?: ""
        val state = prefs.getString("state", "") ?: ""
        val gstin = prefs.getString("gstin", "") ?: ""
        val role = prefs.getString("role", "business_owner") ?: "business_owner"

        if (accessToken.isBlank() || userId.isBlank()) return null

        return SupabaseUserSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = expiresAt,
            userId = userId,
            email = email,
            fullName = fullName,
            businessName = businessName,
            state = state,
            gstin = gstin,
            role = role
        )
    }

    fun saveSession(session: SupabaseUserSession) {
        prefs.edit()
            .putString("access_token", session.accessToken)
            .putString("refresh_token", session.refreshToken)
            .putLong("expires_at", session.expiresAt)
            .putString("user_id", session.userId)
            .putString("email", session.email)
            .putString("full_name", session.fullName)
            .putString("business_name", session.businessName)
            .putString("state", session.state)
            .putString("gstin", session.gstin)
            .putString("role", session.role)
            .putBoolean("is_authenticated", true)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isAuthenticated(): Boolean {
        val session = getStoredSession() ?: return false
        // Allow 60 second clock skew buffer
        return session.expiresAt > (System.currentTimeMillis() / 1000 + 60)
    }

    /**
     * Sign In with Email and Password using Supabase GoTrue Auth REST API
     */
    suspend fun signIn(email: String, password: String): AuthResult<SupabaseUserSession> = withContext(Dispatchers.IO) {
        if (email.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Email and password are required.")
        }

        try {
            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/token?grant_type=password")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val accessToken = json.optString("access_token")
                val refreshToken = json.optString("refresh_token")
                val expiresIn = json.optLong("expires_in", 3600L)
                val expiresAt = (System.currentTimeMillis() / 1000) + expiresIn

                val userObj = json.optJSONObject("user") ?: JSONObject()
                val userId = userObj.optString("id", "")
                val userEmail = userObj.optString("email", email)

                val userMetadata = userObj.optJSONObject("user_metadata") ?: JSONObject()
                val fullName = userMetadata.optString("full_name", userMetadata.optString("name", ""))
                val businessName = userMetadata.optString("business_name", "")
                val state = userMetadata.optString("state", "")
                val gstin = userMetadata.optString("gstin", "")
                val role = userMetadata.optString("role", if (userEmail.equals("prajindezaa142@gmail.com", ignoreCase = true)) "super_admin" else "business_owner")

                val session = SupabaseUserSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = expiresAt,
                    userId = userId,
                    email = userEmail,
                    fullName = fullName,
                    businessName = businessName,
                    state = state,
                    gstin = gstin,
                    role = role
                )

                saveSession(session)
                AuthResult.Success(session, "Signed in successfully!")
            } else {
                val errorMsg = parseErrorMessage(responseBody, response.code)
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error during sign in: ${e.localizedMessage ?: "Unable to connect to server"}")
        }
    }

    /**
     * Sign Up / Create new Account using Supabase GoTrue Auth REST API
     */
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        businessName: String,
        state: String,
        gstin: String
    ): AuthResult<SupabaseUserSession> = withContext(Dispatchers.IO) {
        if (email.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Email and password are required.")
        }
        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
        }

        try {
            val userMetadata = JSONObject().apply {
                put("full_name", fullName.trim())
                put("business_name", businessName.trim())
                put("state", state.trim())
                put("gstin", gstin.trim().uppercase())
                put("role", if (email.trim().equals("prajindezaa142@gmail.com", ignoreCase = true)) "super_admin" else "business_owner")
            }

            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", password)
                put("data", userMetadata)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/signup")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val accessToken = json.optString("access_token", "")
                val refreshToken = json.optString("refresh_token", "")
                val expiresIn = json.optLong("expires_in", 3600L)
                val expiresAt = (System.currentTimeMillis() / 1000) + expiresIn

                val userObj = json.optJSONObject("user") ?: json
                val userId = userObj.optString("id", "")
                val userEmail = userObj.optString("email", email)

                if (accessToken.isNotBlank()) {
                    val session = SupabaseUserSession(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresAt = expiresAt,
                        userId = userId,
                        email = userEmail,
                        fullName = fullName,
                        businessName = businessName,
                        state = state,
                        gstin = gstin,
                        role = "business_owner"
                    )
                    saveSession(session)
                    AuthResult.Success(session, "Account created successfully!")
                } else {
                    // Email confirmation is enabled in Supabase project
                    AuthResult.EmailVerificationRequired(
                        email = userEmail,
                        message = "Account created! Please check your email inbox ($userEmail) to confirm your account, then sign in."
                    )
                }
            } else {
                val errorMsg = parseErrorMessage(responseBody, response.code)
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error during sign up: ${e.localizedMessage ?: "Unable to connect to server"}")
        }
    }

    /**
     * Send Password Reset Email via Supabase GoTrue Auth REST API
     */
    suspend fun sendPasswordReset(email: String): AuthResult<Boolean> = withContext(Dispatchers.IO) {
        if (email.isBlank()) {
            return@withContext AuthResult.Error("Please enter your registered email address.")
        }

        try {
            val payload = JSONObject().apply {
                put("email", email.trim().lowercase())
            }

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/recover")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful || response.code in 200..299) {
                AuthResult.Success(true, "Password recovery email sent! Check your inbox for reset instructions.")
            } else {
                val errorMsg = parseErrorMessage(responseBody, response.code)
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Unable to send recovery email: ${e.localizedMessage ?: "Network error"}")
        }
    }

    /**
     * Refresh existing session using refresh_token
     */
    suspend fun refreshSession(): AuthResult<SupabaseUserSession> = withContext(Dispatchers.IO) {
        val currentSession = getStoredSession()
        if (currentSession == null || currentSession.refreshToken.isBlank()) {
            return@withContext AuthResult.Error("No stored session available to refresh.")
        }

        try {
            val payload = JSONObject().apply {
                put("refresh_token", currentSession.refreshToken)
            }

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/token?grant_type=refresh_token")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val accessToken = json.optString("access_token")
                val refreshToken = json.optString("refresh_token", currentSession.refreshToken)
                val expiresIn = json.optLong("expires_in", 3600L)
                val expiresAt = (System.currentTimeMillis() / 1000) + expiresIn

                val newSession = currentSession.copy(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = expiresAt
                )

                saveSession(newSession)
                AuthResult.Success(newSession, "Session refreshed.")
            } else {
                clearSession()
                AuthResult.Error("Session expired. Please sign in again.")
            }
        } catch (e: Exception) {
            // Keep local cached session if network unavailable
            if (currentSession.accessToken.isNotBlank()) {
                AuthResult.Success(currentSession, "Offline mode active.")
            } else {
                AuthResult.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Sign Out and revoke session
     */
    suspend fun signOut(): AuthResult<Boolean> = withContext(Dispatchers.IO) {
        val session = getStoredSession()
        if (session != null && session.accessToken.isNotBlank()) {
            try {
                val request = Request.Builder()
                    .url("$supabaseUrl/auth/v1/logout")
                    .addHeader("apikey", supabaseAnonKey)
                    .addHeader("Authorization", "Bearer ${session.accessToken}")
                    .post("{}".toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute()
            } catch (_: Exception) {
                // Ignore network errors on logout
            }
        }
        clearSession()
        AuthResult.Success(true, "Signed out successfully.")
    }

    private fun parseErrorMessage(jsonBody: String, httpCode: Int): String {
        return try {
            val json = JSONObject(jsonBody)
            val msg = json.optString("msg", json.optString("error_description", json.optString("message", "")))
            if (msg.isNotBlank()) {
                when {
                    msg.contains("Invalid login credentials", ignoreCase = true) -> "Invalid email or password. Please try again."
                    msg.contains("User already registered", ignoreCase = true) -> "An account with this email already exists. Please sign in instead."
                    msg.contains("Email not confirmed", ignoreCase = true) -> "Please check your email and verify your account before signing in."
                    msg.contains("Password should be at least", ignoreCase = true) -> "Password must be at least 6 characters long."
                    else -> msg
                }
            } else {
                "Authentication failed (HTTP $httpCode)."
            }
        } catch (e: Exception) {
            when (httpCode) {
                400 -> "Invalid credentials or request data."
                401 -> "Unauthorized. Please check your login credentials."
                422 -> "Unprocessable entry. Please verify your email format and password."
                429 -> "Too many attempts. Please wait a moment and try again."
                else -> "Authentication failed (HTTP $httpCode)."
            }
        }
    }
}
