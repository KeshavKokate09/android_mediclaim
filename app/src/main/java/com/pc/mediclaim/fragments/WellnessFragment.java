package com.pc.mediclaim.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.pc.mediclaim.R;

import java.util.Locale;

public class WellnessFragment extends Fragment {

    private View cardBMI, cardBMR, cardWater;
    private View resultLayoutBMI, resultLayoutBMR, resultLayoutWater;
    private EditText etWeightBMI, etHeightBMI;
    private EditText etAgeBMR, etWeightBMR, etHeightBMR;
    private EditText etWeightWater;
    private RadioGroup rgGender;
    private TextView tvResultBMI, tvCategoryBMI, tvResultBMR, tvResultWater;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_wellness, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar(view);
        setupToggleGroup(view);
        setupBMI(view);
        setupBMR(view);
        setupWater(view);
    }

    private void setupToolbar(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (!Navigation.findNavController(v).navigateUp()) {
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });
    }

    private void setupToggleGroup(View view) {
        cardBMI = view.findViewById(R.id.cardBMI);
        cardBMR = view.findViewById(R.id.cardBMR);
        cardWater = view.findViewById(R.id.cardWater);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                cardBMI.setVisibility(checkedId == R.id.btnBMI ? View.VISIBLE : View.GONE);
                cardBMR.setVisibility(checkedId == R.id.btnBMR ? View.VISIBLE : View.GONE);
                cardWater.setVisibility(checkedId == R.id.btnWater ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupBMI(View view) {
        etWeightBMI = view.findViewById(R.id.etWeight);
        etHeightBMI = view.findViewById(R.id.etHeight);
        tvResultBMI = view.findViewById(R.id.tvResultBMI);
        tvCategoryBMI = view.findViewById(R.id.tvCategoryBMI);
        resultLayoutBMI = view.findViewById(R.id.resultLayoutBMI);

        view.findViewById(R.id.btnCalculateBMI).setOnClickListener(v -> calculateBMI());
    }

    private void setupBMR(View view) {
        etAgeBMR = view.findViewById(R.id.etAgeBMR);
        etWeightBMR = view.findViewById(R.id.etWeightBMR);
        etHeightBMR = view.findViewById(R.id.etHeightBMR);
        rgGender = view.findViewById(R.id.rgGender);
        tvResultBMR = view.findViewById(R.id.tvResultBMR);
        resultLayoutBMR = view.findViewById(R.id.resultLayoutBMR);

        view.findViewById(R.id.btnCalculateBMR).setOnClickListener(v -> calculateBMR());
    }

    private void setupWater(View view) {
        etWeightWater = view.findViewById(R.id.etWeightWater);
        tvResultWater = view.findViewById(R.id.tvResultWater);
        resultLayoutWater = view.findViewById(R.id.resultLayoutWater);

        view.findViewById(R.id.btnCalculateWater).setOnClickListener(v -> calculateWater());
    }

    private void calculateBMI() {
        String wStr = etWeightBMI.getText().toString();
        String hStr = etHeightBMI.getText().toString();

        if (wStr.isEmpty() || hStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter weight and height", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float weight = Float.parseFloat(wStr);
            float height = Float.parseFloat(hStr) / 100;
            if (height == 0) return;
            float bmi = weight / (height * height);

            tvResultBMI.setText(String.format(Locale.getDefault(), "%.1f", bmi));
            String category;
            if (bmi < 18.5) category = "Underweight";
            else if (bmi < 25) category = "Normal weight";
            else if (bmi < 30) category = "Overweight";
            else category = "Obese";
            tvCategoryBMI.setText(category);
            resultLayoutBMI.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateBMR() {
        String ageStr = etAgeBMR.getText().toString();
        String wStr = etWeightBMR.getText().toString();
        String hStr = etHeightBMR.getText().toString();

        if (ageStr.isEmpty() || wStr.isEmpty() || hStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter all values", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float weight = Float.parseFloat(wStr);
            float height = Float.parseFloat(hStr);
            boolean isMale = rgGender.getCheckedRadioButtonId() == R.id.rbMale;

            double bmr;
            if (isMale) {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            } else {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            }

            tvResultBMR.setText(String.format(Locale.getDefault(), "%.0f kcal/day", bmr));
            resultLayoutBMR.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateWater() {
        String wStr = etWeightWater.getText().toString();
        if (wStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter weight", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float weight = Float.parseFloat(wStr);
            float liters = weight * 0.033f;

            tvResultWater.setText(String.format(Locale.getDefault(), "%.1f Liters/day", liters));
            resultLayoutWater.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }
}
