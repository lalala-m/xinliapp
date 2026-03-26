package com.example.tongyangyuan

import android.content.Context
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
            newRoom.events.collect { event ->
                when (event) {
                    is RoomEvent.Connected -> callbacks?.onConnected()
                    is RoomEvent.Disconnected -> callbacks?.onRoomDisconnected()
                    is RoomEvent.FailedToConnect -> callbacks?.onError(event.error.message)
                    is RoomEvent.ParticipantDisconnected -> callbacks?.onRemoteParticipantLeft()
                    is RoomEvent.TrackSubscribed -> {
                        if (!audioOnly && event.track is VideoTrack) {
                            removeVideoSink(attachedRemote, remoteRenderer)
                            attachedRemote = event.track as VideoTrack
                            remoteRenderer?.let { addVideoSink(attachedRemote!!, it) }
                            callbacks?.onRemoteVideoReady()
                        }
                    }

                    is RoomEvent.TrackUnsubscribed -> {
                        val unsubscribedTrack = event.track
                        if (unsubscribedTrack is VideoTrack && unsubscribedTrack == attachedRemote) {
                            removeVideoSink(attachedRemote, remoteRenderer)
                            attachedRemote = null
                        }
                    }

                    else -> Unit
                }
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
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            try {
                newRoom.connect(
                    serverUrl,
                    token,
                    ConnectOptions(
                        audio = true,
                        video = !audioOnlyParam,
                    ),
                )
            } catch (e: Exception) {
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
