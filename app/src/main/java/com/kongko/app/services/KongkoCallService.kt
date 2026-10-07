package com.kongko.app.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kongko.app.KongkoApp
import com.kongko.app.MainActivity
import com.kongko.app.features.call.AgoraManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class KongkoCallService : Service() {

    @Inject
    lateinit var agoraManager: AgoraManager

    companion object {
        const val ACTION_START_CALL = "com.kongko.app.ACTION_START_CALL"
        const val ACTION_END_CALL = "com.kongko.app.ACTION_END_CALL"
        const val EXTRA_CALL_ID = "EXTRA_CALL_ID"
        const val EXTRA_CALLER_NAME = "EXTRA_CALLER_NAME"
        const val EXTRA_IS_VIDEO = "EXTRA_IS_VIDEO"
        const val EXTRA_CHANNEL = "EXTRA_CHANNEL"
        const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun startCall(
            context: Context,
            callId: String,
            callerName: String,
            isVideo: Boolean,
            channelName: String,
            token: String?
        ) {
            val intent = Intent(context, KongkoCallService::class.java).apply {
                action = ACTION_START_CALL
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_CALLER_NAME, callerName)
                putExtra(EXTRA_IS_VIDEO, isVideo)
                putExtra(EXTRA_CHANNEL, channelName)
                putExtra(EXTRA_TOKEN, token)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopCall(context: Context) {
            val intent = Intent(context, KongkoCallService::class.java).apply {
                action = ACTION_END_CALL
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CALL -> {
                val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
                val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Panggilan"
                val isVideo = intent.getBooleanExtra(EXTRA_IS_VIDEO, false)
                val channel = intent.getStringExtra(EXTRA_CHANNEL) ?: ""
                val token = intent.getStringExtra(EXTRA_TOKEN)

                val notification = buildOngoingCallNotification(callerName, isVideo)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    } else {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
                    }
                    startForeground(1001, notification, serviceType)
                } else {
                    startForeground(1001, notification)
                }

                agoraManager.init(this, isVideo)
                agoraManager.joinChannel(token, channel)
            }
            ACTION_END_CALL -> {
                agoraManager.leaveChannel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun buildOngoingCallNotification(callerName: String, isVideo: Boolean): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, KongkoApp.CHANNEL_CALLS)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Panggilan ${if (isVideo) "Video" else "Suara"} Berlangsung")
            .setContentText("Sedang terhubung dengan $callerName")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}
