package com.example.tongyangyuan.consult;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.tongyangyuan.R;

import java.util.List;

public class ConsultantAdapter extends RecyclerView.Adapter<ConsultantAdapter.ConsultantViewHolder> {

    public interface ConsultantListener {
        void onConsultantClick(Consultant consultant);
    }

    private final List<Consultant> data;
    private final ConsultantListener listener;

    public ConsultantAdapter(List<Consultant> data, ConsultantListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConsultantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consultant, parent, false);
        return new ConsultantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultantViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ConsultantViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgAvatar;
        private final TextView tvName;
        private final TextView tvTitle;
        private final TextView tvRating;
        private final TextView tvServed;
        private final TextView tvSpecialty;
        private final LinearLayout tagGroup;
        private final LayoutInflater inflater;

        ConsultantViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvServed = itemView.findViewById(R.id.tvServed);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tagGroup = itemView.findViewById(R.id.tagGroup);
            inflater = LayoutInflater.from(itemView.getContext());
        }

        void bind(Consultant consultant) {
            tvName.setText(consultant.getName());
            tvTitle.setText(consultant.getTitle());
            tvRating.setText(itemView.getContext().getString(R.string.consultant_rating, consultant.getRating()));
            tvServed.setText(itemView.getContext().getString(R.string.consultant_served, consultant.getServedCount()));
            tvSpecialty.setText(consultant.getSpecialty());
            renderTags(consultant);
            
            // 使用 Glide 加载真实头像或使用颜色
            String avatarUrl = consultant.getAvatarUrl();
            if (!TextUtils.isEmpty(avatarUrl)) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop())
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person))
                        .into(imgAvatar);
            } else {
                // 无头像时使用颜色
                imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                int colorRes = "green".equals(consultant.getAvatarColor()) ? R.color.brand_green_deep : R.color.brand_blue_deep;
                imgAvatar.setColorFilter(ContextCompat.getColor(itemView.getContext(), colorRes));
            }

            itemView.setOnClickListener(v -> animateCard(v, () -> {
                if (listener != null) {
                    listener.onConsultantClick(consultant);
                }
            }));
        }

        private void animateCard(View target, Runnable endAction) {
            target.animate()
                    .scaleX(0.98f)
                    .scaleY(0.98f)
                    .setDuration(90)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> target.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(140)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(endAction)
                            .start())
                    .start();
        }

        private void renderTags(Consultant consultant) {
            tagGroup.removeAllViews();
            String tag = consultant.getDisplayIdentityTag();
            if (TextUtils.isEmpty(tag)) {
                tagGroup.setVisibility(View.GONE);
                return;
            }
            tagGroup.setVisibility(View.VISIBLE);
                TextView chip = (TextView) inflater.inflate(R.layout.view_identity_tag, tagGroup, false);
                chip.setText(tag);
                tagGroup.addView(chip);
        }
    }
}

