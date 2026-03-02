package com.example.tongyangyuan;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tongyangyuan.database.NetworkConfig;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import io.livekit.android.LiveKit;
import io.livekit.android.room.Room;
import io.livekit.android.room.ConnectOptions;
import io.livekit.android.room.RoomOptions;
import io.livekit.android.events.RoomEvent.TrackSubscribed;
import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.track.TrackPublication;
import io.livekit.android.room.track.VideoTrack;
import kotlin.Unit;
import kotlin.Pair;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

public class VideoCallActivity extends AppCompatActivity {

    private static final String TAG = "VideoCallActivity";

    private Room room;
    private TextureViewRenderer localVideoView;
    private TextureViewRenderer remoteVideoView;

    public static final String KEY_CONSULTANT_NAME = "consultant_name";
    public static final String KEY_APPOINTMENT_ID = "appointment_id";
    public static final String KEY_CURRENT_USER_ID = "current_user_id";
    public static final String KEY_TARGET_USER_ID = "target_user_id";
    public static final String KEY_CALL_TYPE = "call_type";
    public static final String KEY_IS_CALLER = "is_caller";
    
    // Maintain backward compatibility with EXTRA_* names
    public static final String EXTRA_APPOINTMENT_ID = KEY_APPOINTMENT_ID;
    public static final String EXTRA_CURRENT_USER_ID = KEY_CURRENT_USER_ID;
    public static final String EXTRA_TARGET_USER_ID = KEY_TARGET_USER_ID;

    private Long currentUserId;
    private Long appointmentId;
    private boolean isAudioCall;
    private String callType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // Get data from intent
        appointmentId = getIntent().getLongExtra(EXTRA_APPOINTMENT_ID, -1L);
        currentUserId = getIntent().getLongExtra(EXTRA_CURRENT_USER_ID, -1L);
        callType = getIntent().getStringExtra(KEY_CALL_TYPE);
        isAudioCall = "audio".equalsIgnoreCase(callType);

        if (appointmentId == -1L || currentUserId == -1L) {
            Toast.makeText(this, "Invalid call data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        localVideoView = findViewById(R.id.localVideoView);
        remoteVideoView = findViewById(R.id.remoteVideoView);

        // Check for permissions
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = isAudioCall
                ? new String[]{Permission.RECORD_AUDIO}
                : new String[]{Permission.CAMERA, Permission.RECORD_AUDIO};

        XXPermissions.with(this)
                .permission(permissions)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (allGranted) {
                            connectToRoom();
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            XXPermissions.startPermissionActivity(VideoCallActivity.this, permissions);
                        } else {
                            Toast.makeText(VideoCallActivity.this, "需要权限才能通话", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    private void connectToRoom() {
        room = LiveKit.create(this, new RoomOptions(), null);
        initRoomEvents();

        localVideoView.setMirror(true);
        if (!isAudioCall) {
            room.initVideoRenderer(localVideoView);
        }
        room.initVideoRenderer(remoteVideoView);

        fetchTokenAndConnect();
    }

    private void initRoomEvents() {
        GlobalScope.INSTANCE.launch(Dispatchers.getMain(), kotlinx.coroutines.CoroutineStart.DEFAULT, (scope, continuation) -> {
            room.getEvents().collect((event, continuation2) -> {
                if (event instanceof TrackSubscribed) {
                    TrackSubscribed trackEvent = (TrackSubscribed) event;
                    if (trackEvent.getTrack() instanceof VideoTrack) {
                        VideoTrack videoTrack = (VideoTrack) trackEvent.getTrack();
                        videoTrack.addRenderer(remoteVideoView);
                        runOnUiThread(() -> remoteVideoView.setVisibility(android.view.View.VISIBLE));
                    }
                }
                return Unit.INSTANCE;
            }, continuation);
            return Unit.INSTANCE;
        });
    }

    private void fetchTokenAndConnect() {
        new Thread(() -> {
            try {
                String baseUrl = NetworkConfig.BASE_URL.replace("/api", ""); // Adjust as needed
                String tokenUrl = baseUrl + "/api/livekit/token?room=" + appointmentId + "&identity=" + currentUserId;
                
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(tokenUrl).build();
                Response response = client.newCall(request).execute();
                
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    JSONObject jsonObj = new JSONObject(json);
                    String token = jsonObj.getString("token");
                    String wsUrl = jsonObj.getString("serverUrl");

                    runOnUiThread(() -> {
                        GlobalScope.INSTANCE.launch(Dispatchers.getMain(), kotlinx.coroutines.CoroutineStart.DEFAULT, (scope, continuation) -> {
                            try {
                                room.connect(wsUrl, token, new ConnectOptions(), continuation);
                                
                                // Turn on local tracks
                                room.getLocalParticipant().setMicrophoneEnabled(true, continuation);
                                if (!isAudioCall) {
                                    room.getLocalParticipant().setCameraEnabled(true, continuation);
                                    // Attach local video track to view
                                    if (room.getLocalParticipant().getVideoTrackPublications().size() > 0) {
                                        for(Pair<TrackPublication, io.livekit.android.room.track.Track> pair : room.getLocalParticipant().getVideoTrackPublications()) {
                                            TrackPublication pub = pair.getFirst();
                                            if (pub.getTrack() != null) {
                                                ((VideoTrack)pub.getTrack()).addRenderer(localVideoView);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Connect failed", e);
                                Toast.makeText(VideoCallActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                            }
                            return Unit.INSTANCE;
                        });
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Token fetch failed", e);
                runOnUiThread(() -> Toast.makeText(VideoCallActivity.this, "无法获取通话凭证", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (room != null) {
            room.disconnect();
            room.release();
        }
    }
}
