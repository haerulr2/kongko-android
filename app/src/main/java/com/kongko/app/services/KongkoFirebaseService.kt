package com.kongko.app.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kongko.app.KongkoApp
import com.kongko.app.MainActivity
import com.kongko.app.core.network.KongkoApiService
import com.kongko.app.core.network.dto.RegisterFcmTokenRequest
import com.kongko.app.features.call.presentation.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class KongkoFirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var apiService: KongkoApiService

    private val scope = CoroutineScope(Dispatchers.IO)
    private val tag = "KongkoFCM"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "Received new FCM token: $token")
        scope.launch {
            try {
                apiService.registerFcmToken(RegisterFcmTokenRequest(token))
            } catch (e: Exception) {
                Log.e(tag, "Failed to register FCM token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val type = data["type"] ?: return

        when (type) {
            "call.incoming" -> {
                val callId = data["call_id"] ?: ""
                val callerName = data["caller_name"] ?: "Seseorang"
                val callType = data["call_type"] ?: "audio"
                val channelName = data["channel_name"] ?: ""

                showIncomingCallNotification(callId, callerName, callType == "video", channelName)
            }
            "message" -> {
                val title = message.notification?.title ?: data["sender_name"] ?: "Pesan baru"
                val body = message.notification?.body ?: data["content"] ?: ""
                showMessageNotification(title, body)
            }
        }
    }

    private fun showIncomingCallNotification(callId: String, callerName: String, isVideo: Boolean, channelName: String) {
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("EXTRA_CALL_ID", callId)
            putExtra("EXTRA_CALLER_NAME", callerName)
            putExtra("EXTRA_IS_VIDEO", isVideo)
            putExtra("EXTRA_CHANNEL_NAME", channelName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            callId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, KongkoApp.CHANNEL_CALLS)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Panggilan ${if (isVideo) "Video" else "Suara"} Masuk")
            .setContentText("$callerName sedang memanggil...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(callId.hashCode(), notification)
        } catch (e: SecurityException) {
            Log.e(tag, "Missing notification permission: ${e.message}")
        }
    }

    private fun showMessageNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, KongkoApp.CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Log.e(tag, "Missing notification permission: ${e.message}")
        }
    }
}
