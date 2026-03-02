package com.example.tongyangyuan.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tongyangyuan.webview.WebViewFragment;

public class ProfileFragment extends WebViewFragment {
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArguments(createArguments("profile.html"));
    }
    
    private Bundle createArguments(String htmlFile) {
        Bundle args = new Bundle();
        args.putString("html_file", htmlFile);
        return args;
    }
}

