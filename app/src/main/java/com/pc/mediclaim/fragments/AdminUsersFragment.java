package com.pc.mediclaim.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pc.mediclaim.R;
import com.pc.mediclaim.model.User;
import com.pc.mediclaim.viewmodel.AdminUsersViewModel;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {

    private RecyclerView rvUsers;
    private AdminUsersViewModel viewModel;
    private List<User> userList;
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminUsersViewModel.class);
        rvUsers = view.findViewById(R.id.rvPendingUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        
        userList = new ArrayList<>();
        adapter = new UserAdapter(userList);
        rvUsers.setAdapter(adapter);

        viewModel.getPendingUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            userList.clear();
            userList.addAll(users);
            adapter.notifyDataSetChanged();
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.loadPendingUsers();
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private List<User> users;
        public UserAdapter(List<User> users) { this.users = users; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvName.setText(user.getName());
            holder.tvEmail.setText(user.getEmail());
            holder.btnApprove.setOnClickListener(v -> {
                // In a real app, this might come from an input dialog
                String memberId = "SINH-" + (10000 + user.getId());
                viewModel.approveUser(user.getId(), memberId, "Individual Health", 500000.0);
            });
        }

        @Override
        public int getItemCount() { return users.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail;
            Button btnApprove;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPendingName);
                tvEmail = itemView.findViewById(R.id.tvPendingEmail);
                btnApprove = itemView.findViewById(R.id.btnApproveUser);
            }
        }
    }
}
