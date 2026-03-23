package com.example.tongyangyuan.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.webkit.WebView;

import com.example.tongyangyuan.webview.WebViewFragment;
import com.example.tongyangyuan.R;

public class MessageFragment extends WebViewFragment {
    
    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance() {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString("html_file", "message.html");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            setArguments(createArguments("message.html"));
        }
    }
    
    private Bundle createArguments(String htmlFile) {
        Bundle args = new Bundle();
        args.putString("html_file", htmlFile);
        return args;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // AgentWeb handles lifecycle automatically, no need to manually reload webview here
        // If specific logic needed, use AgentWeb instance if accessible
    }
}

