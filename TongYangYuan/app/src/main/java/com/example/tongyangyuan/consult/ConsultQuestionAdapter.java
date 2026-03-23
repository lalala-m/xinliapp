package com.example.tongyangyuan.consult;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import com.example.tongyangyuan.R;

import java.util.ArrayList;
import java.util.List;

public class ConsultQuestionAdapter extends RecyclerView.Adapter<ConsultQuestionAdapter.QuestionViewHolder> {

    public interface QuestionListener {
        void onQuestionSelected(String question);
    }

    private final List<String> data = new ArrayList<>();
    private final List<String> selected = new ArrayList<>();
    private QuestionListener listener;
    private boolean multiSelect = false;

    public ConsultQuestionAdapter(QuestionListener listener) {
        this.listener = listener;
    }

    public void setQuestions(List<String> questions, boolean multiSelect) {
        data.clear();
        data.addAll(questions);
        this.multiSelect = multiSelect;
        selected.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consult_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        String question = data.get(position);
        holder.bind(question, selected.contains(question));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvQuestion;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
        }

        void bind(String content, boolean isSelected) {
            tvQuestion.setText(content);
            MaterialCardView card = (MaterialCardView) itemView;
            card.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(),
                    isSelected ? R.color.brand_blue_deep : R.color.brand_beige));
            tvQuestion.setTextColor(ContextCompat.getColor(itemView.getContext(),
                    isSelected ? R.color.white : R.color.brand_gray));
            itemView.setOnClickListener(v -> {
                if (multiSelect) {
                    if (selected.contains(content)) {
                        selected.remove(content);
                    } else {
                        selected.add(content);
                    }
                } else {
                    selected.clear();
                    selected.add(content);
                    notifyDataSetChanged();
                }
                notifyItemChanged(getBindingAdapterPosition());
                if (listener != null) {
                    listener.onQuestionSelected(content);
                }
            });
        }
    }
}

