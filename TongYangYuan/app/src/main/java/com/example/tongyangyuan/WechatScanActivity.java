package com.example.tongyangyuan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class WechatScanActivity extends AppCompatActivity {

    public static final String KEY_PHONE = "wechat_phone";
    public static final String KEY_OPEN_ID = "wechat_open_id";
    public static final String KEY_NICKNAME = "wechat_nickname";
    public static final String KEY_AVATAR = "wechat_avatar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wechat_scan);

        TextView title = findViewById(R.id.tvToolbarCenter);
        if (title != null) {
            title.setText(R.string.wechat_scan_title);
        }

        MaterialButton btnAuthorize = findViewById(R.id.btnAuthorize);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        btnAuthorize.setOnClickListener(v -> {
            Intent data = new Intent();
            // 模拟微信授权返回的 openId（正式上线需集成微信 SDK 替换此处）
            String mockOpenId = "wxmock_" + System.currentTimeMillis();
            String mockNickname = "微信用户";
            String mockAvatar = "";
            data.putExtra(KEY_OPEN_ID, mockOpenId);
            data.putExtra(KEY_NICKNAME, mockNickname);
            data.putExtra(KEY_AVATAR, mockAvatar);
            setResult(RESULT_OK, data);
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}

