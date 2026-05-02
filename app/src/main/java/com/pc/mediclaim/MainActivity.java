package com.pc.mediclaim;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pc.mediclaim.utils.Constants;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    private View appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottom_navigation);
        appBarLayout = findViewById(R.id.app_bar_layout);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            appBarLayout.setPadding(0, systemBars.top, 0, 0);
            bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return windowInsets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.homeFragment);
            topLevelDestinations.add(R.id.adminHomeFragment);
            topLevelDestinations.add(R.id.claimsFragment);
            topLevelDestinations.add(R.id.hospitalFragment);
            topLevelDestinations.add(R.id.profileFragment);

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
            
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                // Hide Activity's Toolbar and BottomNav for fragments that manage their own or shouldn't have one
                if (destId == R.id.loginFragment || destId == R.id.registerFragment || 
                    destId == R.id.newClaimFragment || destId == R.id.wellnessFragment) {
                    appBarLayout.setVisibility(View.GONE);
                    bottomNav.setVisibility(View.GONE);
                } else {
                    appBarLayout.setVisibility(View.VISIBLE);
                    bottomNav.setVisibility(View.VISIBLE);
                    
                    if (destId == R.id.homeFragment || destId == R.id.adminHomeFragment) bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                    else if (destId == R.id.claimsFragment) bottomNav.getMenu().findItem(R.id.nav_claims).setChecked(true);
                    else if (destId == R.id.hospitalFragment) bottomNav.getMenu().findItem(R.id.nav_hospital).setChecked(true);
                    else if (destId == R.id.profileFragment) bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
                }
            });

            SharedPreferences pref = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
            
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                String role = pref.getString(Constants.KEY_USER_ROLE, Constants.ROLE_USER);
                boolean isAdmin = Constants.ROLE_ADMIN.equalsIgnoreCase(role);

                if (itemId == R.id.nav_home) {
                    navController.navigate(isAdmin ? R.id.adminHomeFragment : R.id.homeFragment);
                    return true;
                } else if (itemId == R.id.nav_claims) {
                    navController.navigate(R.id.claimsFragment);
                    return true;
                } else if (itemId == R.id.nav_hospital) {
                    navController.navigate(R.id.hospitalFragment);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    navController.navigate(R.id.profileFragment);
                    return true;
                }
                return false;
            });

            // Handle Back Navigation
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    int currentId = navController.getCurrentDestination().getId();
                    
                    if (currentId == R.id.homeFragment || currentId == R.id.adminHomeFragment) {
                        finish();
                    } 
                    else if (currentId == R.id.claimsFragment || currentId == R.id.hospitalFragment || 
                             currentId == R.id.profileFragment) {
                        String role = pref.getString(Constants.KEY_USER_ROLE, Constants.ROLE_USER);
                        if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                            navController.navigate(R.id.adminHomeFragment);
                        } else {
                            navController.navigate(R.id.homeFragment);
                        }
                    } 
                    else {
                        if (!navController.popBackStack()) {
                            finish();
                        }
                    }
                }
            });

            if (pref.getInt(Constants.KEY_USER_ID, -1) != -1) {
                if (navController.getCurrentDestination() != null && 
                    navController.getCurrentDestination().getId() == R.id.loginFragment) {
                    String role = pref.getString(Constants.KEY_USER_ROLE, Constants.ROLE_USER);
                    if (Constants.ROLE_ADMIN.equalsIgnoreCase(role)) {
                        navController.navigate(R.id.adminHomeFragment);
                    } else {
                        navController.navigate(R.id.homeFragment);
                    }
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && navController.navigateUp()) || super.onSupportNavigateUp();
    }
}
