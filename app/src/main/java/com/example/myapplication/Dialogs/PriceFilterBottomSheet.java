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

public class PriceFilterBottomSheet extends BottomSheetDialogFragment {

    public interface OnPriceSelectedListener {
        void onPriceSelected(double maxPrice);
    }

    private OnPriceSelectedListener listener;

    private RadioGroup radioGroup;
    private RadioButton radio50, radio70, radio100;
    private SeekBar seekBar;
    private TextView txtPriceLabel;
    private double selectedPrice = -1;
    private double initialPrice = -1;

    public void setInitialPrice(double price) {
        this.initialPrice = price;
    }

    public void setOnPriceSelectedListener(OnPriceSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_price_filter, container, false);

        radioGroup = view.findViewById(R.id.radioGroupPrice);
        radio50 = view.findViewById(R.id.radioUnder50);
        radio70 = view.findViewById(R.id.radioUnder70);
        radio100 = view.findViewById(R.id.radioUnder100);
        seekBar = view.findViewById(R.id.seekBarPrice);
        txtPriceLabel = view.findViewById(R.id.txtPriceLabel);
        TextView btnApply = view.findViewById(R.id.btnApply);
        TextView btnReset = view.findViewById(R.id.btnReset);

        seekBar.setMax(200);

        if (initialPrice >= 0) {
            updateSeekBar((int) initialPrice);
        } else {
            updateSeekBar(70); // default UI
        }
        radioGroup.clearCheck();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedPrice = progress;
                txtPriceLabel.setText(progress + "K");
                if (fromUser) radioGroup.clearCheck();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioUnder50) updateSeekBar(50);
            else if (checkedId == R.id.radioUnder70) updateSeekBar(70);
            else if (checkedId == R.id.radioUnder100) updateSeekBar(100);
        });

        btnReset.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPriceSelected(-1);
            }
            dismiss();
        });

        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                if (selectedPrice < 0) {
                    listener.onPriceSelected(-1);
                } else {
                    listener.onPriceSelected(selectedPrice);
                }
            }
            dismiss();
        });

        return view;
    }

    private void updateSeekBar(int value) {
        selectedPrice = value;
        seekBar.setProgress(value);
        txtPriceLabel.setText(value + "K");
    }
}
