package com.pc.mediclaim.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pc.mediclaim.R;
import com.pc.mediclaim.model.User;
import java.util.List;

public class PendingUserAdapter extends RecyclerView.Adapter<PendingUserAdapter.ViewHolder> {
    private List<User> users;
    private OnUserApprovalListener listener;

    public interface OnUserApprovalListener {
        void onApprove(User user);
    }

    public PendingUserAdapter(List<User> users, OnUserApprovalListener listener) {
        this.users = users;
        this.listener = listener;
    }

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
        holder.btnApprove.setOnClickListener(v -> listener.onApprove(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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
