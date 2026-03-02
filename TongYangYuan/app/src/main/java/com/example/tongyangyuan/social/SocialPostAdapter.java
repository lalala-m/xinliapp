package com.example.tongyangyuan.social;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tongyangyuan.R;

import java.util.ArrayList;
import java.util.List;

public class SocialPostAdapter extends RecyclerView.Adapter<SocialPostAdapter.PostViewHolder> {

    private final List<SocialPost> posts = new ArrayList<>();

    public void setPosts(List<SocialPost> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView postImageView;
        private final TextView postTitleTextView;
        private final ImageView authorAvatarImageView;
        private final TextView authorNameTextView;
        private final ImageView likeButton;
        private final TextView likeCountTextView;
        private final TextView consultantTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
            postTitleTextView = itemView.findViewById(R.id.postTitleTextView);
            authorAvatarImageView = itemView.findViewById(R.id.authorAvatarImageView);
            authorNameTextView = itemView.findViewById(R.id.authorNameTextView);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeCountTextView = itemView.findViewById(R.id.likeCountTextView);
            consultantTextView = itemView.findViewById(R.id.consultantTextView);
        }

        public void bind(SocialPost post) {
            // Here we would use a library like Glide or Picasso to load images from URLs.
            // For now, we'll just set placeholders.
            // postImageView.setImageResource(R.drawable.ic_launcher_background);
            // authorAvatarImageView.setImageResource(R.drawable.ic_person);

            postTitleTextView.setText(post.getTitle());
            authorNameTextView.setText(post.getAuthorName());
            likeCountTextView.setText(String.valueOf(post.getLikeCount()));
            consultantTextView.setText("由 " + post.getConsultantName() + " 指导");
        }
    }
}

