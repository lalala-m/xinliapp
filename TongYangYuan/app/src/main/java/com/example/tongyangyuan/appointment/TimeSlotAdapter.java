package com.example.tongyangyuan.appointment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tongyangyuan.R;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    public interface SlotListener {
        void onSlotSelected(String slot);
    }

    private final List<String> data = new ArrayList<>();
    private int selectedPosition = RecyclerView.NO_POSITION;
    private final SlotListener listener;

    public TimeSlotAdapter(SlotListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<String> slots) {
        data.clear();
        data.addAll(slots);
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public String getSelectedSlot() {
        if (selectedPosition == RecyclerView.NO_POSITION || selectedPosition >= data.size()) {
            return null;
        }
        return data.get(selectedPosition);
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        holder.bind(data.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class TimeSlotViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSlot;

        TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlot = (TextView) itemView;
        }

        void bind(String slot, boolean selected) {
            tvSlot.setText(slot);
            tvSlot.setSelected(selected);
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                int old = selectedPosition;
                selectedPosition = position;
                if (old != RecyclerView.NO_POSITION) {
                    notifyItemChanged(old);
                }
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onSlotSelected(slot);
                }
            });
        }
    }
}

