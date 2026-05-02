package com.pc.mediclaim.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.pc.mediclaim.R;
import com.pc.mediclaim.utils.Constants;
import com.pc.mediclaim.viewmodel.LoginViewModel;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoToRegister = view.findViewById(R.id.btnGoToRegister);

        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;
            
            if (user.getId() == -2) {
                Toast.makeText(getContext(), "Account pending admin approval", Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences pref = requireActivity().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            pref.edit()
                .putInt(Constants.KEY_USER_ID, user.getId())
                .putString(Constants.KEY_USER_ROLE, user.getRole())
                .putString(Constants.KEY_USER_NAME, user.getName())
                .apply();

            if (Constants.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_adminHomeFragment);
            } else {
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment);
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            viewModel.login(email, pass);
        });

        btnGoToRegister.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment);
        });
    }
}
