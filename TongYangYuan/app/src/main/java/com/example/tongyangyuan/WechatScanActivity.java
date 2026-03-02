package com.example.tongyangyuan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class WechatScanActivity extends AppCompatActivity {

    public static final String KEY_PHONE = "wechat_phone";

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
            String phone = getIntent().getStringExtra(KEY_PHONE);
            if (phone == null || phone.length() != 11) {
                phone = "13800000000";
            }
            data.putExtra(KEY_PHONE, phone);
            setResult(RESULT_OK, data);
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}

