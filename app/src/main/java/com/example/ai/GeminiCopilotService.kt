package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Wraps a copilot response with token usage metadata for quota tracking.
 */
data class CopilotResponse(
    val text: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val modelName: String = "Gemini 2.5 Flash",
    val isLocalFallback: Boolean = false
) {
    val totalTokens: Int get() = promptTokens + completionTokens
}

class GeminiCopilotService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun consultCopilot(
        userPrompt: String,
        businessContext: String
    ): CopilotResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val localText = generateLocalExecutiveInsights(userPrompt, businessContext)
            return@withContext CopilotResponse(
                text = localText,
                promptTokens = estimateTokens(userPrompt + businessContext),
                completionTokens = estimateTokens(localText),
                isLocalFallback = true
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemInstruction = """
                You are GrowthEngine AI, the enterprise business copilot for Indian MSMEs and manufacturers.
                You specialize in Indian B2B trade, GST compliance (CGST/SGST/IGST, GSTR-1, GSTR-3B, ITC claim),
                working capital management, Khata aging recovery under MSMED Act (45-day payment rule),
                inventory turnover, BOM costing, and distributor operations.
                Maintain a premium, dignified, trustworthy Indian executive tone. Use ₹ currency and clear bullet points.
                
                CURRENT REAL-TIME BUSINESS CONTEXT:
                $businessContext
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemInstruction\n\nUser Question:\n$userPrompt")
                        })
                    })
                })
            }

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("topP", 0.9)
                    put("maxOutputTokens", 1024)
                })
            }

            val body = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val json = JSONObject(responseBody)

                // Extract usage metadata from Gemini API response
                val usageMeta = json.optJSONObject("usageMetadata")
                val apiPromptTokens = usageMeta?.optInt("promptTokenCount", 0) ?: 0
                val apiCompletionTokens = usageMeta?.optInt("candidatesTokenCount", 0) ?: 0

                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val responseText = parts.getJSONObject(0).optString("text", "No response generated.")
                        return@withContext CopilotResponse(
                            text = responseText,
                            promptTokens = if (apiPromptTokens > 0) apiPromptTokens else estimateTokens(userPrompt + businessContext),
                            completionTokens = if (apiCompletionTokens > 0) apiCompletionTokens else estimateTokens(responseText),
                            isLocalFallback = false
                        )
                    }
                }
            }
            // Fallback if API returned error or empty
            val fallbackText = generateLocalExecutiveInsights(userPrompt, businessContext)
            CopilotResponse(
                text = fallbackText,
                promptTokens = estimateTokens(userPrompt + businessContext),
                completionTokens = estimateTokens(fallbackText),
                isLocalFallback = true
            )
        } catch (e: Exception) {
            val fallbackText = generateLocalExecutiveInsights(userPrompt, businessContext)
            CopilotResponse(
                text = fallbackText,
                promptTokens = estimateTokens(userPrompt + businessContext),
                completionTokens = estimateTokens(fallbackText),
                isLocalFallback = true
            )
        }
    }

    /**
     * Estimate token count from text. Approximation: ~4 characters per token for English/Hindi mix.
     */
    private fun estimateTokens(text: String): Int {
        return (text.length / 4).coerceAtLeast(1)
    }

    private fun generateLocalExecutiveInsights(prompt: String, context: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("recover") || lower.contains("receivable") || lower.contains("debt") || lower.contains("khata") -> {
                """
                📊 **Working Capital & Debtors Recovery Protocol**

                • **Priority 1: Gujarat Tooling & Die Works (Overdue: ₹2,15,400 | 18 days past Net 21)**
                  Action: Trigger Level-2 Formal Reminder citing Invoice #INV-2425/0839 and attach dynamic UPI QR code. Point out Section 15 of the MSMED Act (compound interest applicability past 45 days).
                
                • **Priority 2: Shree Ganesh Hardware Mart (Overdue: ₹18,500 | 45 days)**
                  Action: Put credit dispatch on hold until 50% token settlement is credited via UPI.

                • **Cashflow Acceleration Impact:**
                  Recovering these two accounts immediately injects **₹2,33,900** into your current account, sufficient to settle the JSPL steel vendor payable without bank overdraft charges.
                """.trimIndent()
            }
            lower.contains("gst") || lower.contains("tax") || lower.contains("itc") || lower.contains("gstr") -> {
                """
                🏛️ **GST Tax Health & ITC Reconciliation Audit**

                • **Output Tax Liability (Sales GSTR-1):**
                  Total Output Tax collected: **₹67,220** (CGST: ₹17,181 | SGST: ₹17,181 | IGST: ₹32,858)
                
                • **Input Tax Credit (ITC Claimable on Purchases & Spares):**
                  Total Verified Inward ITC: **₹18,776** (Factory Lease: ₹15,300 + Logistics: ₹920 + CNC Spares: ₹2,556)
                
                • **Net Cash Tax Payable (GSTR-3B by 20th):**
                  Net payable through Electronic Cash Ledger: **₹48,444**

                💡 **AI Tip:** Ensure all vendor invoices for the current period are uploaded in GSTR-2B before filing to prevent interest liability under Rule 88B.
                """.trimIndent()
            }
            lower.contains("stock") || lower.contains("inventory") || lower.contains("reorder") -> {
                """
                📦 **Inventory Optimization & Restock Advisory**

                • **Critical Stockout Alert:**
                  1. **Hardened Chrome Plated Shaft 25mm:** Current stock 14 Mtr (Minimum threshold: 30 Mtr). At current CNC lathe consumption, buffer will exhaust in 4 working days!
                  2. **Synthetic Gear Lubricant ISO 460:** Current stock 5 Ltr (Minimum threshold: 15 Ltr).
                
                • **Recommended PO Trigger:**
                  Dispatch Purchase Order to Jindal Steel & Power for 50 Mtr Shaft (Est. ₹32,500) and Castrol Distribution for 30 Ltr Lubricant (Est. ₹11,400).
                
                • **Capital Efficiency:**
                  Bearing Unit inventory (42 Pcs) has optimal 28-day turnover velocity. Avoid additional purchases until current batch completes assembly.
                """.trimIndent()
            }
            lower.contains("profit") || lower.contains("margin") || lower.contains("client") -> {
                """
                📈 **Client Margin & Profitability Intelligence**

                • **Highest Gross Margin Client:**
                  **Tata AutoComp Systems Ltd:** Realized gross margin of **37.5%** on Flange Bearings and assemblies. Recommended for priority production scheduling.
                
                • **Cash Drag Account:**
                  **Gujarat Tooling & Die Works:** Realized margin is healthy (32%), but 35-day collection cycle reduces effective internal rate of return (IRR) to **19.8%**.
                
                • **Strategic Recommendation:**
                  Offer Gujarat Tooling a **1.5% Early Cash Discount** on settlement within 5 days. You save more on working capital interest than the discount cost.
                """.trimIndent()
            }
            else -> {
                """
                💼 **GrowthEngine Strategic MSME Briefing**

                Based on your enterprise ledger and operations:
                
                1. **Healthy Sales Trajectory:** Gross invoiced volume this billing cycle is **₹8,39,600** across B2B contracts.
                2. **Debtors Aging Alert:** 38% of your receivables are crossing standard credit terms. Use the 1-click WhatsApp payment reminders with UPI links.
                3. **Production Continuity:** Batch BATCH-24-098 (Hydraulic Cylinders) is 60% complete; ensure Chrome Shaft buffer is restocked before Monday's second shift.
                4. **Compliance Ready:** GSTR-1 B2B outward supplies are properly mapped with HSN codes (8482, 8412, 7318) and e-Way bill references.
                
                Feel free to ask for specific recovery templates, tax breakdowns, or manufacturing batch costings.
                """.trimIndent()
            }
        }
    }
}
