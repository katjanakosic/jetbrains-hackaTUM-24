package com.github.katjanakosic.jetbrainshackatum24.voiceAssistance

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object DeepgramClient {
    fun transcribeAudio(audioData: ByteArray, apiKey: String): String {
        val client = OkHttpClient()

        val mediaType = "audio/raw; encoding=linear16; sample_rate=16000".toMediaType()
        val requestBody = audioData.toRequestBody(mediaType)

        val url = "https://api.deepgram.com/v1/listen?encoding=linear16&sample_rate=16000&channels=1"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Token $apiKey")
            .addHeader("Content-Type", "audio/raw; encoding=linear16; sample_rate=16000")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string() ?: ""
            // Parse the response JSON to get the transcript
            val jsonObject = JSONObject(responseBody)
            val transcript = jsonObject.getJSONObject("results")
                .getJSONArray("channels")
                .getJSONObject(0)
                .getJSONArray("alternatives")
                .getJSONObject(0)
                .getString("transcript")
            return transcript
        }
    }
}
