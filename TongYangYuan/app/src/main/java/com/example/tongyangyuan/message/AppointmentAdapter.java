package com.example.tongyangyuan.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tongyangyuan.R;
import com.example.tongyangyuan.data.AppointmentRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private List<AppointmentRecord> appointments;
    private final OnAppointmentClickListener listener;

    public interface OnAppointmentClickListener {
        void onAppointmentClick(AppointmentRecord appointment);
    }

    public AppointmentAdapter(List<AppointmentRecord> appointments, OnAppointmentClickListener listener) {
        this.appointments = new ArrayList<>(appointments);
        this.listener = listener;
    }

    public void updateAppointments(List<AppointmentRecord> appointments) {
        this.appointments = new ArrayList<>(appointments);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppointmentRecord appointment = appointments.get(position);
        holder.bind(appointment);
        holder.itemView.setOnClickListener(v -> listener.onAppointmentClick(appointment));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvConsultantName;
        private final TextView tvAppointmentTime;
        private final TextView tvDescription;
        private final TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConsultantName = itemView.findViewById(R.id.tvConsultantName);
            tvAppointmentTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(AppointmentRecord appointment) {
            tvConsultantName.setText(appointment.getConsultant().getName() + " 咨询师");
            tvAppointmentTime.setText(appointment.getDate() + " " + appointment.getTimeSlot());
            tvDescription.setText(appointment.getDescription());

            String status = appointment.getStatus();
            String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
            if (appointment.hasChatted() || "COMPLETED".equals(normalized)) {
                tvStatus.setText("已沟通");
            } else if (normalized.isEmpty() || "PENDING".equals(normalized)) {
                tvStatus.setText("待确认");
            } else if ("CANCELLED".equals(normalized)) {
                tvStatus.setText("已取消");
            } else {
                tvStatus.setText("待沟通");
            }
        }
    }
}

