package com.android.swiftbus;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Main extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Fragment homeFragment, bookingFragment, profileFragment, trackingFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize FragmentManager
        fragmentManager = getSupportFragmentManager();

        // Initialize Fragments
        homeFragment = new Home();
        bookingFragment = new Booking();
        profileFragment = new Profile();
        trackingFragment = new Tracking();

        // Set default active fragment
        activeFragment = homeFragment;

        // Add all fragments but hide all except home
        fragmentManager.beginTransaction()
                .add(R.id.framecontainer, trackingFragment, "Tracking").hide(trackingFragment)
                .add(R.id.framecontainer, profileFragment, "Profile").hide(profileFragment)
                .add(R.id.framecontainer, bookingFragment, "Booking").hide(bookingFragment)
                .add(R.id.framecontainer, homeFragment, "Home")
                .commit();

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                switchFragment(homeFragment);
            } else if (itemId == R.id.booking) {
                switchFragment(bookingFragment);
            } else if (itemId == R.id.profile) {
                switchFragment(profileFragment);
            } else if (itemId == R.id.traking) {
                switchFragment(trackingFragment);
            }

            return true;
        });
    }

    // Function to switch fragments without reloading
    private void switchFragment(Fragment newFragment) {
        if (activeFragment != newFragment) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.hide(activeFragment).show(newFragment).commit();
            activeFragment = newFragment;
        }
    }
}
