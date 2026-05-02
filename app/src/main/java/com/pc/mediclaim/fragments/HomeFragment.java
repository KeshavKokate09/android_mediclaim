package com.pc.mediclaim.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pc.mediclaim.R;
import com.pc.mediclaim.adapters.DependentAdapter;
import com.pc.mediclaim.model.Dependent;
import com.pc.mediclaim.model.Policy;
import com.pc.mediclaim.repository.MediclaimRepository;
import com.pc.mediclaim.utils.Constants;
import com.pc.mediclaim.viewmodel.HomeViewModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private TextView tvWelcome, tvInsurer, tvPolicyType, tvPolicyNo, tvSumInsured, tvUtilized, tvAvailable, tvStatus;
    private RecyclerView rvDependents;
    private DependentAdapter dependentsAdapter;
    private List<Dependent> dependentList;
    private View llWellness, cvHospitals, cvClaims, cvPolicyCard;
    private Policy currentPolicy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvInsurer = view.findViewById(R.id.tvInsurer);
        tvPolicyType = view.findViewById(R.id.tvPolicyType);
        tvPolicyNo = view.findViewById(R.id.tvPolicyNo);
        tvSumInsured = view.findViewById(R.id.tvSumInsured);
        tvUtilized = view.findViewById(R.id.tvUtilized);
        tvAvailable = view.findViewById(R.id.tvAvailable);
        tvStatus = view.findViewById(R.id.tvStatus);
        rvDependents = view.findViewById(R.id.rvDependents);
        llWellness = view.findViewById(R.id.llWellness);
        cvHospitals = view.findViewById(R.id.cvHospitals);
        cvClaims = view.findViewById(R.id.cvClaims);
        cvPolicyCard = view.findViewById(R.id.cvPolicyCard);

        rvDependents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dependentList = new ArrayList<>();
        dependentsAdapter = new DependentAdapter(dependentList);
        rvDependents.setAdapter(dependentsAdapter);

        setupObservers();
        loadData();
        setupClickListeners();
    }

    private void setupClickListeners() {
        if (llWellness != null) {
            llWellness.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_wellnessFragment));
        }
        if (cvHospitals != null) {
            cvHospitals.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.hospitalFragment));
        }
        if (cvClaims != null) {
            cvClaims.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.claimsFragment));
        }
        if (cvPolicyCard != null) {
            cvPolicyCard.setOnClickListener(v -> showECardDialog());
        }
    }

    private void showECardDialog() {
        if (currentPolicy == null) return;

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_ecard);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String userName = pref.getString(Constants.KEY_USER_NAME, "User");
        String memberId = pref.getString(Constants.KEY_USER_MEMBER_ID, "SINH-0000000");

        TextView tvHolderName = dialog.findViewById(R.id.tvHolderName);
        TextView tvMemberId = dialog.findViewById(R.id.tvMemberId);
        TextView tvPolicyNo = dialog.findViewById(R.id.tvPolicyNo);
        TextView tvValidTill = dialog.findViewById(R.id.tvValidTill);
        TextView tvType = dialog.findViewById(R.id.tvType);
        View btnDownload = dialog.findViewById(R.id.btnDownload);

        tvHolderName.setText(userName.toUpperCase());
        tvMemberId.setText("Member ID: " + memberId);
        tvPolicyNo.setText(currentPolicy.getPolicyNo());
        tvValidTill.setText(currentPolicy.getValidTo());
        tvType.setText(currentPolicy.getType().toUpperCase());

        btnDownload.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Downloading E-Card...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupObservers() {
        viewModel.getPolicyLiveData().observe(getViewLifecycleOwner(), policy -> {
            if (policy != null) {
                currentPolicy = policy;
                tvInsurer.setText(policy.getCompany());
                tvPolicyType.setText(policy.getType());
                tvPolicyNo.setText(policy.getPolicyNo());
                tvStatus.setText(policy.getStatus().toUpperCase());
                tvSumInsured.setText(String.format("₹ %,.0f", policy.getSumInsured()));
                tvUtilized.setText(String.format("₹ %,.0f", policy.getUtilized()));
                tvAvailable.setText(String.format("₹ %,.0f", policy.getAvailable()));
            }
        });
    }

    private void loadData() {
        SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        int userId = pref.getInt(Constants.KEY_USER_ID, -1);
        String userName = pref.getString(Constants.KEY_USER_NAME, "User");

        String greeting = "Hello";
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 12) greeting = "Good Morning";
        else if (hour >= 12 && hour < 16) greeting = "Good Afternoon";
        else greeting = "Good Evening";

        tvWelcome.setText(greeting + ", " + userName);

        viewModel.loadPolicy(userId);
        
        MediclaimRepository repo = new MediclaimRepository(getContext());
        dependentList.clear();
        dependentList.addAll(repo.getDependentsByUserId(userId));
        dependentsAdapter.notifyDataSetChanged();
    }
}
