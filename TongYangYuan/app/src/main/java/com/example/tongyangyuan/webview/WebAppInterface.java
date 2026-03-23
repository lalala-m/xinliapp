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

    // ==================== 旧 NIM 配置已移除 ====================

    public interface MediaDelegate {
        void pickImage();
        void captureImage();
        void pickVideo();
        void captureVideo();
        void startVoiceRecord();
        void stopVoiceRecord();
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
    private final WeakReference<WebView> webViewRef;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        AppointmentRecord record = appointmentStore.getAppointmentById(appointmentId);
        if (record == null) {
            try {
                long serverId = Long.parseLong(appointmentId);
                record = appointmentStore.getAppointmentByServerId(serverId);
            } catch (NumberFormatException e) {
            }
        }
        if (record != null) {
            String status = record.getStatus();
            if ("CANCELLED".equalsIgnoreCase(status)) {
                showToast("该咨询已取消，无法进入聊天");
                return;
            }
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra(ChatActivity.KEY_CONSULTANT, record.getConsultant());
            intent.putExtra(ChatActivity.KEY_APPOINTMENT_DATE, record.getDate());
            intent.putExtra(ChatActivity.KEY_APPOINTMENT_SLOT, record.getTimeSlot());
            intent.putExtra(ChatActivity.KEY_APPOINTMENT_ID, record.getId());
            intent.putExtra(ChatActivity.KEY_CHILD_ID, record.getChildId());
            intent.putExtra(ChatActivity.KEY_CHILD_NAME, record.getChildName());
            intent.putExtra("chat_server_id", record.getServerId());
            context.startActivity(intent);
        } else {
            showToast("未找到对应的预约记录，请稍后刷新后重试");
        }
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
    public void playMedia(String type, String uri) {
        if (context instanceof MediaDelegate) {
            ((MediaDelegate) context).playMedia(type, uri);
        }
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
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_IS_CALLER, false);
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
        intent.putExtra(com.example.tongyangyuan.VideoCallActivity.KEY_IS_CALLER, false);
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

    @JavascriptInterface
    public void openAccountManagement() {
        openWebPage("account_manage.html");
    }

    @JavascriptInterface
    public void openChildManagement() {
        openWebPage("child_info.html");
    }

    @JavascriptInterface
    public void logoutAccount() {
        preferenceStore.setLoggedIn(false);
        preferenceStore.setPaidUser(false);
        preferenceStore.setLastLoginPhone("");
        showToast("已退出登录");
    }

    @JavascriptInterface
    public String getUserProfile() {
        try {
            JSONObject obj = new JSONObject();
            String phone = preferenceStore.getLastLoginPhone();
            obj.put("phone", phone != null ? phone : "");
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
                
                if (finalConsultant.getUserId() <= 0) {
                     Log.w("WebAppInterface", "Consultant userId is missing/invalid: " + finalConsultant.getUserId());
                }
                
                long consultantId = finalConsultant.getUserId();
                Log.d("WebAppInterface", "Submitting appointment: consultantId=" + consultantId + ", parentId=" + parentUserId);

                // 构建JSON数据
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("consultantId", consultantId);
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
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("TARGET_FRAGMENT", "MESSAGE_FRAGMENT");
            activity.startActivity(intent);
            activity.finish();
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("TARGET_FRAGMENT", "MESSAGE_FRAGMENT");
            context.startActivity(intent);
        }
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
            String appointmentId = obj.getString("id");
            String consultantName = obj.optString("consultantName", "咨询师");
            String childName = obj.optString("childName", "孩子");
            String date = obj.optString("date", "");
            String slot = obj.optString("timeSlot", "");
            
            // 构造 Consultant 对象 (部分信息)
            Consultant consultant = new Consultant(
                    obj.optLong("consultantId"),
                    consultantName,
                    "咨询师", // title unknown
                    "", 0, "", "", new ArrayList<>(), "", new ArrayList<>()
            );

            mainHandler.post(() -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(ChatActivity.KEY_CONSULTANT, consultant);
                intent.putExtra(ChatActivity.KEY_APPOINTMENT_DATE, date);
                intent.putExtra(ChatActivity.KEY_APPOINTMENT_SLOT, slot);
                intent.putExtra(ChatActivity.KEY_APPOINTMENT_ID, appointmentId);
                intent.putExtra(ChatActivity.KEY_CHILD_NAME, childName);
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

    @JavascriptInterface
    public String getAppointments() {
        // 先准备一个默认结果（当前本地缓存），如果服务器超时就用这个
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

        final String[] result = {initialJsonArray.toString()};
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

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
                conn.setConnectTimeout(3000);

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

                            List<AppointmentRecord> updatedRecords = appointmentStore.getAllAppointments();
                            JSONArray updatedJson = new JSONArray();
                            try {
                                for (AppointmentRecord apt : updatedRecords) {
                                    JSONObject obj = recordToJsonObject(apt);
                                    updatedJson.put(obj);
                                }
                                result[0] = updatedJson.toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("WebAppInterface", "Failed to fetch appointments from server, using local cache", e);
            } finally {
                latch.countDown();
            }
        }).start();

        try {
            // 等待服务器响应，最多等待 2 秒。如果超时，latch 未倒数，继续执行，返回本地数据
            latch.await(2000, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return result[0];
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
        obj.put("consultantId", apt.getConsultant().getUserId());
        obj.put("consultantName", apt.getConsultant().getName());
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
        
        if (preferenceStore.hasChildProfile()) {
            // 已有孩子信息，进入主页 (MainActivity)
            Intent intent = new Intent(context, MainActivity.class);
            // 清除之前的任务栈，确保用户按返回键不会回到登录页
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else {
            // 没有孩子信息，进入信息完善页
            openWebPage("child_info.html");
        }
        finishHost();
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


    @JavascriptInterface
    public void notifyVoiceRecordState(boolean recording) {
        if (!voiceRecordCallbackEnabled) return;
        mainHandler.post(() -> {
            if (webViewRef.get() != null) {
                webViewRef.get().evaluateJavascript("if (window.onVoiceRecordStateChanged) window.onVoiceRecordStateChanged(" + recording + ")", null);
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

    private void openWebPage(String htmlFile) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, htmlFile);
        context.startActivity(intent);
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
            String sessionId = openIMService.startCall(targetAccountId, callType);
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
