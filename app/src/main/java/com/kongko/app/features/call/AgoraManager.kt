package com.kongko.app.features.call

import android.content.Context
import android.util.Log
import com.kongko.app.core.network.ApiConstants
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgoraManager @Inject constructor() {

    private val tag = "AgoraManager"
    private var rtcEngine: RtcEngine? = null

    private val _isJoined = MutableStateFlow(false)
    val isJoined: StateFlow<Boolean> = _isJoined.asStateFlow()

    private val _remoteUid = MutableStateFlow<Int?>(null)
    val remoteUid: StateFlow<Int?> = _remoteUid.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    fun init(context: Context, isVideo: Boolean) {
        if (rtcEngine != null) return

        try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = ApiConstants.AGORA_APP_ID
                mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                        Log.i(tag, "Joined Agora channel: $channel, uid: $uid")
                        _isJoined.value = true
                    }

                    override fun onUserJoined(uid: Int, elapsed: Int) {
                        Log.i(tag, "Remote user joined: $uid")
                        _remoteUid.value = uid
                    }

                    override fun onUserOffline(uid: Int, reason: Int) {
                        Log.i(tag, "Remote user offline: $uid, reason: $reason")
                        _remoteUid.value = null
                    }
                }
            }

            rtcEngine = RtcEngine.create(config).apply {
                setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
                if (isVideo) {
                    enableVideo()
                } else {
                    enableAudio()
                    disableVideo()
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error initializing Agora engine: ${e.message}")
        }
    }

    fun joinChannel(token: String?, channelName: String, uid: Int = 0) {
        rtcEngine?.joinChannel(token, channelName, "", uid)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
        _isJoined.value = false
        _remoteUid.value = null
    }

    fun toggleMute(): Boolean {
        val newState = !_isMuted.value
        rtcEngine?.muteLocalAudioStream(newState)
        _isMuted.value = newState
        return newState
    }

    fun toggleSpeaker(): Boolean {
        val newState = !_isSpeakerOn.value
        rtcEngine?.setEnableSpeakerphone(newState)
        _isSpeakerOn.value = newState
        return newState
    }

    fun destroy() {
        leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
    }
}
