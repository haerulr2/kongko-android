package com.kongko.app.features.call.presentation

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kongko.app.core.designsystem.components.AppAvatar
import com.kongko.app.core.designsystem.theme.BackgroundDark
import com.kongko.app.core.designsystem.theme.KongkoTheme
import com.kongko.app.core.designsystem.theme.RaspberryAccent
import com.kongko.app.core.designsystem.theme.StatusOnline
import com.kongko.app.core.network.KongkoWebSocketClient
import com.kongko.app.services.KongkoCallService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    @Inject
    lateinit var wsClient: KongkoWebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and show on lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val callId = intent.getStringExtra("EXTRA_CALL_ID") ?: ""
        val callerName = intent.getStringExtra("EXTRA_CALLER_NAME") ?: "Panggilan Masuk"
        val isVideo = intent.getBooleanExtra("EXTRA_IS_VIDEO", false)
        val channelName = intent.getStringExtra("EXTRA_CHANNEL_NAME") ?: ""

        setContent {
            KongkoTheme(darkTheme = true) {
                IncomingCallContent(
                    callerName = callerName,
                    isVideo = isVideo,
                    onAccept = {
                        wsClient.acceptCall(callId)
                        KongkoCallService.startCall(
                            context = this,
                            callId = callId,
                            callerName = callerName,
                            isVideo = isVideo,
                            channelName = channelName,
                            token = null
                        )
                        finish()
                    },
                    onDecline = {
                        wsClient.declineCall(callId)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun IncomingCallContent(
    callerName: String,
    isVideo: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppAvatar(
                name = callerName,
                avatarUrl = null,
                size = 110.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = callerName,
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isVideo) "Panggilan Video Masuk..." else "Panggilan Suara Masuk...",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.LightGray)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Decline Button
            FloatingActionButton(
                onClick = onDecline,
                shape = CircleShape,
                containerColor = RaspberryAccent,
                contentColor = Color.White,
                modifier = Modifier.size(68.dp)
            ) {
                Icon(Icons.Filled.CallEnd, contentDescription = "Tolak Panggilan", modifier = Modifier.size(32.dp))
            }

            // Accept Button
            FloatingActionButton(
                onClick = onAccept,
                shape = CircleShape,
                containerColor = StatusOnline,
                contentColor = Color.White,
                modifier = Modifier.size(68.dp)
            ) {
                Icon(Icons.Filled.Call, contentDescription = "Terima Panggilan", modifier = Modifier.size(32.dp))
            }
        }
    }
}
