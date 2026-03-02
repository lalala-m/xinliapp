package com.example.tongyangyuan;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tongyangyuan.webview.WebViewActivity;

/**
 * Legacy entry kept for compatibility. Now simply opens the WebView version
 * of the "孩子信息管理" 页面.
 */
public class ChildInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_HTML_FILE, "child_info.html");
        startActivity(intent);
        finish();
    }
}

