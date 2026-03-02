package com.example.tongyangyuan;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tongyangyuan.consult.Consultant;
import com.example.tongyangyuan.webview.WebViewActivity;

public class AppointmentActivity extends AppCompatActivity {

    public static final String KEY_CONSULTANT = "appointment_consultant";
    private Consultant consultant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        consultant = (Consultant) getIntent().getSerializableExtra(KEY_CONSULTANT);
        
        // 使用WebView版本
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "appointment.html");
        if (consultant != null) {
            intent.putExtra("consultant_name", consultant.getName());
            intent.putExtra(KEY_CONSULTANT, consultant);
        }
        startActivity(intent);
        finish();
    }
}
