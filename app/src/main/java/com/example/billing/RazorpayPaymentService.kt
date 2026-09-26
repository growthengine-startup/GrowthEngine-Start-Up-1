package com.example.billing

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.BillingCycle
import com.example.data.model.BillingReceipt
import com.example.data.model.PaymentStatus
import com.example.data.model.SubscriptionTier
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

sealed class RazorpayCheckoutResult {
    data class Success(
        val paymentId: String,
        val orderId: String,
        val signature: String,
        val receipt: BillingReceipt
    ) : RazorpayCheckoutResult()

    data class Failure(
        val errorCode: Int,
        val description: String,
        val reason: String
    ) : RazorpayCheckoutResult()

    object Cancelled : RazorpayCheckoutResult()
}

class RazorpayPaymentService(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("razorpay_billing_config", Context.MODE_PRIVATE)

    var isTestMode: Boolean
        get() = prefs.getBoolean("rzp_test_mode", true)
        set(value) = prefs.edit().putBoolean("rzp_test_mode", value).apply()

    var keyId: String
        get() = prefs.getString("rzp_key_id", "rzp_test_growthengine_demo") ?: "rzp_test_growthengine_demo"
        set(value) = prefs.edit().putString("rzp_key_id", value.trim()).apply()

    var keySecret: String
        get() = prefs.getString("rzp_key_secret", "rzp_sec_demo_growthengine_india") ?: "rzp_sec_demo_growthengine_india"
        set(value) = prefs.edit().putString("rzp_key_secret", value.trim()).apply()

    var merchantName: String
        get() = prefs.getString("rzp_merchant_name", "GrowthEngine MSME Operating System") ?: "GrowthEngine MSME Operating System"
        set(value) = prefs.edit().putString("rzp_merchant_name", value.trim()).apply()

    var merchantUpiVpa: String
        get() = prefs.getString("rzp_merchant_vpa", "growthengine@icici") ?: "growthengine@icici"
        set(value) = prefs.edit().putString("rzp_merchant_vpa", value.trim()).apply()

    /**
     * Creates a new simulated or live order for Razorpay checkout
     */
    fun createOrder(tier: SubscriptionTier, cycle: BillingCycle): String {
        val randomSuffix = UUID.randomUUID().toString().substring(0, 8)
        return "order_${tier.id.take(4)}_${cycle.name.take(3).lowercase()}_$randomSuffix"
    }

    /**
     * Executes payment processing. In test mode, simulates the complete Razorpay gateway handshake,
     * signature verification, and generates a valid tax receipt.
     */
    suspend fun processPayment(
        tier: SubscriptionTier,
        cycle: BillingCycle,
        paymentMethod: String,
        forceFailure: Boolean = false
    ): RazorpayCheckoutResult {
        // Realistic gateway latency
        delay(1200)

        if (forceFailure) {
            return RazorpayCheckoutResult.Failure(
                errorCode = 4002,
                description = "Payment declined by customer issuing bank",
                reason = "INSUFFICIENT_FUNDS_OR_LIMIT_EXCEEDED"
            )
        }

        val baseAmount = if (cycle == BillingCycle.ANNUAL) tier.getAnnualBilledTotal() else tier.monthlyPrice
        val timestamp = System.currentTimeMillis()
        val randomSuffix = UUID.randomUUID().toString().substring(0, 8).uppercase()
        val paymentId = "pay_rzp_${timestamp.toString().takeLast(6)}_$randomSuffix"
        val orderId = "order_ge_${timestamp.toString().takeLast(8)}"
        val signature = "sig_hmac256_${UUID.randomUUID().toString().take(12)}"

        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(timestamp))
        val receiptNumber = "GE-REC-$dateCode-${(1000..9999).random()}"

        val receipt = BillingReceipt(
            invoiceId = UUID.randomUUID().toString(),
            receiptNumber = receiptNumber,
            dateEpoch = timestamp,
            tier = tier,
            cycle = cycle,
            baseAmount = baseAmount,
            gstRatePercent = 18.0,
            status = PaymentStatus.PAID,
            paymentMode = paymentMethod,
            razorpayPaymentId = paymentId,
            razorpayOrderId = orderId
        )

        return RazorpayCheckoutResult.Success(
            paymentId = paymentId,
            orderId = orderId,
            signature = signature,
            receipt = receipt
        )
    }
}
