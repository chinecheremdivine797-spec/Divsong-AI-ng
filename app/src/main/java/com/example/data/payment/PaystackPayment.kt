package com.example.data.payment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object PaystackPayment {
    private const val ENDPOINT = "https://iyoxcubcxgywybkufjab.supabase.co/functions/v1/divsong-paystack-payment"

    suspend fun startPremiumCheckout(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            val payload = JSONObject().put("email", email.trim().lowercase()).toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (responseCode !in 200..299) {
                return@withContext Result.failure(Exception(JSONObject(body).optString("error", "Unable to start Paystack checkout")))
            }
            val checkoutUrl = JSONObject(body).optString("authorization_url")
            if (checkoutUrl.isBlank()) return@withContext Result.failure(Exception("Paystack did not return a checkout URL"))
            Result.success(checkoutUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
