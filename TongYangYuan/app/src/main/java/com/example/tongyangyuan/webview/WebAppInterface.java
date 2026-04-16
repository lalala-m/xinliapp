package com.example.tongyangyuan.webview;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedDispatcher;

import com.example.tongyangyuan.AppointmentActivity;
import com.example.tongyangyuan.ChatActivity;
import com.example.tongyangyuan.ConsultantDetailActivity;
import com.example.tongyangyuan.MainActivity;
import com.example.tongyangyuan.child.ChildProfile;
import com.example.tongyangyuan.child.ChildProfileRepository;
import com.example.tongyangyuan.consult.Consultant;
import com.example.tongyangyuan.consult.ConsultantRepository;
import com.example.tongyangyuan.data.AppointmentRecord;
import com.example.tongyangyuan.data.AppointmentStore;
import com.example.tongyangyuan.data.ChatMessageRecord;
import com.example.tongyangyuan.data.ChatStore;
import com.example.tongyangyuan.data.PreferenceStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WebAppInterface {

    private static final String TAG = "WebAppInterface";

    // ==================== 旧 NIM 配置已移除 ====================

    public interface MediaDelegate {
        void pickImage();
        void captureImage();
        void pickVideo();
        void captureVideo();
        void startVoiceRecord();
        void stopVoiceRecord();
        void cancelVoiceRecord();
        void playMedia(String type, String uri);
    }
    
    // OpenIM 消息回调接口
    public interface OpenIMMessageListener {
        void onMessageReceived(String accountId, String content, String msgType, long timestamp);
        void onCallReceived(String accountId, String callType, String sessionId);
        void onCallAnswered(String sessionId, boolean accepted);
        void onCallEnded(String sessionId);
    }

    private OpenIMMessageListener openIMMessageListener;
    private com.example.tongyangyuan.openim.OpenIMService openIMService;

    public void setOpenIMMessageListener(OpenIMMessageListener listener) {
        this.openIMMessageListener = listener;
    }
    private MediaDelegate mediaDelegate;
    private boolean voiceRecordCallbackEnabled = true;

    public void setMediaDelegate(MediaDelegate delegate) {
        this.mediaDelegate = delegate;
    }

    public void setVoiceRecordCallbackEnabled(boolean enabled) {
        this.voiceRecordCallbackEnabled = enabled;
    }
    private final Context context;
    private final AppointmentStore appointmentStore;
    private final ChatStore chatStore;
    private final PreferenceStore preferenceStore;
    private final ConsultantRepository consultantRepository;
    private final ChildProfileRepository childProfileRepository;
    private WeakReference<WebView> webViewRef;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * ChatActivity 在 file:// 下注入：WebView 常把 {@code location.search} 置空，JS 读不到 URL 参数。
     */
    private volatile String chatLocalAppointmentId = "";
    private volatile long chatServerAppointmentIdNative = -1L;
    private volatile String chatConsultantDisplayName = "";
    private volatile long chatConsultantUserIdNative = 0L;

    /** 仅 ChatActivity 在 loadUrl 前调用 */
    public void setChatSessionForWebView(String localAppointmentId, long serverAppointmentId,
                                         String consultantName, long consultantUserId) {
        this.chatLocalAppointmentId = localAppointmentId != null ? localAppointmentId : "";
        this.chatServerAppointmentIdNative = serverAppointmentId;
        this.chatConsultantDisplayName = consultantName != null ? consultantName : "";
        this.chatConsultantUserIdNative = consultantUserId;
    }

    @JavascriptInterface
    public String getChatLocalAppointmentId() {
        return chatLocalAppointmentId != null ? chatLocalAppointmentId : "";
    }

    @JavascriptInterface
    public long getChatServerAppointmentIdNative() {
        return chatServerAppointmentIdNative;
    }

    @JavascriptInterface
    public String getChatConsultantDisplayName() {
        return chatConsultantDisplayName != null ? chatConsultantDisplayName : "";
    }

    @JavascriptInterface
    public long getChatConsultantUserIdNative() {
        return chatConsultantUserIdNative;
    }

    public void setWebView(WebView webView) {
        this.webViewRef = new WeakReference<>(webView);
    }

    public WebAppInterface(Context context, WebView webView) {
        this.context = context;
        this.appointmentStore = new AppointmentStore(context);
        this.chatStore = new ChatStore(context);
        this.preferenceStore = new PreferenceStore(context);
        this.consultantRepository = ConsultantRepository.getInstance(context);
        this.childProfileRepository = com.example.tongyangyuan.child.ChildProfileRepository.getInstance(context);
        this.webViewRef = new WeakReference<>(webView);
        
        // 如果是 MainActivity，设置当前的 webInterface 实例
        if (context instanceof MainActivity) {
            ((MainActivity) context).setCurrentWebInterface(this);
        }
    }

    @JavascriptInterface
    public String getToken() {
        return preferenceStore.getAuthToken();
    }

    @JavascriptInterface
    public void showToast(String message) {
        mainHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    @JavascriptInterface
    public void setBottomNavDimmed(boolean dimmed) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).setBottomNavDimmed(dimmed);
        }
    }

    @JavascriptInterface
    public long getServerAppointmentId(String localAppointmentId) {
        if (TextUtils.isEmpty(localAppointmentId)) {
            return -1;
        }
        AppointmentRecord record = appointmentStore.getAppointmentById(localAppointmentId);
        return record != null ? record.getServerId() : -1;
    }

    /**
     * splash.html「跳过动画」及通用跳转入口。
     * @param page "home" → MainActivity; 其余走 openWebPage(fileName)
     */
    @JavascriptInterface
    public void navigateTo(String page) {
        if (context == null) return;
        if ("home".equalsIgnoreCase(page)) {
            if (!preferenceStore.isLoggedIn()) {
                openWebPage("auth.html");
                finishHost();
                return;
            }
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            finishHost();
        } else {
            openWebPage(page);
            finishHost();
        }
    }

    @JavascriptInterface
    public void navigateToLearning() {
        openWebPage("learning.html");
    }

    @JavascriptInterface
    public void navigateToAssessment() {
        openWebPage("assessment.html");
    }

    @JavascriptInterface
    public void navigateToConsult() {
        openWebPage("consult.html");
    }

    @JavascriptInterface
    public void navigateToChat(String appointmentId) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            showToast("预约记录无效");
            return;
        }
        AppointmentRecord record = appointmentStore.getAppointmentById(appointmentId.trim());
        if (record == null) {
            try {
                long serverId = Long.parseLong(appointmentId.trim());
                record = appointmentStore.getAppointmentByServerId(serverId);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        if (record == null) {
            showToast("未找到对应的预约记录，请稍后刷新后重试");
            return;
        }
        String status = record.getStatus();
        if ("CANCELLED".equalsIgnoreCase(status)) {
            showToast("该咨询已取消，无法进入聊天");
            return;
        }
        if ("PENDING".equalsIgnoreCase(status)) {
            showToast("请等待咨询师确认后再开始聊天");
            return;
        }
        Consultant consultant = record.getConsultant();
        if (consultant == null) {
            showToast("预约记录缺少咨询师信息，请刷新后重试");
            return;
        }
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.KEY_CONSULTANT, consultant);
        intent.putExtra(ChatActivity.KEY_APPOINTMENT_DATE, record.getDate());
        intent.putExtra(ChatActivity.KEY_APPOINTMENT_SLOT, record.getTimeSlot());
        intent.putExtra(ChatActivity.KEY_APPOINTMENT_ID, record.getId());
        intent.putExtra(ChatActivity.KEY_CHILD_ID, record.getChildId() != null ? record.getChildId() : "");
        intent.putExtra(ChatActivity.KEY_CHILD_NAME, record.getChildName() != null ? record.getChildName() : "");
        intent.putExtra("chat_server_id", record.getServerId());
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void endConsultation(String appointmentId) {
        appointmentStore.markConsultationEnded(appointmentId);
    }

    @JavascriptInterface
    public boolean isConsultationEnded(String appointmentId) {
        return appointmentStore.isConsultationEnded(appointmentId);
    }

    @JavascriptInterface
    public void goBack() {
        mainHandler.post(() -> {
            WebView webView = webViewRef != null ? webViewRef.get() : null;
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                Activity activity = getActivity();
                if (activity instanceof ComponentActivity) {
                    OnBackPressedDispatcher dispatcher = ((ComponentActivity) activity).getOnBackPressedDispatcher();
                    dispatcher.onBackPressed();
                } else if (activity != null) {
                    activity.onBackPressed();
                }
            }
        });
    }


    @JavascriptInterface
    public void openImagePicker() {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).pickImage();
        } else {
            showToast("当前页面不支持发送图片");
        }
    }

    @JavascriptInterface
    public void openVideoPicker() {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).pickVideo();
        } else {
            showToast("当前页面不支持发送视频");
        }
    }

    @JavascriptInterface
    public void captureImage() {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).captureImage();
        } else {
            showToast("当前页面不支持拍照");
        }
    }

    @JavascriptInterface
    public void captureVideo() {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).captureVideo();
        } else {
            showToast("当前页面不支持拍摄视频");
        }
    }

    @JavascriptInterface
    public void startVoiceRecord() {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).startVoiceRecord();
        } else {
            showToast("当前页面不支持录音");
        }
    }

    @JavascriptInterface
    public void stopVoiceRecord() {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).stopVoiceRecord();
        }
    }

    @JavascriptInterface
    public void cancelVoiceRecord() {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).cancelVoiceRecord();
        }
    }

    @JavascriptInterface
    public void playMedia(String type, String uri) {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).playMedia(type, uri);
        }
    }

    /**
     * 通知WebView当前正在播放音频
     * 用于显示播放动画
     */
    @JavascriptInterface
    public void notifyAudioPlaying(String mediaUrl, boolean isPlaying) {
        mainHandler.post(() -> {
            WebView webView = webViewRef != null ? webViewRef.get() : null;
            if (webView != null) {
                String escapedUrl = mediaUrl != null ? mediaUrl.replace("'", "\\'") : "";
                String script = String.format(
                    "if (window.onAudioPlayingState) window.onAudioPlayingState('%s', %b);",
                    escapedUrl, isPlaying
                );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(script, null);
                } else {
                    webView.loadUrl("javascript:" + script);
                }
            }
        });
    }

    @JavascriptInterface
    public void navigateToConsultantList(String selectedQuestions) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "consultant_list.html");
        if (!TextUtils.isEmpty(selectedQuestions)) {
            intent.putExtra("selected_questions", selectedQuestions);
        }
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void navigateToConsultantList() {
        navigateToConsultantList(null);
    }

    @JavascriptInterface
    public void navigateToRecharge() {
        openWebPage("upgrade_service.html");
    }

    @JavascriptInterface
    public boolean isPaidUser() {
        return preferenceStore.isPaidUser();
    }

    @JavascriptInterface
    public void onPaymentSuccess() {
        preferenceStore.setPaidUser(true);
        showToast("支付成功，您已成为会员");
        // 通知可能存在的监听者或者刷新页面状态
        mainHandler.post(() -> {
            // 如果需要做一些UI更新可以在这里做
        });
    }

    @JavascriptInterface
    public void purchaseUpgradeService(String json) {
        try {
            JSONObject data = new JSONObject(json);
            String paymentMethod = data.getString("paymentMethod");
            String packageName = data.getString("packageName");
            double price = data.getDouble("price");

            // 模拟支付逻辑：跳转到对应APP
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if ("alipay".equals(paymentMethod)) {
                // 支付宝跳转链接 (示例)
                intent.setData(Uri.parse("alipays://platformapi/startapp"));
                showToast("正在启动支付宝支付...");
            } else if ("wechat".equals(paymentMethod)) {
                // 微信跳转链接 (示例)
                intent.setData(Uri.parse("weixin://"));
                showToast("正在启动微信支付...");
            } else {
                showToast("不支持的支付方式");
                return;
            }

            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                showToast("未安装对应的支付应用，将使用模拟支付");
            }

            // 模拟支付回调：延迟几秒后提示成功并开通权益
            // 在真实场景中，这里应该等待服务端的回调或查询订单状态
            mainHandler.postDelayed(() -> {
                preferenceStore.setPaidUser(true);
                // 这里可以根据套餐类型做更细致的权限控制，目前统一设置为会员
                showToast("支付成功！已开通 " + packageName);

                // 返回上一页
                goBack();
            }, 3000);

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("订单数据错误");
        }
    }

    @JavascriptInterface
    public void launchWeChatPay(String amount, String orderId, String productName) {
        showToast("正在启动微信支付 (测试模式)...");
        simulatePaymentSuccess("微信支付");
    }

    @JavascriptInterface
    public void launchAlipay(String amount, String orderId, String productName) {
        showToast("正在启动支付宝 (测试模式)...");
        simulatePaymentSuccess("支付宝");
    }

    private void simulatePaymentSuccess(String method) {
        // 先通知前端隐藏 Loading
        dispatchJs("window.onPaymentSuccess && window.onPaymentSuccess()");
        
        mainHandler.postDelayed(() -> {
            preferenceStore.setPaidUser(true);
            showToast(method + "成功 (测试模式)");
            
            // 确保刷新状态
            mainHandler.postDelayed(() -> {
                // 尝试结束当前页面
                goBack();
            }, 1000);
        }, 1000);
    }

    @JavascriptInterface
    public void startVideoCall(String appointmentId, String consultantName, long targetUserId) {
        Intent intent = new Intent(context, com.example.tongyangyuan.VideoCallActivity.class);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_APPOINTMENT_ID, Long.parseLong(appointmentId));
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CONSULTANT_NAME, consultantName);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CURRENT_USER_ID, preferenceStore.getUserId());
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_TARGET_USER_ID, targetUserId);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CALL_TYPE, "video");
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_IS_CALLER, true);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void startVoiceCall(String consultantName, String appointmentId) {
        // 语音通话功能
        showToast("正在连接" + consultantName + "咨询师的语音通话...");
        // 可以在这里实现实际的语音通话逻辑
    }

    @JavascriptInterface
    public void startVideoCallWithType(String appointmentId, String consultantName, long targetUserId, String callType) {
        Intent intent = new Intent(context, com.example.tongyangyuan.VideoCallActivity.class);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_APPOINTMENT_ID, Long.parseLong(appointmentId));
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CONSULTANT_NAME, consultantName);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CURRENT_USER_ID, preferenceStore.getUserId());
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_TARGET_USER_ID, targetUserId);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CALL_TYPE, callType);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_IS_CALLER, true);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void startOutgoingCall(String appointmentId, String consultantName, long targetUserId, String callType) {
        Intent intent = new Intent(context, com.example.tongyangyuan.VideoCallActivity.class);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_APPOINTMENT_ID, Long.parseLong(appointmentId));
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CONSULTANT_NAME, consultantName);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CURRENT_USER_ID, preferenceStore.getUserId());
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_TARGET_USER_ID, targetUserId);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_CALL_TYPE, callType);
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_IS_CALLER, true);
        context.startActivity(intent);
    }

    // ==================== 音视频通话控制方法 ====================

    @JavascriptInterface
    public void setMute(boolean muted) {
        Log.d(TAG, "setMute: " + muted);
        // 实际静音控制需要传递到VideoCallActivity
        // 这里只做日志记录
    }

    @JavascriptInterface
    public void setVideoEnabled(boolean enabled) {
        Log.d(TAG, "setVideoEnabled: " + enabled);
        // 实际视频控制需要传递到VideoCallActivity
    }

    @JavascriptInterface
    public void endCurrentCall() {
        Log.d(TAG, "endCurrentCall called");
        // 结束当前通话
    }

    @JavascriptInterface
    public void onCallConnected() {
        Log.d(TAG, "onCallConnected");
        mainHandler.post(() -> showToast("通话已连接"));
    }

    @JavascriptInterface
    public void openCallPage(String appointmentId, String consultantName, String targetUserId, String callType) {
        // 打开通话测试页面
        Intent intent = new Intent(context, com.example.tongyangyuan.webview.WebViewActivity.class);
        intent.putExtra(com.example.tongyangyuan.webview.WebViewActivity.EXTRA_HTML_FILE, "call.html");
        if (appointmentId != null) intent.putExtra("appointmentId", appointmentId);
        if (consultantName != null) intent.putExtra("consultantName", consultantName);
        if (targetUserId != null) intent.putExtra("targetUserId", targetUserId);
        if (callType != null) intent.putExtra("callType", callType);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void openAccountManagement() {
        openWebPage("account_manage.html");
    }

    @JavascriptInterface
    public void openWebPage(String htmlFile) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, htmlFile);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void openChildManagement() {
        openWebPage("child_info.html");
    }

    // ========== 咨询师相关 ==========

    /**
     * 打开咨询师登录页面
     */
    @JavascriptInterface
    public void openConsultantLogin() {
        openWebPage("consultant_login.html");
    }

    /**
     * 打开咨询师个人中心页面
     */
    @JavascriptInterface
    public void openConsultantProfile() {
        openWebPage("consultant_profile.html");
    }

    /**
     * 保存咨询师的 JWT Token
     */
    @JavascriptInterface
    public void saveAuthToken(String token) {
        preferenceStore.saveConsultantToken(token);
    }

    /**
     * 获取咨询师 JWT Token（与普通登录 token 区分）
     */
    @JavascriptInterface
    public String getConsultantAuthToken() {
        String token = preferenceStore.getConsultantToken();
        return token != null ? token : "";
    }

    /**
     * 退出咨询师登录
     */
    @JavascriptInterface
    public void consultantLogout() {
        preferenceStore.clearConsultantToken();
        showToast("已退出咨询师账号");
    }

    /**
     * 获取当前登录用户类型（CONSULTANT / PARENT / ADMIN）
     */
    @JavascriptInterface
    public String getUserType() {
        return preferenceStore.getUserType() != null ? preferenceStore.getUserType() : "";
    }

    /**
     * 保存用户类型
     */
    @JavascriptInterface
    public void saveUserType(String userType) {
        preferenceStore.saveUserType(userType);
    }

    /**
     * 保存用户完整资料（含头像）
     */
    @JavascriptInterface
    public void saveUserProfile(String jsonProfile) {
        try {
            JSONObject profile = new JSONObject(jsonProfile);
            if (profile.has("userId")) {
                preferenceStore.saveLastLoginUserId(profile.getLong("userId"));
            }
            if (profile.has("userType")) {
                preferenceStore.saveUserType(profile.getString("userType"));
            }
            if (profile.has("phone")) {
                preferenceStore.saveLastLoginPhone(profile.getString("phone"));
            }
            if (profile.has("nickname")) {
                preferenceStore.saveNickname(profile.getString("nickname"));
            }
            if (profile.has("avatarUrl")) {
                preferenceStore.saveAvatarUrl(profile.getString("avatarUrl"));
            }
            if (profile.has("isLoggedIn")) {
                preferenceStore.setLoggedIn(profile.getBoolean("isLoggedIn"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取头像 URL
     */
    @JavascriptInterface
    public String getAvatarUrl() {
        String url = preferenceStore.getAvatarUrl();
        return url != null ? url : "";
    }

    /**
     * 获取用户昵称
     */
    @JavascriptInterface
    public String getNickname() {
        String name = preferenceStore.getNickname();
        return name != null ? name : "";
    }

    @JavascriptInterface
    public void logoutAccount() {
        preferenceStore.setLoggedIn(false);
        preferenceStore.setPaidUser(false);
        preferenceStore.setLastLoginPhone("");
        showToast("已退出登录");
        // 跳转到登录页面并关闭所有Activity
        Intent intent = new Intent(context, com.example.tongyangyuan.AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        finishHost();
    }

    /**
     * 更新用户资料后回调（由前端 H5 调用）
     * 保存昵称和头像到本地
     */
    @JavascriptInterface
    public void onProfileUpdated(String nickname, String avatarUrl) {
        if (nickname != null && !nickname.isEmpty()) {
            preferenceStore.saveNickname(nickname);
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            preferenceStore.saveAvatarUrl(avatarUrl);
        }
        Log.d(TAG, "onProfileUpdated: nickname=" + nickname + ", avatarUrl=" + avatarUrl);
    }

    /**
     * 完整更新用户资料（由前端 H5 调用）
     * 接收完整的 JSON 对象字符串，包含所有用户信息
     */
    @JavascriptInterface
    public void updateUserProfile(String jsonProfile) {
        try {
            JSONObject profile = new JSONObject(jsonProfile);
            if (profile.has("nickname")) {
                preferenceStore.saveNickname(profile.getString("nickname"));
            }
            if (profile.has("avatarUrl")) {
                preferenceStore.saveAvatarUrl(profile.getString("avatarUrl"));
            }
            if (profile.has("phone")) {
                preferenceStore.saveLastLoginPhone(profile.getString("phone"));
            }
            if (profile.has("userType")) {
                preferenceStore.saveUserType(profile.getString("userType"));
            }
            if (profile.has("isPaid")) {
                preferenceStore.setPaidUser(profile.getBoolean("isPaid"));
            }
            Log.d(TAG, "updateUserProfile: " + jsonProfile);
        } catch (JSONException e) {
            Log.e(TAG, "updateUserProfile failed", e);
        }
    }

    @JavascriptInterface
    public String getUserProfile() {
        try {
            JSONObject obj = new JSONObject();
            String phone = preferenceStore.getLastLoginPhone();
            String nickname = preferenceStore.getNickname();
            String avatarUrl = preferenceStore.getAvatarUrl();
            obj.put("phone", phone != null ? phone : "");
            obj.put("nickname", nickname != null ? nickname : "");
            obj.put("avatarUrl", avatarUrl != null ? avatarUrl : "");
            obj.put("isPaid", preferenceStore.isPaidUser());
            obj.put("hasChildProfile", preferenceStore.hasChildProfile());
            obj.put("isLoggedIn", preferenceStore.isLoggedIn());
            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @JavascriptInterface
    public String getConsultants() {
        JSONArray array = new JSONArray();
        for (Consultant consultant : consultantRepository.getAllSync()) {
            try {
                array.put(consultantToJson(consultant));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array.toString();
    }

    @JavascriptInterface
    public void navigateToConsultantDetail(String name) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "consultant_detail.html");
        intent.putExtra("name", name);
        Consultant consultant = consultantRepository.findByNameSync(name);
        if (consultant != null) {
            intent.putExtra(ConsultantDetailActivity.KEY_CONSULTANT, consultant);
        }
        context.startActivity(intent);
    }

    @JavascriptInterface
    public String getConsultantDetail(String name) {
        Consultant consultant = consultantRepository.findByNameSync(name);
        // 如果数据库没有找到，不再使用 fallback
        if (consultant == null) {
            return "{}";
        }
        try {
            return consultantToJson(consultant).toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @JavascriptInterface
    public void navigateToAppointment(String consultantName) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "appointment.html");
        if (consultantName != null) {
            intent.putExtra("consultant_name", consultantName);
            Consultant consultant = consultantRepository.findByNameSync(consultantName);
            if (consultant != null) {
                intent.putExtra(AppointmentActivity.KEY_CONSULTANT, consultant);
            }
        }
        context.startActivity(intent);
    }

    @JavascriptInterface
    public void openDatePicker() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                activity,
                (view, year, month, dayOfMonth) -> {
                    String value = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    dispatchJs("window.onDateSelected && window.onDateSelected('" + value + "')");
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    @JavascriptInterface
    public void submitAppointment(String date, String timeSlot, String description, String childId, String childName, String domain) {
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(timeSlot) || TextUtils.isEmpty(childId)) {
            showToast("请填写完整信息");
            return;
        }

        // 1. 获取当前 Consult (如果是在ConsultantDetailActivity)
        Consultant consultant = null;
        if (context instanceof ConsultantDetailActivity) {
            consultant = ((ConsultantDetailActivity) context).getConsultant();
        }

        // 2. 如果获取不到，尝试从Intent获取
        if (consultant == null && context instanceof Activity) {
            consultant = ((Activity) context).getIntent().getParcelableExtra("consultant");
            // Also try extractConsultant logic if parcelable is null
            if (consultant == null) {
                 consultant = extractConsultant(((Activity) context).getIntent());
            }
        }

        // 3. 如果还是获取不到
        if (consultant == null) {
            showToast("无法获取咨询师信息，请重试");
            return;
        }
        
        final Consultant finalConsultant = consultant;

        // 4. 保存到本地数据库 (先保存本地，再异步同步服务器)
        String localAppointmentId = appointmentStore.addAppointment(
                consultant, date, timeSlot, description, childId, childName, domain
        );

        // 5. 异步提交到服务器
        new Thread(() -> {
            try {
                long parentUserId = preferenceStore.getUserId();
                if (parentUserId <= 0) {
                    mainHandler.post(() -> showToast("请先登录"));
                    return;
                }
                
                // 区分 userId（users表主键）和 serverId（consultants表主键/档案ID）
                // 预约表 appointments.consultant_id 引用的是 consultants.id，不是 users.id
                final long consultantUserId = finalConsultant.getUserId();
                final long consultantServerId = finalConsultant.getServerId();

                if (consultantServerId <= 0) {
                     Log.e("WebAppInterface", "Consultant serverId (档案ID) 缺失或无效: " + consultantServerId);
                     mainHandler.post(() -> showToast("咨询师档案数据异常，请重新登录APP后重试"));
                     return;
                }

                Log.d("WebAppInterface", "Submitting appointment: serverId(档案ID)=" + consultantServerId
                        + ", userId(用户ID)=" + consultantUserId + ", parentId=" + parentUserId);

                // 构建JSON数据
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("consultantId", consultantServerId); // 用 consultants.id，不要用 users.id
                jsonObject.put("parentUserId", parentUserId);
                jsonObject.put("childName", childName);
                jsonObject.put("childAge", calculateAge(childId)); // 计算年龄
                jsonObject.put("appointmentDate", date);
                jsonObject.put("timeSlot", timeSlot);
                jsonObject.put("description", description);
                jsonObject.put("domain", domain);
                jsonObject.put("status", "PENDING");
                
                // 构造POST请求
                String jsonBody = jsonObject.toString();
                
                java.net.URL url = new java.net.URL(com.example.tongyangyuan.database.NetworkConfig.getBaseUrl() + "/appointments");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + preferenceStore.getAuthToken());
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                // 写入数据
                java.io.OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.close();

                // 读取响应
                int responseCode = conn.getResponseCode();
                if (responseCode == java.net.HttpURLConnection.HTTP_OK || responseCode == java.net.HttpURLConnection.HTTP_CREATED) {
                    Log.d("WebAppInterface", "预约提交成功");
                    
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    mainHandler.post(() -> {
                        showToast("预约提交成功，待咨询师确认");
                        navigateToMessageFragment();
                    });
                } else {
                    java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();
                    Log.e("WebAppInterface", "提交预约失败: " + responseCode + ", " + errorResponse.toString());
                    
                    String errorMsg = "服务器响应 " + responseCode;
                    try {
                         JSONObject errorJson = new JSONObject(errorResponse.toString());
                         if (errorJson.has("message")) {
                             errorMsg = errorJson.getString("message");
                         }
                    } catch (Exception e) {}
                    
                    final String finalErrorMsg = errorMsg;
                    mainHandler.post(() -> showToast("提交失败: " + finalErrorMsg));
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e("WebAppInterface", "提交预约到服务器失败", e);
                mainHandler.post(() -> showToast("网络错误: " + e.getMessage()));
            }
        }).start();
    }

    private void navigateToMessageFragment() {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("TARGET_FRAGMENT", "MESSAGE_FRAGMENT");
        context.startActivity(intent);
        finishHost();
    }

    private int calculateAge(String childId) {
        // 从childId或childProfile获取年龄
        try {
            // 先尝试从本地数据库获取
            ChildProfile profile = childProfileRepository.getProfileByIdSync(childId);
            if (profile != null && profile.getBirthDate() != null) {
                String birthDate = profile.getBirthDate();
                if (birthDate.length() >= 4) {
                    int birthYear = Integer.parseInt(birthDate.substring(0, 4));
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    return Math.max(0, currentYear - birthYear);
                }
            }
        } catch (Exception e) {
            Log.e("WebAppInterface", "计算年龄失败", e);
        }
        return 0; // 默认返回0
    }

    @JavascriptInterface
    public void openChat(String appointmentJson) {
        try {
            JSONObject obj = new JSONObject(appointmentJson);
            // id = 预约服务器端ID（用于 KEY_APPOINTMENT_ID，因为来自 message.html 的 safeId 就是 appointment.serverId）
            // 不能把预约ID当作咨询师ID传给 Consultant 构造函数
            String appointmentId = obj.optString("id", "");
            long consultantId = obj.optLong("consultantId", 0);
            String consultantName = obj.optString("consultantName", "咨询师");
            String childName = obj.optString("childName", "孩子");
            String date = obj.optString("date", "");
            String slot = obj.optString("timeSlot", "");

            // 构造 Consultant 对象（仅用于显示）
            // 用正确的 consultantId 作为 userId
            Consultant consultant = new Consultant(
                    consultantId,
                    consultantName,
                    "咨询师",
                    "", 0, "", "", new ArrayList<>(), "", new ArrayList<>()
            );

            mainHandler.post(() -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(ChatActivity.KEY_CONSULTANT, consultant);
                intent.putExtra(ChatActivity.KEY_APPOINTMENT_DATE, date);
                intent.putExtra(ChatActivity.KEY_APPOINTMENT_SLOT, slot);
                intent.putExtra(ChatActivity.KEY_APPOINTMENT_ID, appointmentId);
                intent.putExtra(ChatActivity.KEY_CHILD_NAME, childName);
                // 传递 serverAppointmentId（由 message.html 的 safeId 传入，它就是 appointment.serverId）
                intent.putExtra(ChatActivity.KEY_SERVER_ID, obj.optLong("id", -1));
                context.startActivity(intent);
            });
        } catch (JSONException e) {
            Log.e("WebAppInterface", "Failed to parse appointment json", e);
            showToast("打开详情失败");
        }
    }

    @JavascriptInterface
    public void confirmPayment(String plan, String payment) {
        // 处理支付
        preferenceStore.setPaidUser(true);
        showToast("充值成功，已开通咨询服务");
        mainHandler.post(this::finishHost);
    }

    @JavascriptInterface
    public String getChatHistory(String appointmentId) {
        if (TextUtils.isEmpty(appointmentId)) {
            return "[]";
        }
        List<ChatMessageRecord> messages = chatStore.getMessages(appointmentId);
        JSONArray jsonArray = new JSONArray();
        for (ChatMessageRecord record : messages) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("fromConsultant", record.isFromConsultant());
                obj.put("type", record.getType());
                obj.put("content", record.getContent());
                obj.put("mediaUri", record.getMediaUri() != null ? record.getMediaUri().toString() : "");
                obj.put("timestamp", record.getTimestamp());
                jsonArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    /**
     * 从服务器获取聊天历史记录并同步到本地存储
     * @param appointmentId 服务器端的 appointment ID（数字）
     * @return 服务器返回的历史消息 JSON 数组字符串
     */
    @JavascriptInterface
    public String getChatHistoryFromServer(String appointmentId) {
        if (TextUtils.isEmpty(appointmentId)) {
            return "[]";
        }
        
        try {
            long aptId = Long.parseLong(appointmentId.trim());
            String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
            java.net.URL url = new java.net.URL(baseUrl + "/messages/appointment/" + aptId);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            String token = preferenceStore.getAuthToken();
            if (!android.text.TextUtils.isEmpty(token)) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                // 解析服务器响应：{code: 200, data: [...]}
                org.json.JSONObject jsonResponse = new org.json.JSONObject(response.toString());
                if (jsonResponse.optInt("code") == 200 && jsonResponse.has("data")) {
                    org.json.JSONArray serverMessages = jsonResponse.getJSONArray("data");
                    JSONArray resultArray = new JSONArray();
                    
                    // 转换服务器消息格式并同步到本地
                    for (int i = 0; i < serverMessages.length(); i++) {
                        org.json.JSONObject msg = serverMessages.getJSONObject(i);
                        JSONObject converted = new JSONObject();
                        try {
                            // 服务器字段映射到前端需要的格式
                            converted.put("fromConsultant", msg.optBoolean("isFromConsultant", false));
                            converted.put("type", msg.optString("messageType", "TEXT").toLowerCase());
                            converted.put("content", msg.optString("content", ""));
                            converted.put("mediaUri", msg.optString("mediaUrl", ""));
                            
                            // 处理时间戳
                            long timestamp = msg.optLong("timestamp", 0);
                            if (timestamp == 0 && msg.has("createdAt")) {
                                // 尝试解析 createdAt 时间
                                String createdAt = msg.getString("createdAt");
                                try {
                                    java.time.Instant instant = java.time.Instant.parse(createdAt);
                                    timestamp = instant.toEpochMilli();
                                } catch (Exception e) {}
                            }
                            converted.put("timestamp", timestamp);
                            
                            resultArray.put(converted);
                            
                            // 同步到本地存储
                            ChatMessageRecord record = new ChatMessageRecord(
                                msg.optBoolean("isFromConsultant", false),
                                msg.optString("messageType", "TEXT").toLowerCase(),
                                msg.optString("content", ""),
                                null
                            );
                            chatStore.saveMessage(appointmentId, record);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    return resultArray.toString();
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "从服务器获取聊天历史失败", e);
        }
        
        // 服务器获取失败，返回本地数据
        return getChatHistory(appointmentId);
    }

    @JavascriptInterface
    public void saveChatMessage(String appointmentId, boolean fromConsultant,
                                String type, String content, String mediaUri) {
        if (TextUtils.isEmpty(appointmentId)) {
            return;
        }
        ChatMessageRecord record = new ChatMessageRecord(
                fromConsultant,
                type,
                content,
                !TextUtils.isEmpty(mediaUri) ? Uri.parse(mediaUri) : null
        );
        chatStore.saveMessage(appointmentId, record);
    }

    @JavascriptInterface
    public void markAppointmentChatted(String appointmentId) {
        if (!TextUtils.isEmpty(appointmentId)) {
            appointmentStore.markAsChatted(appointmentId);
        }
    }

    @JavascriptInterface
    public void pinAppointment(String appointmentId, boolean pinned) {
        if (TextUtils.isEmpty(appointmentId)) {
            return;
        }
        appointmentStore.setPinned(appointmentId, pinned);
        showToast(pinned ? "已置顶该咨询" : "已取消置顶");
    }

    @JavascriptInterface
    public void deleteAppointment(String appointmentId) {
        if (TextUtils.isEmpty(appointmentId)) {
            return;
        }
        appointmentStore.deleteAppointment(appointmentId);
    }

    /** 返回有过后端同步失败的本地预约 ID 列表（JSON 数组字符串） */
    @JavascriptInterface
    public String getSyncErrorIds() {
        String[] ids = appointmentStore.getSyncErrorIds();
        JSONArray arr = new JSONArray();
        for (String id : ids) arr.put(id);
        return arr.toString();
    }

    /** 返回本次同步发现的孤儿预约 ID 列表（从未入库，JSON 数组字符串） */
    @JavascriptInterface
    public String getOrphanIds() {
        String ids = appointmentStore.getOrphanIds();
        JSONArray arr = new JSONArray();
        if (!ids.isEmpty()) {
            for (String id : ids.split(",")) {
                if (!id.isEmpty()) arr.put(id);
            }
        }
        return arr.toString();
    }

    /** 清除孤儿标记（用户确认后调用） */
    @JavascriptInterface
    public void clearOrphanIds() {
        appointmentStore.clearOrphanIds();
    }

    @JavascriptInterface
    public String getAppointments() {
        // 立即返回本地缓存，避免 CountDownLatch 阻塞 WebView JS 线程导致整页卡死。
        List<AppointmentRecord> initialRecords = appointmentStore.getAllAppointments();
        JSONArray initialJsonArray = new JSONArray();
        try {
            for (AppointmentRecord apt : initialRecords) {
                JSONObject obj = recordToJsonObject(apt);
                initialJsonArray.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                long userId = preferenceStore.getUserId();
                if (userId <= 0) {
                    userId = 2;
                }

                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/appointments/parent/" + userId);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                String token = preferenceStore.getAuthToken();
                if (!android.text.TextUtils.isEmpty(token)) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                if (conn.getResponseCode() == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();

                    JSONObject response = new JSONObject(sb.toString());
                    if (response.optInt("code") == 200) {
                        org.json.JSONArray dataArray = response.optJSONArray("data");
                        if (dataArray != null) {
                            appointmentStore.updateFromServerJsonArray(dataArray);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("WebAppInterface", "Background sync appointments failed", e);
            }
        }, "sync-appointments").start();

        return initialJsonArray.toString();
    }

    private JSONObject recordToJsonObject(AppointmentRecord apt) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", apt.getId());
        obj.put("appointmentNo", apt.getId()); // 本地ID作为编号
        obj.put("date", apt.getDate());
        obj.put("appointmentDate", apt.getDate()); // 兼容字段
        obj.put("timeSlot", apt.getTimeSlot());
        obj.put("description", apt.getDescription());
        obj.put("status", apt.getStatus());
        obj.put("domain", apt.getDomain());
        obj.put("hasChatted", apt.hasChatted());
        obj.put("pinned", apt.isPinned());
        
        // 构造 consultantName 和 consultantId
        Consultant c = apt.getConsultant();
        obj.put("consultantId", c != null ? c.getUserId() : 0);
        obj.put("consultantName", c != null ? c.getName() : "咨询师");
        obj.put("childName", apt.getChildName());
        
        return obj;
    }

    @JavascriptInterface
    public void loginWithPhone(String phone, String code) {
        if (TextUtils.isEmpty(phone) || phone.length() != 11) {
            showToast("请填写11位手机号");
            return;
        }
        if (TextUtils.isEmpty(code) || code.length() != 6) {
            showToast("请填写6位验证码");
            return;
        }

        // 发起网络请求登录
        showToast("正在连接服务器...");
        new Thread(() -> {
            try {
                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/auth/login/code");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                Log.d("WebAppInterface", "Attempting login with phone: " + phone);
                String params = "phone=" + phone + "&code=" + code;
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(params.getBytes());
                }

                int responseCode = conn.getResponseCode();
                Log.d("WebAppInterface", "Login response code: " + responseCode);

                if (responseCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    Log.d("WebAppInterface", "Login response body: " + response.toString());
                    
                    JSONObject json = new JSONObject(response.toString());
                    if (json.getInt("code") == 200) {
                        JSONObject data = json.getJSONObject("data");
                        String token = data.getString("token");
                        long userId = data.getLong("userId");
                        
                        preferenceStore.setLoggedIn(true);
                        preferenceStore.setLastLoginPhone(phone);
                        preferenceStore.setAuthToken(token);
                        preferenceStore.setUserId(userId);

                        // 从服务器刷新真实会员状态
                        preferenceStore.refreshPaidStatusFromServer(context);

                        mainHandler.post(() -> {
                            showToast("登录成功");
                            proceedAfterLogin();
                        });
                    } else {
                        String msg = json.optString("message", "登录失败");
                        mainHandler.post(() -> showToast(msg));
                    }
                } else {
                    Log.e("WebAppInterface", "Server error: " + responseCode);
                    mainHandler.post(() -> showToast("服务器错误: " + responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("WebAppInterface", "Login exception", e);
                mainHandler.post(() -> showToast("网络请求失败: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * 发送验证码接口
     * @param phone 手机号
     */
    @JavascriptInterface
    public void sendVerifyCode(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() != 11) {
            showToast("请填写11位手机号");
            return;
        }
        
        new Thread(() -> {
            try {
                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/auth/sendCode");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "phone=" + phone;
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(params.getBytes());
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    Log.d("WebAppInterface", "Send code response: " + response.toString());
                    
                    JSONObject json = new JSONObject(response.toString());
                    if (json.getInt("code") == 200) {
                        mainHandler.post(() -> showToast("验证码已发送"));
                    } else {
                        String msg = json.optString("message", "发送失败");
                        mainHandler.post(() -> showToast(msg));
                    }
                } else {
                    mainHandler.post(() -> showToast("发送失败: " + responseCode));
                }
            } catch (Exception e) {
                Log.e("WebAppInterface", "Send code exception", e);
                mainHandler.post(() -> showToast("网络请求失败"));
            }
        }).start();
    }
    
    /**
     * 账号密码登录
     * @param account 账号（用户名或手机号）
     * @param password 密码
     */
    @JavascriptInterface
    public void loginWithPassword(String account, String password) {
        if (TextUtils.isEmpty(account)) {
            showToast("请输入账号");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showToast("请输入密码");
            return;
        }

        showToast("正在登录...");
        new Thread(() -> {
            try {
                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/auth/login/password");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "account=" + account + "&password=" + password;
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(params.getBytes());
                }

                int responseCode = conn.getResponseCode();
                Log.d("WebAppInterface", "Password login response: " + responseCode);

                if (responseCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    
                    JSONObject json = new JSONObject(response.toString());
                    if (json.getInt("code") == 200) {
                        JSONObject data = json.getJSONObject("data");
                        String token = data.getString("token");
                        long userId = data.getLong("userId");
                        String phone = data.optString("phone", account);
                        
                        preferenceStore.setLoggedIn(true);
                        preferenceStore.setLastLoginPhone(phone);
                        preferenceStore.setAuthToken(token);
                        preferenceStore.setUserId(userId);

                        // 从服务器刷新真实会员状态
                        preferenceStore.refreshPaidStatusFromServer(context);

                        mainHandler.post(() -> {
                            showToast("登录成功");
                            proceedAfterLogin();
                        });
                    } else {
                        String msg = json.optString("message", "登录失败");
                        mainHandler.post(() -> showToast(msg));
                    }
                } else {
                    mainHandler.post(() -> showToast("服务器错误: " + responseCode));
                }
            } catch (Exception e) {
                Log.e("WebAppInterface", "Password login exception", e);
                mainHandler.post(() -> showToast("网络请求失败: " + e.getMessage()));
            }
        }).start();
    }
    
    /**
     * 微信登录（由前端触发）
     */
    @JavascriptInterface
    public void loginWithWechat() {
        mainHandler.post(() -> {
            Activity activity = getActivity();
            if (activity == null) {
                showToast("无法打开微信授权");
                return;
            }
            Intent intent = new Intent(context, com.example.tongyangyuan.WechatScanActivity.class);
            activity.startActivityForResult(intent, REQUEST_WECHAT_LOGIN);
        });
    }

    private static final int REQUEST_WECHAT_LOGIN = 1001;

    /**
     * 由 Activity 的 onActivityResult 调用，传入微信授权结果
     */
    public void onWechatAuthResult(int resultCode, String openId, String nickname, String avatarUrl) {
        if (resultCode != Activity.RESULT_OK || openId == null || openId.trim().isEmpty()) {
            showToast("微信授权已取消");
            return;
        }
        doWechatLogin(openId, nickname, avatarUrl);
    }

    /**
     * 执行微信登录（由前端在授权成功后将 openId/nickname/avatar 发过来）
     */
    @JavascriptInterface
    public void doWechatLogin(String openId, String nickname, String avatarUrl) {
        if (openId == null || openId.trim().isEmpty()) {
            showToast("微信授权失败，请重试");
            return;
        }
        mainHandler.post(() -> showToast("正在登录..."));
        new Thread(() -> {
            try {
                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/auth/login/wechat");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject body = new JSONObject();
                body.put("openId", openId);
                body.put("nickname", nickname != null ? nickname : "");
                body.put("avatarUrl", avatarUrl != null ? avatarUrl : "");

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) response.append(line);
                    br.close();

                    JSONObject json = new JSONObject(response.toString());
                    if (json.getInt("code") == 200) {
                        JSONObject data = json.getJSONObject("data");
                        String token = data.getString("token");
                        long userId = data.getLong("userId");
                        String phone = data.optString("phone", "");
                        String userNickname = data.optString("nickname", nickname != null ? nickname : "微信用户");
                        String userAvatar = data.optString("avatarUrl", avatarUrl != null ? avatarUrl : "");

                        preferenceStore.setLoggedIn(true);
                        preferenceStore.setLastLoginPhone(phone);
                        preferenceStore.setAuthToken(token);
                        preferenceStore.setUserId(userId);

                        // 从服务器刷新真实会员状态
                        preferenceStore.refreshPaidStatusFromServer(context);

                        mainHandler.post(() -> {
                            showToast("微信登录成功");
                            proceedAfterLogin();
                        });
                    } else {
                        String msg = json.optString("message", "登录失败");
                        mainHandler.post(() -> showToast(msg));
                    }
                } else {
                    mainHandler.post(() -> showToast("服务器错误: " + responseCode));
                }
            } catch (Exception e) {
                Log.e("WebAppInterface", "WeChat login exception", e);
                mainHandler.post(() -> showToast("网络请求失败"));
            }
        }).start();
    }
    
    /**
     * 登录成功后的回调（由前端调用）
     * @param account 登录账号
     */
    @JavascriptInterface
    public void onLoginSuccess(String account) {
        preferenceStore.setLoggedIn(true);
        preferenceStore.setLastLoginPhone(account);
        // 从服务器刷新真实会员状态
        preferenceStore.refreshPaidStatusFromServer(context);
        mainHandler.post(() -> {
            proceedAfterLogin();
        });
    }
    
    /**
     * 登录失败回调
     * @param message 错误信息
     */
    @JavascriptInterface
    public void onLoginFailed(String message) {
        mainHandler.post(() -> showToast("登录失败: " + message));
    }

    @JavascriptInterface
    public String getBaseUrl() {
        return com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
    }

    @JavascriptInterface
    public String getAuthToken() {
        return preferenceStore.getAuthToken();
    }
    
    @JavascriptInterface
    public long getUserId() {
        return preferenceStore.getUserId();
    }

    // ==================== 当前操作孩子相关 ====================

    @JavascriptInterface
    public long getCurrentChildId() {
        return preferenceStore.getCurrentChildId();
    }

    /**
     * 当前孩子 id 的字符串形式：服务器数字 id 或本地 UUID，供 H5 与档案 id 做一致比较。
     */
    @JavascriptInterface
    public String getCurrentChildIdStr() {
        long n = preferenceStore.getCurrentChildId();
        if (n >= 0) {
            return String.valueOf(n);
        }
        String s = preferenceStore.getCurrentChildIdString();
        return s != null ? s : "";
    }

    @JavascriptInterface
    public String getCurrentChildName() {
        return preferenceStore.getCurrentChildName();
    }

    @JavascriptInterface
    public void setCurrentChild(String childId, String childName) {
        try {
            if (TextUtils.isEmpty(childId)) {
                showToast("切换孩子失败");
                return;
            }
            String trimmed = childId.trim();
            try {
                long id = Long.parseLong(trimmed);
                preferenceStore.setCurrentChildId(id);
            } catch (NumberFormatException e) {
                // 本地档案可能仍为 UUID，无法解析为 long
                preferenceStore.setCurrentChildId(-1);
                preferenceStore.setCurrentChildIdString(trimmed);
            }
            if (!TextUtils.isEmpty(childName)) {
                preferenceStore.setCurrentChildName(childName);
            }
            long syncId = preferenceStore.getCurrentChildId();
            if (syncId >= 0) {
                syncCurrentChildToServer(syncId);
            }
            Log.d(TAG, "已设置当前孩子: id=" + trimmed + ", name=" + childName);
        } catch (Exception e) {
            Log.e(TAG, "setCurrentChild error", e);
            showToast("切换孩子失败");
        }
    }

    @JavascriptInterface
    public String getCurrentChildInfo() {
        try {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("childId", preferenceStore.getCurrentChildId());
            obj.put("childIdStr", getCurrentChildIdStr());
            obj.put("childName", preferenceStore.getCurrentChildName());
            return obj.toString();
        } catch (Exception e) {
            Log.e(TAG, "getCurrentChildInfo error", e);
            return "{}";
        }
    }

    private void syncCurrentChildToServer(long childId) {
        new Thread(() -> {
            try {
                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/user/current-child");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + preferenceStore.getAuthToken());
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                org.json.JSONObject body = new org.json.JSONObject();
                body.put("childId", childId);
                java.io.OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "同步当前孩子到服务器: childId=" + childId + ", response=" + responseCode);
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "同步当前孩子失败", e);
            }
        }).start();
    }

    private void loadCurrentChildFromServer() {
        new Thread(() -> {
            try {
                String baseUrl = com.example.tongyangyuan.database.NetworkConfig.getBaseUrl();
                java.net.URL url = new java.net.URL(baseUrl + "/user/current-child");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + preferenceStore.getAuthToken());
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.BufferedReader br = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    org.json.JSONObject response = new org.json.JSONObject(sb.toString());
                    if (response.optInt("code") == 200 && response.has("data")) {
                        org.json.JSONObject data = response.getJSONObject("data");
                        if (data != null && data.has("id")) {
                            final long childId = data.getLong("id");
                            final String childName = data.optString("name", "");
                            mainHandler.post(() -> {
                                preferenceStore.setCurrentChildId(childId);
                                preferenceStore.setCurrentChildName(childName);
                                Log.d(TAG, "从服务器加载当前孩子: id=" + childId + ", name=" + childName);
                            });
                        }
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "加载当前孩子失败", e);
            }
        }).start();
    }

    @JavascriptInterface
    public boolean hasBadge(String badgeId) {
        return preferenceStore.hasBadge(badgeId);
    }

    @JavascriptInterface
    public void grantBadge(String badgeId, String badgeName) {
        preferenceStore.grantBadge(badgeId);
        if (!TextUtils.isEmpty(badgeName)) {
            showToast("已获得徽章：" + badgeName);
        }
    }


    @JavascriptInterface
    public String getChildProfiles() {
        String userPhone = preferenceStore.getLastLoginPhone();
        if (TextUtils.isEmpty(userPhone)) {
            return "[]";
        }
        List<ChildProfile> profiles = childProfileRepository.getProfilesByUserSync(userPhone);
        return childProfileRepository.profilesToJson(profiles);
    }

    @JavascriptInterface
    public void syncChildrenFromServer() {
        String userPhone = preferenceStore.getLastLoginPhone();
        if (TextUtils.isEmpty(userPhone)) {
            return;
        }
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        childProfileRepository.syncFromServer(userPhone, new com.example.tongyangyuan.child.ChildProfileRepository.SaveCallback() {
            @Override
            public void onSuccess() {
                latch.countDown();
            }

            @Override
            public void onError(Exception e) {
                latch.countDown();
            }
        });
        try {
            latch.await(2000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public String getChildProfileById(String childId) {
        if (TextUtils.isEmpty(childId)) {
            return "{}";
        }
        ChildProfile profile = childProfileRepository.getProfileByIdSync(childId);
        if (profile == null) {
            return "{}";
        }
        try {
            return profile.toJson().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    @JavascriptInterface
    public void saveChildInfo(String json) {
        if (TextUtils.isEmpty(json)) {
            showToast("请填写孩子信息");
            return;
        }
        String userPhone = preferenceStore.getLastLoginPhone();
        if (TextUtils.isEmpty(userPhone)) {
            showToast("请先登录");
            return;
        }
        try {
            JSONArray array = new JSONArray(json);
            if (array.length() == 0) {
                showToast("请至少添加一名孩子信息");
                return;
            }
            List<ChildProfile> profiles = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ChildProfile profile = ChildProfile.fromJson(obj);
                if (!profile.isCompleted()) {
                    showToast("请完善孩子信息后再保存");
                    return;
                }
                profiles.add(profile);
            }

            childProfileRepository.saveProfiles(userPhone, profiles, new com.example.tongyangyuan.child.ChildProfileRepository.SaveCallback() {
                @Override
                public void onSuccess() {
                    preferenceStore.setHasChildProfile(true);
                    showToast("孩子信息已保存并同步到云端");
                    
                    // 保存成功后跳转到主页 (MainActivity)
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                    // startActivity 之后 finish，防止点返回时 splash 动画闪回
                    finishHost();
                }

                @Override
                public void onError(Exception e) {
                    showToast("保存失败: " + e.getMessage());
                }
            });
        } catch (JSONException e) {
            showToast("孩子信息格式错误");
        }
    }

    private void proceedAfterLogin() {
        String userPhone = preferenceStore.getLastLoginPhone();
        if (!TextUtils.isEmpty(userPhone)) {
            childProfileRepository.syncFromServer(userPhone, new com.example.tongyangyuan.child.ChildProfileRepository.SaveCallback() {
                @Override
                public void onSuccess() {
                    Log.d("WebAppInterface", "Child profiles synced successfully");
                }

                @Override
                public void onError(Exception e) {
                    Log.e("WebAppInterface", "Failed to sync child profiles", e);
                }
            });
        }
        
        // 同步当前孩子信息
        loadCurrentChildFromServer();
        
        if (preferenceStore.hasChildProfile()) {
            // 已有孩子信息，进入主页 (MainActivity)
            Intent intent = new Intent(context, MainActivity.class);
            // 清除之前的任务栈，确保用户按返回键不会回到登录页
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            finishHost(); // startActivity 之后 finish，防止点返回时 splash 动画闪回
        } else {
            // 没有孩子信息，进入信息完善页
            openWebPage("child_info.html");
            // openWebPage 内部已调用 finishHost
        }
    }

    private Activity getActivity() {
        return context instanceof Activity ? (Activity) context : null;
    }

    private void finishHost() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    private void dispatchJs(String script) {
        WebView webView = webViewRef != null ? webViewRef.get() : null;
        if (webView == null) {
            return;
        }
        webView.post(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(script, null);
            } else {
                webView.loadUrl("javascript:" + script);
            }
        });
    }

    private JSONObject consultantToJson(Consultant consultant) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("userId", consultant.getUserId());
        obj.put("serverId", consultant.getServerId()); // 咨询师档案ID（appointments.consultant_id 引用此值）
        obj.put("name", consultant.getName());
        obj.put("title", consultant.getTitle());
        obj.put("specialty", consultant.getSpecialty());
        obj.put("rating", consultant.getRating());
        obj.put("servedCount", consultant.getServedCount());
        obj.put("avatarColor", consultant.getAvatarColor());
        obj.put("avatarUrl", consultant.getAvatarUrl() != null ? consultant.getAvatarUrl() : "");
        obj.put("intro", consultant.getIntro());
        obj.put("identityTier", consultant.getIdentityTier().name());
        obj.put("displayIdentityTag", consultant.getDisplayIdentityTag());
        JSONArray tags = new JSONArray();
        for (String tag : consultant.getIdentityTags()) {
            tags.put(tag);
        }
        obj.put("identityTags", tags);
        JSONArray reviews = new JSONArray();
        for (String review : consultant.getReviews()) {
            reviews.put(review);
        }
        obj.put("reviews", reviews);
        obj.put("userId", consultant.getUserId());
        return obj;
    }

    public void notifyMediaMessage(String type, String content, String mediaUri) {
        String safeType = escapeJs(type);
        String safeContent = escapeJs(content);
        String safeMedia = mediaUri != null ? escapeJs(mediaUri) : "";
        String script = String.format(
                Locale.US,
                "window.onMediaSelected && window.onMediaSelected('%s','%s','%s')",
                safeType,
                safeContent,
                safeMedia
        );
        dispatchJs(script);
    }

    /**
     * 从 VideoCallActivity 返回时由 Activity 调用：WebView 在后台时定时器不跑，需原生侧主动清横幅并拉取含「通话结束」的记录。
     */
    public void notifyVideoCallClosed() {
        mainHandler.post(() -> dispatchJs(
                "(function(){try{if(typeof dismissCallNotification==='function')dismissCallNotification();"
                        + "callState=null;if(typeof refreshChatHistoryFromNative==='function')"
                        + "refreshChatHistoryFromNative();else if(typeof loadHistoryMessages==='function')"
                        + "loadHistoryMessages();}catch(e){}})()"
        ));
    }


    @JavascriptInterface
    public void notifyVoiceRecordState(boolean recording) {
        if (!voiceRecordCallbackEnabled) return;
        mainHandler.post(() -> {
            if (webViewRef.get() != null) {
                webViewRef.get().evaluateJavascript("if (window.onVoiceRecordState) window.onVoiceRecordState(" + recording + ")", null);
            }
        });
    }

    private String escapeJs(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private Consultant extractConsultant(Intent intent) {
        if (intent == null) {
            return null;
        }
        Object serializable = intent.getSerializableExtra(AppointmentActivity.KEY_CONSULTANT);
        if (serializable instanceof Consultant) {
            return (Consultant) serializable;
        }
        String name = intent.getStringExtra("consultant_name");
        Consultant consultant = consultantRepository.findByNameSync(name);
        // 如果数据库没有找到，不再使用 fallback
        return consultant;
    }

    // ==================== OpenIM JavaScript接口（替代旧 NIM） ====================

    private com.example.tongyangyuan.openim.OpenIMService getOpenIMService() {
        if (openIMService == null) {
            openIMService = com.example.tongyangyuan.openim.OpenIMService.getInstance(context);
        }
        return openIMService;
    }

    /**
     * 初始化 OpenIM 服务
     */
    @JavascriptInterface
    public void initNIM() {
        mainHandler.post(() -> {
            getOpenIMService().init();
            showToast("IM服务已初始化");
        });
    }

    /**
     * 登录 OpenIM
     * @param accountId 账户ID
     * @param token 登录令牌（可为空，由后端自动获取）
     */
    @JavascriptInterface
    public void loginNIM(String accountId, String token) {
        getOpenIMService().login(accountId, token, new com.example.tongyangyuan.openim.OpenIMService.LoginCallback() {
            @Override
            public void onSuccess() {
                mainHandler.post(() -> {
                    showToast("IM登录成功");
                    dispatchJs("window.onNIMLoginSuccess && window.onNIMLoginSuccess()");
                });
            }

            @Override
            public void onFailed(int code) {
                mainHandler.post(() -> {
                    showToast("IM登录失败: " + code);
                    dispatchJs("window.onNIMLoginFailed && window.onNIMLoginFailed(" + code + ")");
                });
            }

            @Override
            public void onException(Throwable exception) {
                mainHandler.post(() -> {
                    showToast("IM登录异常: " + exception.getMessage());
                    dispatchJs("window.onNIMLoginError && window.onNIMLoginError('" + escapeJs(exception.getMessage()) + "')");
                });
            }
        });
    }

    /**
     * 退出登录
     */
    @JavascriptInterface
    public void logoutNIM() {
        if (openIMService != null) {
            openIMService.logout();
            showToast("已退出IM");
            dispatchJs("window.onNIMLogout && window.onNIMLogout()");
        }
    }

    /**
     * 检查是否已登录
     */
    @JavascriptInterface
    public boolean isNIMLoggedIn() {
        return openIMService != null && openIMService.isLoggedIn();
    }

    /**
     * 发送文本消息
     */
    @JavascriptInterface
    public void sendNIMTextMessage(String targetAccountId, String content) {
        if (openIMService != null && openIMService.isLoggedIn()) {
            openIMService.sendTextMessage(targetAccountId, content);
            dispatchJs("window.onNIMMessageSent && window.onNIMMessageSent('" + escapeJs(content) + "')");
        } else {
            showToast("请先登录IM");
        }
    }

    /**
     * 发送图片消息
     */
    @JavascriptInterface
    public void sendNIMImageMessage(String targetAccountId, String imagePath) {
        if (openIMService != null && openIMService.isLoggedIn()) {
            openIMService.sendImageMessage(targetAccountId, imagePath);
            showToast("图片消息已发送");
        } else {
            showToast("请先登录IM");
        }
    }

    /**
     * 发起音视频通话（信令走 OpenIM，媒体走 LiveKit）
     */
    @JavascriptInterface
    public void startNIMCall(String targetAccountId, String callType) {
        if (openIMService != null && openIMService.isLoggedIn()) {
            String sessionId = openIMService.startCall(targetAccountId, callType, 0L);
            showToast("正在呼叫...");
            dispatchJs("window.onNIMCallStarted && window.onNIMCallStarted('" + escapeJs(sessionId) + "')");
        } else {
            showToast("请先登录IM");
        }
    }

    /**
     * 结束通话
     */
    @JavascriptInterface
    public void endNIMCall() {
        if (openIMService != null) {
            openIMService.endCall();
            showToast("通话已结束");
            dispatchJs("window.onNIMCallEnded && window.onNIMCallEnded()");
        }
    }

    /**
     * 接听来电
     */
    @JavascriptInterface
    public void acceptNIMCall(String sessionId, String callType) {
        if (openIMService != null) {
            openIMService.acceptCall(sessionId, callType);
            showToast("已接听");
        }
    }

    /**
     * 拒绝来电
     */
    @JavascriptInterface
    public void rejectNIMCall(String sessionId) {
        if (openIMService != null) {
            openIMService.rejectCall(sessionId);
            showToast("已拒绝");
        }
    }

    /**
     * 获取 OpenIM WebSocket 地址
     */
    @JavascriptInterface
    public String getNIMAppKey() {
        return openIMService != null ? openIMService.getWsUrl() : "";
    }

    /**
     * 获取当前账户ID
     */
    @JavascriptInterface
    public String getNIMAccountId() {
        if (openIMService != null) {
            return openIMService.getCurrentAccountId();
        }
        return "";
    }
}
