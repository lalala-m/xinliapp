package com.example.tongyangyuan.webview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tongyangyuan.R;
import com.just.agentweb.AgentWeb;

import java.util.ArrayList;
import java.util.List;

public class WebViewFragment extends Fragment {
    private static final String ARG_HTML_FILE = "html_file";
    private AgentWeb mAgentWeb;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static WebViewFragment newInstance(String htmlFile) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HTML_FILE, htmlFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        ViewGroup container = view.findViewById(R.id.container);

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(container, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()
                .go(null);

        // 允许 file:// 页面发起跨域请求（SockJS 需要）
        android.webkit.WebSettings webSettings = mAgentWeb.getWebCreator().getWebView().getSettings();
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);

        // 注入 JS 接口
        mAgentWeb.getJsInterfaceHolder().addJavaObject("Android", new WebAppInterface(requireContext(), mAgentWeb.getWebCreator().getWebView()));

        String htmlFile = getArguments() != null ? getArguments().getString(ARG_HTML_FILE, "index.html") : "index.html";
        loadHtmlFile(htmlFile);

        if (swipeRefreshLayout != null) {
            WebView webView = mAgentWeb != null ? mAgentWeb.getWebCreator().getWebView() : null;
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (webView != null) {
                    webView.evaluateJavascript(
                        "if (typeof refreshHome === 'function') { refreshHome(); } else if (typeof loadAppointments === 'function') { loadAppointments(true); } else { location.reload(); }",
                        null
                    );
                }
                swipeRefreshLayout.setRefreshing(false);
            });
            // 仅当页面滚动到顶部时才允许下拉刷新，避免与上滑滚动冲突
            if (webView != null) {
                swipeRefreshLayout.setOnChildScrollUpCallback((parent, child) -> {
                    if (webView.getScrollY() > 0) {
                        return true; // 可继续上滑 = 不触发刷新
                    }
                    return false; // 在顶部 = 允许刷新
                });
            }
        }
    }

    private void loadHtmlFile(String htmlFile) {
        String url = "file:///android_asset/" + htmlFile;
        mAgentWeb.getUrlLoader().loadUrl(url);
    }

    @Override
    public void onPause() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onResume();
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onDestroy();
        }
        super.onDestroyView();
    }
}

