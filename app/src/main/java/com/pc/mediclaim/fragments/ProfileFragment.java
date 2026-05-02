package com.pc.mediclaim.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pc.mediclaim.R;
import com.pc.mediclaim.utils.Constants;
import com.pc.mediclaim.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView tvName, tvMemberId, tvEmail, tvMobile, tvTitle, tvSubtitle;
    private MaterialButton btnLogout;
    private View layoutEmail, layoutMobile, layoutChangePassword, layoutUserSections;
    private View mobileDivider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        tvTitle = view.findViewById(R.id.tvProfileTitle);
        tvSubtitle = view.findViewById(R.id.tvProfileSubtitle);
        tvName = view.findViewById(R.id.tvProfileName);
        tvMemberId = view.findViewById(R.id.tvMemberId);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvMobile = view.findViewById(R.id.tvMobile);
        btnLogout = view.findViewById(R.id.btnLogout);
        
        layoutEmail = view.findViewById(R.id.layoutEmail);
        layoutMobile = view.findViewById(R.id.layoutMobile);
        layoutChangePassword = view.findViewById(R.id.layoutChangePassword);
        layoutUserSections = view.findViewById(R.id.layoutUserSections);
        mobileDivider = view.findViewById(R.id.mobileDivider);

        SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String role = pref.getString(Constants.KEY_USER_ROLE, Constants.ROLE_USER);
        boolean isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(role);

        if (isAdmin) {
            tvTitle.setText("Admin Profile");
            tvSubtitle.setText("System Administrator");
            layoutUserSections.setVisibility(View.GONE);
            layoutMobile.setVisibility(View.GONE);
            mobileDivider.setVisibility(View.GONE);
            tvMemberId.setText("Administrator");
        } else {
            tvTitle.setText("My Profile");
            tvSubtitle.setText("Manage your account details");
            layoutUserSections.setVisibility(View.VISIBLE);
            layoutMobile.setVisibility(View.VISIBLE);
            mobileDivider.setVisibility(View.VISIBLE);
        }

        setupObservers();
        loadProfile();

        btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        pref.edit().clear().apply();
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.loginFragment, null, new NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph, true)
                                .build());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        layoutEmail.setOnClickListener(v -> showEditDialog("Email", tvEmail.getText().toString(), InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS));
        layoutMobile.setOnClickListener(v -> showEditDialog("Mobile", tvMobile.getText().toString(), InputType.TYPE_CLASS_PHONE));
        layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void setupObservers() {
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvName.setText(user.getName());
                tvEmail.setText(user.getEmail());
                tvMobile.setText(user.getMobile());
                
                SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
                String role = pref.getString(Constants.KEY_USER_ROLE, Constants.ROLE_USER);
                
                if (!Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                    if (user.getMemberId() != null && !user.getMemberId().isEmpty()) {
                        tvMemberId.setText("Member ID: " + user.getMemberId());
                    } else {
                        tvMemberId.setText("ID Not Assigned");
                    }
                }
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
        SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        int userId = pref.getInt(Constants.KEY_USER_ID, -1);
        if (userId != -1) {
            viewModel.loadProfile(userId);
        }
    }

    private void showEditDialog(String field, String currentVal, int inputType) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Update " + field);
        
        final EditText input = new EditText(requireContext());
        input.setInputType(inputType);
        input.setText(currentVal);
        
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newVal = input.getText().toString().trim();
            if (newVal.isEmpty()) return;

            if (field.equals("Email") && !Patterns.EMAIL_ADDRESS.matcher(newVal).matches()) {
                Toast.makeText(getContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }
            if (field.equals("Mobile") && newVal.length() != 10) {
                Toast.makeText(getContext(), "Mobile must be 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            int userId = pref.getInt(Constants.KEY_USER_ID, -1);
            if (field.equalsIgnoreCase("Email")) {
                viewModel.updateProfile(userId, newVal, null);
            } else {
                viewModel.updateProfile(userId, null, newVal);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangePasswordDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Change Password");

        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etOld = view.findViewById(R.id.etOldPassword);
        EditText etNew = view.findViewById(R.id.etNewPassword);
        
        builder.setView(view);
        builder.setPositiveButton("Change", (dialog, which) -> {
            String oldP = etOld.getText().toString();
            String newP = etNew.getText().toString();
            if (oldP.isEmpty() || newP.isEmpty()) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newP.length() < 6) {
                Toast.makeText(getContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "Password update feature coming soon", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
