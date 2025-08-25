package com.android.swiftbus;

import android.content.Intent;
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
    private BottomNavigationView bottomNavigationView;

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
        bottomNavigationView = findViewById(R.id.bottom_navigation);
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

        // Check if we need to navigate to booking with offer data
        handleOfferIntent();
    }

    // Function to switch fragments without reloading
    private void switchFragment(Fragment newFragment) {
        if (activeFragment != newFragment) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.hide(activeFragment).show(newFragment).commit();
            activeFragment = newFragment;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOfferIntent();
    }

    private void handleOfferIntent() {
        Intent intent = getIntent();
        if (intent != null && "booking".equals(intent.getStringExtra("navigate_to"))) {
            // Navigate to booking fragment with offer data
            navigateToBookingWithOffer(intent);
        }
    }

    private void navigateToBookingWithOffer(Intent intent) {
        // Create booking fragment
        Booking bookingFragment = new Booking();

        // Create bundle with offer data
        Bundle args = new Bundle();
        args.putString("offer_id", intent.getStringExtra("offer_id"));
        args.putString("offer_title", intent.getStringExtra("offer_title"));
        args.putString("offer_code", intent.getStringExtra("offer_code"));
        args.putString("offer_type", intent.getStringExtra("offer_type"));
        args.putString("discount_type", intent.getStringExtra("discount_type"));
        args.putInt("discount_percentage", intent.getIntExtra("discount_percentage", 0));
        args.putInt("discount_amount", intent.getIntExtra("discount_amount", 0));
        args.putInt("max_discount_amount", intent.getIntExtra("max_discount_amount", 0));
        args.putInt("minimum_fare", intent.getIntExtra("minimum_fare", 0));
        args.putInt("minimum_passengers", intent.getIntExtra("minimum_passengers", 0));
        args.putInt("advance_booking_days", intent.getIntExtra("advance_booking_days", 0));
        args.putString("source_city", intent.getStringExtra("source_city"));
        args.putString("destination_city", intent.getStringExtra("destination_city"));
        args.putString("applicable_seat_type", intent.getStringExtra("applicable_seat_type"));
        args.putString("applicable_vehicle_type", intent.getStringExtra("applicable_vehicle_type"));

        bookingFragment.setArguments(args);

        // Replace current fragment with booking fragment
        bottomNavigationView.setSelectedItemId(R.id.booking);

        // Update bottom navigation or any other navigation UI
        // For example, if you have a BottomNavigationView:
        // bottomNavigationView.setSelectedItemId(R.id.nav_booking);
    }

}
