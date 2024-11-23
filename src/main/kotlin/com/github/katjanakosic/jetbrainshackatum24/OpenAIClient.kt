package com.github.katjanakosic.jetbrainshackatum24

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object OpenAIClient {
    fun getChatResponse(prompt: String, apiKey: String): String {
        val client = OkHttpClient()

        val json = JSONObject()
        json.put("model", "gpt-3.5-turbo")
        val messages = org.json.JSONArray()
        val messageObject = JSONObject()
        messageObject.put("role", "user")
        messageObject.put("content", prompt)
        messages.put(messageObject)
        json.put("messages", messages)

        val mediaType = "application/json".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string() ?: ""
            // Parse the response JSON to get the assistant's reply
            val jsonObject = JSONObject(responseBody)
            val choices = jsonObject.getJSONArray("choices")
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            val content = message.getString("content")
            return content
        }
    }
}

