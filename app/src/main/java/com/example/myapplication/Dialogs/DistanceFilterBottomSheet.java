package com.example.myapplication.Dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DistanceFilterBottomSheet extends BottomSheetDialogFragment {

    public interface OnDistanceSelectedListener {
        void onDistanceSelected(double maxDistance);
    }

    private OnDistanceSelectedListener listener;

    private RadioGroup radioGroup;
    private RadioButton radioWalk, radio2km, radio5km, radio10km;
    private SeekBar seekBar;
    private TextView txtDistanceLabel;
    private double selectedDistance = -1;
    private double initialDistance = -1;

    public void setInitialDistance(double distance) {
        this.initialDistance = distance;
    }

    public void setOnDistanceSelectedListener(OnDistanceSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_distance_filter, container, false);

        radioGroup = view.findViewById(R.id.radioGroupDistance);
        radioWalk = view.findViewById(R.id.radioWalk);
        radio2km = view.findViewById(R.id.radio2km);
        radio5km = view.findViewById(R.id.radio5km);
        radio10km = view.findViewById(R.id.radio10km);
        seekBar = view.findViewById(R.id.seekBarDistance);
        txtDistanceLabel = view.findViewById(R.id.txtDistanceLabel);
        TextView btnApply = view.findViewById(R.id.btnApply);
        TextView btnReset = view.findViewById(R.id.btnReset);

        seekBar.setMax(30);

        if (initialDistance >= 0) {
            updateSeekBar((int) initialDistance);
        } else {
            updateSeekBar(5); // default
        }

        radioGroup.clearCheck();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedDistance = progress;
                txtDistanceLabel.setText(progress + " km");
                if (fromUser) radioGroup.clearCheck();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioWalk) updateSeekBar(1);
            else if (checkedId == R.id.radio2km) updateSeekBar(2);
            else if (checkedId == R.id.radio5km) updateSeekBar(5);
            else if (checkedId == R.id.radio10km) updateSeekBar(10);
        });

        btnReset.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDistanceSelected(-1);
            }
            dismiss();
        });


        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                if (selectedDistance < 0) {
                    listener.onDistanceSelected(-1);
                } else {
                    listener.onDistanceSelected(selectedDistance);
                }
            }
            dismiss();
        });

        return view;
    }

    private void updateSeekBar(int value) {
        selectedDistance = value;
        seekBar.setProgress(value);
        txtDistanceLabel.setText(value + " km");
    }
}