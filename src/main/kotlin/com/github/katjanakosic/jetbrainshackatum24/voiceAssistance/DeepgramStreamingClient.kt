package com.github.katjanakosic.jetbrainshackatum24.voiceAssistance

import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DeepgramStreamingClient(
    private val apiKey: String,
    private val transcriptionListener: (String, Boolean) -> Unit,
    private val errorListener: (String) -> Unit
) {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    fun connect() {
        val url = "wss://api.deepgram.com/v1/listen" +
                "?encoding=linear16" +
                "&sample_rate=16000" +
                "&channels=1" +
                "&punctuate=true"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Token $apiKey")
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendAudioData(audioData: ByteArray) {
        webSocket?.send(ByteString.of(*audioData))
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
        client.dispatcher.executorService.shutdown()
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(ws: WebSocket, response: Response) {
            // Connection opened
        }

        override fun onMessage(ws: WebSocket, text: String) {
            // Received transcription
            val jsonObject = JSONObject(text)
            if (jsonObject.has("channel")) {
                val channel = jsonObject.getJSONObject("channel")
                val alternatives = channel.getJSONArray("alternatives")
                val transcript = alternatives.getJSONObject(0).getString("transcript")
                val isFinal = jsonObject.optBoolean("is_final", false)
                transcriptionListener(transcript, isFinal)
            }
        }

        override fun onMessage(ws: WebSocket, bytes: ByteString) {
            // Binary messages are not expected
        }

        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
            errorListener("WebSocket Failure: ${t.message}")
        }

        override fun onClosing(ws: WebSocket, code: Int, reason: String) {
            ws.close(code, reason)
        }

        override fun onClosed(ws: WebSocket, code: Int, reason: String) {
            // Connection closed
        }
    }
}



