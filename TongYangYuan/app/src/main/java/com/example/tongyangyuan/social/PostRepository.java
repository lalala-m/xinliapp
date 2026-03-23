package com.example.tongyangyuan.social;

import android.content.Context;

import com.example.tongyangyuan.database.NetworkConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostRepository {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface PostsCallback {
        void onSuccess(List<SocialPost> posts);
        void onError(Exception e);
    }

    public void getPosts(PostsCallback callback) {
        executorService.execute(() -> {
            try {
                String baseUrl = NetworkConfig.getBaseUrl();
                URL url = new URL(baseUrl + "/posts");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray content = jsonResponse.getJSONArray("content");
                    List<SocialPost> posts = new ArrayList<>();

                    for (int i = 0; i < content.length(); i++) {
                        JSONObject item = content.getJSONObject(i);
                        SocialPost post = new SocialPost(
                                item.getString("id"),
                                item.optString("imageUrl", null),
                                item.getString("title"),
                                item.optString("authorAvatarUrl", null),
                                item.getString("authorName"),
                                item.getInt("likeCount"),
                                item.optString("consultantName", "无"),
                                false // isLiked status would need a separate check
                        );
                        posts.add(post);
                    }
                    if (callback != null) callback.onSuccess(posts);
                } else {
                    if (callback != null) callback.onError(new Exception("Network error: " + conn.getResponseCode()));
                }
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }
}

