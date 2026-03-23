package com.example.tongyangyuan.child;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tongyangyuan.R;
import com.example.tongyangyuan.util.SimpleTextWatcher;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChildInfoAdapter extends RecyclerView.Adapter<ChildInfoAdapter.ChildViewHolder> {

    public interface ChildActionListener {
        void onRequestBirthDate(int position);

        void onDelete(int position);

        void onProfileUpdated();
    }

    private final List<ChildProfile> data;
    private final ChildActionListener listener;

    public ChildInfoAdapter(List<ChildProfile> data, ChildActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child_info, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        holder.bind(data.get(position), position, data.size() > 1);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ChildViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvChildIndex;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;
        private final EditText etName;
        private final TextView tvBoy;
        private final TextView tvGirl;
        private final TextView tvBirth;
        private final EditText etEthnicity;
        private final EditText etNativePlace;
        private final EditText etFamilyRank;
        private final EditText etBirthPlace;
        private final EditText etLanguageEnv;
        private final EditText etSchool;
        private final EditText etAddress;
        private final EditText etInterests;
        private final EditText etActivities;
        private final ChipGroup chipGroupBodyStatus;
        private final LinearLayout layoutBodyStatusDetail;
        private final EditText etBodyStatusDesc;
        private final ChipGroup chipGroupMedicalHistory;
        private final LinearLayout layoutMedicalOther;
        private final EditText etMedicalOther;
        private final EditText etFatherPhone;
        private final EditText etMotherPhone;
        private final EditText etGuardianPhone;

        private boolean suppressBodyStatusChanges = false;
        private boolean suppressMedicalChanges = false;

        ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChildIndex = itemView.findViewById(R.id.tvChildIndex);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            etName = itemView.findViewById(R.id.etName);
            tvBoy = itemView.findViewById(R.id.tvBoy);
            tvGirl = itemView.findViewById(R.id.tvGirl);
            tvBirth = itemView.findViewById(R.id.tvBirth);
            etEthnicity = itemView.findViewById(R.id.etEthnicity);
            etNativePlace = itemView.findViewById(R.id.etNativePlace);
            etFamilyRank = itemView.findViewById(R.id.etFamilyRank);
            etBirthPlace = itemView.findViewById(R.id.etBirthPlace);
            etLanguageEnv = itemView.findViewById(R.id.etLanguageEnv);
            etSchool = itemView.findViewById(R.id.etSchool);
            etAddress = itemView.findViewById(R.id.etAddress);
            etInterests = itemView.findViewById(R.id.etInterests);
            etActivities = itemView.findViewById(R.id.etActivities);
            chipGroupBodyStatus = itemView.findViewById(R.id.chipGroupBodyStatus);
            layoutBodyStatusDetail = itemView.findViewById(R.id.layoutBodyStatusDetail);
            etBodyStatusDesc = itemView.findViewById(R.id.etBodyStatusDesc);
            chipGroupMedicalHistory = itemView.findViewById(R.id.chipGroupMedicalHistory);
            layoutMedicalOther = itemView.findViewById(R.id.layoutMedicalOther);
            etMedicalOther = itemView.findViewById(R.id.etMedicalOther);
            etFatherPhone = itemView.findViewById(R.id.etFatherPhone);
            etMotherPhone = itemView.findViewById(R.id.etMotherPhone);
            etGuardianPhone = itemView.findViewById(R.id.etGuardianPhone);

            attachWatcher(etName, (profile, value) -> profile.setName(value));
            attachWatcher(etEthnicity, (profile, value) -> profile.setEthnicity(value));
            attachWatcher(etNativePlace, (profile, value) -> profile.setNativePlace(value));
            attachWatcher(etFamilyRank, (profile, value) -> profile.setFamilyRank(value));
            attachWatcher(etBirthPlace, (profile, value) -> profile.setBirthPlace(value));
            attachWatcher(etLanguageEnv, (profile, value) -> profile.setLanguageEnv(value));
            attachWatcher(etSchool, (profile, value) -> profile.setSchool(value));
            attachWatcher(etAddress, (profile, value) -> profile.setHomeAddress(value));
            attachWatcher(etInterests, (profile, value) -> profile.setInterests(value));
            attachWatcher(etActivities, (profile, value) -> profile.setActivities(value));
            attachWatcher(etBodyStatusDesc, (profile, value) -> profile.setBodyStatusDetail(value));
            attachWatcher(etMedicalOther, (profile, value) -> profile.setMedicalHistoryOther(value));
            attachWatcher(etFatherPhone, (profile, value) -> profile.setFatherPhone(value));
            attachWatcher(etMotherPhone, (profile, value) -> profile.setMotherPhone(value));
            attachWatcher(etGuardianPhone, (profile, value) -> profile.setGuardianPhone(value));

            btnEdit.setOnClickListener(v -> etName.requestFocus());

            btnDelete.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                if (index != RecyclerView.NO_POSITION) {
                    listener.onDelete(index);
                }
            });

            tvBoy.setOnClickListener(v -> toggleGender(ChildProfile.Gender.BOY));
            tvGirl.setOnClickListener(v -> toggleGender(ChildProfile.Gender.GIRL));

            tvBirth.setOnClickListener(v -> {
                int index = getBindingAdapterPosition();
                if (index != RecyclerView.NO_POSITION) {
                    listener.onRequestBirthDate(index);
                }
            });

            chipGroupBodyStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (suppressBodyStatusChanges) {
                    return;
                }
                int index = getBindingAdapterPosition();
                if (index == RecyclerView.NO_POSITION) {
                    return;
                }
                String value = "";
                if (!checkedIds.isEmpty()) {
                    Chip chip = group.findViewById(checkedIds.get(0));
                    if (chip != null && chip.getTag() != null) {
                        value = chip.getTag().toString();
                    }
                }
                ChildProfile profile = data.get(index);
                profile.setBodyStatus(value);
                if (!requiresBodyDetail(value)) {
                    profile.setBodyStatusDetail("");
                }
                updateBodyStatusDetailVisibility(profile);
                listener.onProfileUpdated();
            });

            chipGroupMedicalHistory.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (suppressMedicalChanges) {
                    return;
                }
                int index = getBindingAdapterPosition();
                if (index == RecyclerView.NO_POSITION) {
                    return;
                }
                ChildProfile profile = data.get(index);
                List<String> history = new ArrayList<>();
                for (int id : checkedIds) {
                    Chip chip = group.findViewById(id);
                    if (chip != null && chip.getTag() != null) {
                        history.add(chip.getTag().toString());
                    }
                }
                if (history.contains("无") && history.size() > 1) {
                    suppressMedicalChanges = true;
                    Chip noneChip = group.findViewById(R.id.chipMedicalNone);
                    if (noneChip != null) {
                        noneChip.setChecked(false);
                    }
                    suppressMedicalChanges = false;
                    history.remove("无");
                }
                profile.setMedicalHistory(history);
                if (!history.contains("其他")) {
                    profile.setMedicalHistoryOther("");
                }
                updateMedicalOtherVisibility(profile);
                listener.onProfileUpdated();
            });
        }

        private void attachWatcher(EditText editText, ValueSink sink) {
            editText.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int index = getBindingAdapterPosition();
                    if (index == RecyclerView.NO_POSITION) {
                        return;
                    }
                    ChildProfile profile = data.get(index);
                    sink.apply(profile, s.toString().trim());
                    listener.onProfileUpdated();
                }
            });
        }

        private void toggleGender(ChildProfile.Gender targetGender) {
            int index = getBindingAdapterPosition();
            if (index == RecyclerView.NO_POSITION) {
                return;
            }
            ChildProfile profile = data.get(index);
            if (profile.getGender() != targetGender) {
                profile.setGender(targetGender);
                listener.onProfileUpdated();
                notifyItemChanged(index);
            }
        }

        void bind(ChildProfile profile, int position, boolean canDelete) {
            tvChildIndex.setText(String.format(Locale.getDefault(), "孩子 %d", position + 1));
            etName.setText(profile.getName());
            tvBirth.setText(profile.getBirthDate().isEmpty() ? itemView.getContext().getString(R.string.child_birth_hint) : profile.getBirthDate());
            tvBirth.setAlpha(profile.getBirthDate().isEmpty() ? 0.7f : 1f);
            etEthnicity.setText(profile.getEthnicity());
            etNativePlace.setText(profile.getNativePlace());
            etFamilyRank.setText(profile.getFamilyRank());
            etBirthPlace.setText(profile.getBirthPlace());
            etLanguageEnv.setText(profile.getLanguageEnv());
            etSchool.setText(profile.getSchool());
            etAddress.setText(profile.getHomeAddress());
            etInterests.setText(profile.getInterests());
            etActivities.setText(profile.getActivities());
            etBodyStatusDesc.setText(profile.getBodyStatusDetail());
            etMedicalOther.setText(profile.getMedicalHistoryOther());
            etFatherPhone.setText(profile.getFatherPhone());
            etMotherPhone.setText(profile.getMotherPhone());
            etGuardianPhone.setText(profile.getGuardianPhone());

            boolean isBoy = profile.getGender() == ChildProfile.Gender.BOY;
            tvBoy.setSelected(isBoy);
            tvGirl.setSelected(!isBoy);

            btnDelete.setEnabled(canDelete);
            btnDelete.setAlpha(canDelete ? 1f : 0.4f);

            updateBodyStatusChips(profile);
            updateMedicalHistoryChips(profile);
        }

        private void updateBodyStatusChips(ChildProfile profile) {
            suppressBodyStatusChanges = true;
            String status = profile.getBodyStatus();
            for (int i = 0; i < chipGroupBodyStatus.getChildCount(); i++) {
                View child = chipGroupBodyStatus.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    boolean checked = status.equals(chip.getTag());
                    chip.setChecked(checked);
                }
            }
            suppressBodyStatusChanges = false;
            updateBodyStatusDetailVisibility(profile);
        }

        private void updateBodyStatusDetailVisibility(ChildProfile profile) {
            boolean needDetail = requiresBodyDetail(profile.getBodyStatus());
            layoutBodyStatusDetail.setVisibility(needDetail ? View.VISIBLE : View.GONE);
            if (!needDetail) {
                etBodyStatusDesc.setText("");
            }
        }

        private void updateMedicalHistoryChips(ChildProfile profile) {
            suppressMedicalChanges = true;
            List<String> history = profile.getMedicalHistory();
            for (int i = 0; i < chipGroupMedicalHistory.getChildCount(); i++) {
                View child = chipGroupMedicalHistory.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    boolean checked = history.contains(chip.getTag());
                    chip.setChecked(checked);
                }
            }
            suppressMedicalChanges = false;
            updateMedicalOtherVisibility(profile);
        }

        private void updateMedicalOtherVisibility(ChildProfile profile) {
            boolean needOther = profile.getMedicalHistory().contains("其他");
            layoutMedicalOther.setVisibility(needOther ? View.VISIBLE : View.GONE);
            if (!needOther) {
                etMedicalOther.setText("");
            }
        }

        private boolean requiresBodyDetail(String status) {
            return "较差".equals(status) || "很差".equals(status);
        }
    }

    private interface ValueSink {
        void apply(ChildProfile profile, String value);
    }
}

