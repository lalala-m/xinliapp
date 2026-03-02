package com.example.tongyangyuan.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.tongyangyuan.webview.WebViewFragment;

public class SocialFragment extends WebViewFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArguments(createArguments("social.html"));
    }

    private Bundle createArguments(String htmlFile) {
        Bundle args = new Bundle();
        args.putString("html_file", htmlFile); // 注意：WebViewFragment 中使用的是 private static final String ARG_HTML_FILE = "html_file";
        // 由于 ARG_HTML_FILE 是 private，我们需要确保 key 字符串一致，或者 WebViewFragment 提供了 setter/factory。
        // 查看 WebViewFragment 源码，key 是 "html_file"。
        return args;
    }
}
