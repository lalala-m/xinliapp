package com.example.tongyangyuan.child;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tongyangyuan.R;

import java.util.Calendar;

public class ChildDatePickerDialog extends Dialog {

    public interface OnDateSelectedListener {
        void onDateSelected(String dateString);
    }

    public ChildDatePickerDialog(@NonNull Context context,
                                 String preselected,
                                 OnDateSelectedListener listener) {
        this(context, preselected, listener, -1, -1);
    }

    public ChildDatePickerDialog(@NonNull Context context,
                                 String preselected,
                                 OnDateSelectedListener listener,
                                 long minDateMillis,
                                 long maxDateMillis) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_date_picker);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        setCancelable(true);

        DatePicker datePicker = findViewById(R.id.datePicker);
        TextView btnCancel = findViewById(R.id.btnCancel);
        TextView btnConfirm = findViewById(R.id.btnConfirm);

        Calendar calendar = Calendar.getInstance();
        if (preselected != null && preselected.contains("-")) {
            String[] split = preselected.split("-");
            if (split.length == 3) {
                int year = Integer.parseInt(split[0]);
                int month = Integer.parseInt(split[1]) - 1;
                int day = Integer.parseInt(split[2]);
                calendar.set(year, month, day);
            }
        }

        datePicker.updateDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        if (minDateMillis > 0) {
            datePicker.setMinDate(minDateMillis);
        }
        if (maxDateMillis > 0) {
            datePicker.setMaxDate(maxDateMillis);
        }

        btnCancel.setOnClickListener(v -> dismiss());
        btnConfirm.setOnClickListener(v -> {
            int year = datePicker.getYear();
            int month = datePicker.getMonth() + 1;
            int day = datePicker.getDayOfMonth();
            String formatted = String.format("%04d-%02d-%02d", year, month, day);
            listener.onDateSelected(formatted);
            dismiss();
        });
    }
}

