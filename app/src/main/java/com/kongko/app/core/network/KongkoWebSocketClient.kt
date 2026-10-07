package com.kongko.app.core.network

import android.util.Log
import com.kongko.app.core.datastore.SessionDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

data class WsEvent(
    val type: String,
    val payload: JsonObject?
)

@Singleton
class KongkoWebSocketClient @Inject constructor(
    private val sessionDataStore: SessionDataStore
) {
    private val tag = "KongkoWebSocket"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val isConnected = AtomicBoolean(false)
    private var shouldReconnect = true

    private val _events = MutableSharedFlow<WsEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<WsEvent> = _events.asSharedFlow()

    fun connect() {
        shouldReconnect = true
        scope.launch {
            val token = sessionDataStore.getAccessToken()
            if (token.isNullOrBlank()) {
                Log.w(tag, "Cannot connect WS: No access token")
                return@launch
            }

            if (isConnected.get()) return@launch

            val url = "${ApiConstants.WS_URL}?token=$token"
            val request = Request.Builder().url(url).build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(ws: WebSocket, response: Response) {
                    Log.i(tag, "WebSocket connected successfully")
                    isConnected.set(true)
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    try {
                        val root = json.parseToJsonElement(text).jsonObject
                        val type = root["type"]?.jsonPrimitive?.content ?: return
                        val payload = root["payload"]?.jsonObject
                        scope.launch {
                            _events.emit(WsEvent(type, payload))
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to parse incoming WS message: ${e.message}")
                    }
                }

                override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                    Log.w(tag, "WebSocket closed: $code $reason")
                    isConnected.set(false)
                    attemptReconnect()
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    Log.e(tag, "WebSocket error: ${t.message}")
                    isConnected.set(false)
                    attemptReconnect()
                }
            })
        }
    }

    private fun attemptReconnect() {
        if (!shouldReconnect) return
        scope.launch {
            delay(3000)
            if (!isConnected.get() && shouldReconnect) {
                Log.i(tag, "Attempting to reconnect WebSocket...")
                connect()
            }
        }
    }

    fun disconnect() {
        shouldReconnect = false
        isConnected.set(false)
        webSocket?.close(1000, "User disconnected")
        webSocket = null
    }

    fun sendMessage(chatId: String, content: String, type: String = "text", mediaUrl: String? = null) {
        val payload = buildJsonObject {
            put("chat_id", chatId)
            put("content", content)
            put("type", type)
            if (mediaUrl != null) put("media_url", mediaUrl)
        }
        send("message.send", payload)
    }

    fun sendTyping(chatId: String, isTyping: Boolean) {
        val eventType = if (isTyping) "typing.start" else "typing.stop"
        val payload = buildJsonObject {
            put("chat_id", chatId)
        }
        send(eventType, payload)
    }

    fun initiateCall(receiverId: String, isVideo: Boolean) {
        val payload = buildJsonObject {
            put("receiver_id", receiverId)
            put("type", if (isVideo) "video" else "audio")
        }
        send("call.initiate", payload)
    }

    fun acceptCall(callId: String) {
        val payload = buildJsonObject {
            put("call_id", callId)
        }
        send("call.accept", payload)
    }

    fun declineCall(callId: String) {
        val payload = buildJsonObject {
            put("call_id", callId)
        }
        send("call.decline", payload)
    }

    fun endCall(callId: String, durationSeconds: Int) {
        val payload = buildJsonObject {
            put("call_id", callId)
            put("duration_seconds", durationSeconds)
        }
        send("call.end", payload)
    }

    private fun send(type: String, payload: JsonObject) {
        val msg = buildJsonObject {
            put("type", type)
            put("payload", payload)
        }.toString()

        val sent = webSocket?.send(msg) ?: false
        if (!sent) {
            Log.w(tag, "Failed to send message: WS not connected")
        }
    }
}
