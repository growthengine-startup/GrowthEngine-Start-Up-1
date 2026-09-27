package com.example.supabase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Handles Google Sign-In using Android Credential Manager API
 * and exchanges the Google ID Token with Supabase Auth.
 *
 * Flow:
 * 1. Show Google account picker via Credential Manager
 * 2. Receive Google ID Token
 * 3. Send ID Token to Supabase GoTrue `/auth/v1/token?grant_type=id_token`
 * 4. Supabase validates token with Google, creates/finds user, returns session
 */
class GoogleSignInHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }

    private val credentialManager = CredentialManager.create(context)
    private val supabaseService = SupabaseService(context)
    private val authService = SupabaseAuthService(context)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Get the Google Web Client ID from BuildConfig.
     * Falls back to empty string if not configured.
     */
    private fun getWebClientId(): String {
        return try {
            val buildConfigClass = Class.forName("com.example.BuildConfig")
            val field = buildConfigClass.getField("GOOGLE_WEB_CLIENT_ID")
            val value = field.get(null) as? String ?: ""
            if (value == "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com" || value.isBlank()) {
                ""
            } else {
                value
            }
        } catch (e: Exception) {
            Log.w(TAG, "GOOGLE_WEB_CLIENT_ID not found in BuildConfig: ${e.message}")
            ""
        }
    }

    /**
     * Initiates Google Sign-In flow using Android Credential Manager.
     * Returns AuthResult with the Supabase session on success.
     *
     * @param activityContext Must be an Activity context for Credential Manager to show UI
     */
    suspend fun signInWithGoogle(activityContext: Context): AuthResult<SupabaseUserSession> {
        val webClientId = getWebClientId()
        if (webClientId.isBlank()) {
            return AuthResult.Error(
                "Google Sign-In is not configured. Please add GOOGLE_WEB_CLIENT_ID to your .env file.",
                "MISSING_CONFIG"
            )
        }

        return try {
            // Step 1: Build the Google ID Token request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Show all accounts, not just previously used ones
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true) // Auto-select if only one account
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // Step 2: Launch the Credential Manager UI
            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            // Step 3: Extract Google ID Token from the credential
            val idToken = extractGoogleIdToken(result)
                ?: return AuthResult.Error(
                    "Failed to extract Google ID token from credential response.",
                    "TOKEN_EXTRACTION_FAILED"
                )

            Log.d(TAG, "Google ID Token obtained successfully (length: ${idToken.length})")

            // Step 4: Exchange the Google ID Token with Supabase
            exchangeGoogleTokenWithSupabase(idToken)

        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In")
            AuthResult.Error("Sign-in cancelled.", "USER_CANCELLED")

        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google accounts available: ${e.message}")
            AuthResult.Error(
                "No Google accounts found on this device. Please add a Google account in Settings.",
                "NO_CREDENTIALS"
            )

        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error: ${e.type} - ${e.message}")
            AuthResult.Error(
                "Google Sign-In failed: ${e.message ?: "Unknown error"}",
                "CREDENTIAL_ERROR"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            AuthResult.Error(
                "Google Sign-In failed: ${e.localizedMessage ?: "Unexpected error"}",
                "UNEXPECTED_ERROR"
            )
        }
    }

    /**
     * Extracts the Google ID token from a GetCredentialResponse.
     */
    private fun extractGoogleIdToken(response: GetCredentialResponse): String? {
        val credential = response.credential

        return when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        googleIdTokenCredential.idToken
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Failed to parse Google ID Token credential", e)
                        null
                    }
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    null
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential class: ${credential::class.java.name}")
                null
            }
        }
    }

    /**
     * Exchanges a Google ID Token with Supabase GoTrue Auth to get a session.
     * Uses the `/auth/v1/token?grant_type=id_token` endpoint.
     */
    private suspend fun exchangeGoogleTokenWithSupabase(
        idToken: String
    ): AuthResult<SupabaseUserSession> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("provider", "google")
                put("id_token", idToken)
            }

            val request = Request.Builder()
                .url("${supabaseService.supabaseUrl}/auth/v1/token?grant_type=id_token")
                .addHeader("apikey", supabaseService.supabaseAnonKey)
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
                val userEmail = userObj.optString("email", "")

                // Google user metadata comes from different places
                val userMetadata = userObj.optJSONObject("user_metadata") ?: JSONObject()
                val identityData = userObj.optJSONArray("identities")
                    ?.let { if (it.length() > 0) it.getJSONObject(0).optJSONObject("identity_data") else null }

                val fullName = userMetadata.optString("full_name",
                    userMetadata.optString("name",
                        identityData?.optString("full_name",
                            identityData?.optString("name", "")
                        ) ?: ""
                    )
                )

                val avatarUrl = userMetadata.optString("avatar_url",
                    identityData?.optString("avatar_url", "") ?: ""
                )

                val businessName = userMetadata.optString("business_name", "")
                val state = userMetadata.optString("state", "")
                val gstin = userMetadata.optString("gstin", "")

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
                    role = if (userEmail.equals("prajindezaa142@gmail.com", ignoreCase = true)) "super_admin" else "business_owner"
                )

                // Save session locally
                authService.saveSession(session)

                Log.i(TAG, "Google Sign-In successful for: $userEmail")
                AuthResult.Success(session, "Signed in with Google successfully!")
            } else {
                val errorMsg = try {
                    val errorJson = JSONObject(responseBody)
                    errorJson.optString("msg",
                        errorJson.optString("error_description",
                            errorJson.optString("message", "Google Sign-In failed (HTTP ${response.code})")
                        )
                    )
                } catch (_: Exception) {
                    "Google Sign-In failed (HTTP ${response.code})"
                }

                Log.e(TAG, "Supabase token exchange failed: $errorMsg (HTTP ${response.code})")
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during Supabase token exchange", e)
            AuthResult.Error("Network error during Google Sign-In: ${e.localizedMessage ?: "Unable to connect"}")
        }
    }
}
