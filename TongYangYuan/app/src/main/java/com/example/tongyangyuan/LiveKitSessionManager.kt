package com.example.tongyangyuan

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.participant.LocalParticipant
import io.livekit.android.room.participant.RemoteParticipant
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import livekit.org.webrtc.VideoSink

/**
 * 封装 LiveKit Android SDK 2.x（回调 + RoomListener），供 Java Activity 调用。
 */
class LiveKitSessionManager(
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
) {
    private val TAG = "LiveKitSession"

    interface Callbacks {
        fun onConnected()
        fun onRoomDisconnected()
        fun onRemoteParticipantLeft()
        fun onRemoteVideoReady()
        /** @param message 可能为 null */
        fun onError(message: String?)
    }

    private var room: Room? = null
    private var collectJob: Job? = null
    private var remoteRenderer: TextureViewRenderer? = null
    private var localRenderer: TextureViewRenderer? = null
    private var attachedRemote: VideoTrack? = null
    private var attachedLocal: VideoTrack? = null
    private var audioOnly: Boolean = false

    private var callbacks: Callbacks? = null
    private var connectedFired = false

    private fun onConnectedFired() {
        if (connectedFired) return
        connectedFired = true
        Log.d(TAG, "onConnectedFired() - triggering connection success")
        // 连接成功后立即开启麦克风和摄像头（发布本地媒体流）
        val r = room ?: return
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            r.localParticipant.setMicrophoneEnabled(true)
            if (!audioOnly) {
                r.localParticipant.setCameraEnabled(true)
            }
        }
    }

    private fun addVideoSink(track: VideoTrack, renderer: TextureViewRenderer) {
        track.addRenderer(renderer)
    }

    private fun removeVideoSink(track: VideoTrack?, renderer: TextureViewRenderer?) {
        if (track != null && renderer != null) {
            track.removeRenderer(renderer)
        }
    }

    fun connect(
        serverUrl: String,
        token: String,
        audioOnlyParam: Boolean,
        remoteContainer: FrameLayout,
        localContainer: FrameLayout,
        callbacks: Callbacks,
    ) {
        this.callbacks = callbacks
        this.audioOnly = audioOnlyParam

        disconnect(clearViews = true)
        val newRoom = LiveKit.create(context.applicationContext)
        room = newRoom

        collectJob = lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                newRoom.events.collect { event ->
                    Log.d(TAG, "RoomEvent: ${event::class.simpleName}")
                    when (event) {
                        is RoomEvent.Connected -> {
                            Log.d(TAG, "RoomEvent.Connected!")
                            onConnectedFired()
                            callbacks?.onConnected()
                        }
                        is RoomEvent.Disconnected -> {
                            Log.d(TAG, "RoomEvent.Disconnected")
                            callbacks?.onRoomDisconnected()
                        }
                        is RoomEvent.FailedToConnect -> {
                            Log.e(TAG, "RoomEvent.FailedToConnect: ${event.error}")
                            callbacks?.onError(event.error?.message)
                        }
                        is RoomEvent.ParticipantDisconnected -> {
                            Log.d(TAG, "RoomEvent.ParticipantDisconnected")
                            callbacks?.onRemoteParticipantLeft()
                        }
                        is RoomEvent.TrackPublished -> {
                            Log.d(TAG, "RoomEvent.TrackPublished: ${event.publication.track?.javaClass?.simpleName}")
                            if (!connectedFired) {
                                Log.d(TAG, "TrackPublished before Connected event - triggering connected fallback")
                                onConnectedFired()
                                callbacks?.onConnected()
                            }
                            // 本地摄像头发布后绑定到小窗预览（此前未绑定会导致本地黑屏）
                            if (!audioOnly && event.participant is LocalParticipant) {
                                val track = event.publication.track
                                if (track is VideoTrack) {
                                    removeVideoSink(attachedLocal, localRenderer)
                                    attachedLocal = track
                                    localRenderer?.let { addVideoSink(track, it) }
                                }
                            }
                        }
                        is RoomEvent.TrackSubscribed -> {
                            Log.d(TAG, "RoomEvent.TrackSubscribed: ${event.track.javaClass.simpleName}")
                            if (!audioOnly && event.track is VideoTrack) {
                                removeVideoSink(attachedRemote, remoteRenderer)
                                attachedRemote = event.track as VideoTrack
                                remoteRenderer?.let { addVideoSink(attachedRemote!!, it) }
                                callbacks?.onRemoteVideoReady()
                            }
                        }

                        is RoomEvent.ParticipantConnected -> {
                            Log.d(TAG, "RoomEvent.ParticipantConnected: ${event.participant.identity}")
                            // 对方加入后立即订阅其已有轨道（处理晚加入场景）
                            if (!audioOnly) {
                                event.participant.trackPublications.forEach { (_, pub) ->
                                    val track = pub.track
                                    if (track is VideoTrack && attachedRemote == null) {
                                        removeVideoSink(attachedRemote, remoteRenderer)
                                        attachedRemote = track
                                        remoteRenderer?.let { addVideoSink(track, it) }
                                        callbacks?.onRemoteVideoReady()
                                    }
                                }
                            }
                        }

                        is RoomEvent.TrackUnsubscribed -> {
                            Log.d(TAG, "RoomEvent.TrackUnsubscribed")
                            val unsubscribedTrack = event.track
                            if (unsubscribedTrack is VideoTrack && unsubscribedTrack == attachedRemote) {
                                removeVideoSink(attachedRemote, remoteRenderer)
                                attachedRemote = null
                            }
                        }

                        else -> Log.d(TAG, "RoomEvent other: ${event::class.simpleName}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "collect coroutine error", e)
            }
        }

        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        if (!audioOnlyParam) {
            remoteRenderer = TextureViewRenderer(context).also { v: TextureViewRenderer ->
                newRoom.initVideoRenderer(v)
                v.layoutParams = lp
                remoteContainer.removeAllViews()
                remoteContainer.addView(v)
            }
            localRenderer = TextureViewRenderer(context).also { v: TextureViewRenderer ->
                newRoom.initVideoRenderer(v)
                v.layoutParams = lp
                localContainer.removeAllViews()
                localContainer.addView(v)
            }
        }

        // 连接
        Log.d(TAG, "Attempting connect to: $serverUrl")
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                Log.d(TAG, "Calling newRoom.connect()...")
                newRoom.connect(
                    serverUrl,
                    token,
                    ConnectOptions(
                        audio = true,
                        video = !audioOnlyParam,
                    ),
                )
                Log.d(TAG, "newRoom.connect() call completed (connect is async)")

                // 自己加入后，立即订阅已在 room 中的远端参与者（处理自己晚加入场景）
                if (!audioOnlyParam) {
                    newRoom.remoteParticipants.forEach { (identity, participant) ->
                        Log.d(TAG, "Remote participant already in room: $identity")
                        participant.trackPublications.forEach { (_, pub) ->
                            val track = pub.track
                            if (track is VideoTrack && attachedRemote == null) {
                                removeVideoSink(attachedRemote, remoteRenderer)
                                attachedRemote = track
                                remoteRenderer?.let { addVideoSink(track, it) }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "connect() threw exception", e)
                callbacks.onError(e.message)
            }
        }
    }

    private fun removeViewFromParent(renderer: TextureViewRenderer?) {
        renderer?.let { v ->
            (v.parent as? ViewGroup)?.removeView(v)
        }
    }

    fun disconnect(clearViews: Boolean = true) {
        connectedFired = false
        collectJob?.cancel()
        collectJob = null
        removeVideoSink(attachedRemote, remoteRenderer)
        removeVideoSink(attachedLocal, localRenderer)
        attachedRemote = null
        attachedLocal = null
        if (clearViews) {
            removeViewFromParent(remoteRenderer)
            removeViewFromParent(localRenderer)
        }
        remoteRenderer = null
        localRenderer = null
        room?.disconnect()
        room = null
        callbacks = null
    }

    fun setMicrophoneEnabled(enabled: Boolean) {
        val r = room ?: return
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            r.localParticipant.setMicrophoneEnabled(enabled)
        }
    }

    fun setCameraEnabled(enabled: Boolean) {
        val r = room ?: return
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            r.localParticipant.setCameraEnabled(enabled)
        }
    }
}
