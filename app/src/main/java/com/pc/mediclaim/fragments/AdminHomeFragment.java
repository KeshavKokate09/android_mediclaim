package com.pc.mediclaim.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.card.MaterialCardView;
import com.pc.mediclaim.R;
import com.pc.mediclaim.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeFragment extends Fragment {

    private TextView tvTotalUsers, tvTotalPolicies, tvTotalClaims, tvTotalHospitals, tvWelcome, tvPendingApprovals;
    private MaterialCardView cardPendingApprovals;
    private BarChart barChart;
    private PieChart pieChart;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvWelcome = view.findViewById(R.id.tv_admin_welcome);
        tvTotalUsers = view.findViewById(R.id.tv_total_users);
        tvTotalPolicies = view.findViewById(R.id.tv_total_policies);
        tvTotalClaims = view.findViewById(R.id.tv_total_claims);
        tvTotalHospitals = view.findViewById(R.id.tv_total_hospitals);
        tvPendingApprovals = view.findViewById(R.id.tv_pending_approvals);
        cardPendingApprovals = view.findViewById(R.id.card_pending_approvals);
        barChart = view.findViewById(R.id.claims_bar_chart);
        pieChart = view.findViewById(R.id.claims_pie_chart);

        dbHelper = new DatabaseHelper(requireContext());

        cardPendingApprovals.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.adminUsersFragment));

        loadStatistics();
        setupBarChart();
        setupPieChart();
    }

    private void loadStatistics() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Total Users
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USER_ROLE + " = 'user'", null);
        if (cursor.moveToFirst()) tvTotalUsers.setText(String.valueOf(cursor.getInt(0)));
        cursor.close();

        // Total Policies
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_POLICIES, null);
        if (cursor.moveToFirst()) tvTotalPolicies.setText(String.valueOf(cursor.getInt(0)));
        cursor.close();

        // Total Claims
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CLAIMS, null);
        if (cursor.moveToFirst()) tvTotalClaims.setText(String.valueOf(cursor.getInt(0)));
        cursor.close();

        // Total Hospitals
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_HOSPITALS, null);
        if (cursor.moveToFirst()) tvTotalHospitals.setText(String.valueOf(cursor.getInt(0)));
        cursor.close();

        // Pending Approvals
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS + " WHERE " + DatabaseHelper.COLUMN_USER_IS_APPROVED + " = 0 AND " + DatabaseHelper.COLUMN_USER_ROLE + " = 'user'", null);
        if (cursor.moveToFirst()) tvPendingApprovals.setText(String.valueOf(cursor.getInt(0)));
        cursor.close();
    }

    private void setupBarChart() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Showing Total vs Utilized amount logic simplified for demo
        List<BarEntry> entries = new ArrayList<>();
        
        Cursor cursor = db.rawQuery("SELECT SUM(" + DatabaseHelper.COLUMN_POLICY_SUM_INSURED + "), SUM(" + DatabaseHelper.COLUMN_POLICY_UTILIZED + ") FROM " + DatabaseHelper.TABLE_POLICIES, null);
        
        if (cursor.moveToFirst()) {
            entries.add(new BarEntry(0, (float) cursor.getDouble(0))); // Total Insured
            entries.add(new BarEntry(1, (float) cursor.getDouble(1))); // Total Utilized
        }
        cursor.close();

        BarDataSet dataSet = new BarDataSet(entries, "Insurance Financials");
        dataSet.setColors(new int[]{Color.parseColor("#0D47A1"), Color.parseColor("#29B6F6")});
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Total Insured", "Utilized"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void setupPieChart() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<PieEntry> entries = new ArrayList<>();
        
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_CLAIM_STATUS + ", COUNT(*) FROM " + DatabaseHelper.TABLE_CLAIMS + " GROUP BY " + DatabaseHelper.COLUMN_CLAIM_STATUS, null);
        
        boolean hasData = false;
        while (cursor.moveToNext()) {
            hasData = true;
            entries.add(new PieEntry(cursor.getInt(1), cursor.getString(0)));
        }
        cursor.close();

        if (!hasData) {
            entries.add(new PieEntry(1, "No Claims Yet"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Claims Status");
        pieChart.setHoleRadius(40f);
        pieChart.animateXY(1000, 1000);
        pieChart.invalidate();
    }
}
