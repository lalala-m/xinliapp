package com.example.tongyangyuan.webview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tongyangyuan.R;
import com.just.agentweb.AgentWeb;

import java.util.ArrayList;
import java.util.List;

public class WebViewActivity extends AppCompatActivity {
    public static final String EXTRA_HTML_FILE = "html_file";
    private AgentWeb mAgentWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_webview);

        String htmlFile = getIntent().getStringExtra(EXTRA_HTML_FILE);
        if (htmlFile == null) {
            htmlFile = "index.html";
        }

        ViewGroup container = findViewById(R.id.container);

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(container, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(null);

        // 注入 JS 接口
        mAgentWeb.getJsInterfaceHolder().addJavaObject("Android", new WebAppInterface(this, mAgentWeb.getWebCreator().getWebView()));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!mAgentWeb.back()) {
                    finish();
                }
            }
        });

        loadHtmlFile(htmlFile);
    }

    private void loadHtmlFile(String htmlFile) {
        String url = "file:///android_asset/" + htmlFile;

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.size() > 1) {
            StringBuilder urlBuilder = new StringBuilder(url);
            boolean firstParam = true;

            String selectedQuestions = extras.getString("selected_questions");
            if (selectedQuestions != null) {
                urlBuilder.append(firstParam ? "?" : "&");
                urlBuilder.append("questions").append("=")
                        .append(android.net.Uri.encode(selectedQuestions));
                firstParam = false;
            }

            for (String key : extras.keySet()) {
                if (!EXTRA_HTML_FILE.equals(key) && !"selected_questions".equals(key)) {
                    String value = extras.getString(key);
                    if (value != null) {
                        urlBuilder.append(firstParam ? "?" : "&");
                        urlBuilder.append(key).append("=")
                                .append(android.net.Uri.encode(value));
                        firstParam = false;
                    }
                }
            }
            url = urlBuilder.toString();
        }

        mAgentWeb.getUrlLoader().loadUrl(url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mAgentWeb.getWebLifeCycle().onDestroy();
        super.onDestroy();
    }
}

