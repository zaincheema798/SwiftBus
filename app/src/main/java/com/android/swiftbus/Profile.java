package com.android.swiftbus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Components
    private CircleImageView profileImage;
    private ImageView editProfileBtn;
    private TextView profileName, profileMembership;
    private TextView profileEmail, profilePhone, profileGender, profileAge, profileBlood, profileAddress;
    private TextView tripsCount, citiesCount, pointsCount;
    private RecyclerView upcomingTripsRecycler;
    private TextView noTripsText, viewAllTrips;
    private SwitchCompat notificationsSwitch, darkModeSwitch;
    private Button logoutButton;

    // Containers that need click listeners
    private View paymentMethodsContainer, travelPreferencesContainer;
    private View notificationsContainer, darkModeContainer;
    private View accountSettingsContainer, helpSupportContainer, aboutContainer;

    // User data
    private UserProfile userProfile;
    private List<Trip> upcomingTrips;
    private TripAdapter tripAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        initializeFirebase();

        // Initialize the UI components
        initializeViews(view);

        // Check if user is logged in
        if (checkUserLoggedIn()) {
            // Load data from Firebase
            loadUserDataFromFirebase();
        }

        // Set up click listeners
        setupClickListeners();

        return view;
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private boolean checkUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, navigate to login
            navigateToLogin();
            return false;
        }
        return true;
    }

    private void initializeViews(View view) {
        // Profile section
        profileImage = view.findViewById(R.id.profile_image);
        editProfileBtn = view.findViewById(R.id.edit_profile_btn);
        profileName = view.findViewById(R.id.profile_name);
        profileMembership = view.findViewById(R.id.profile_membership);

        // Personal information
        profileEmail = view.findViewById(R.id.profile_email);
        profilePhone = view.findViewById(R.id.profile_phone);
        profileGender = view.findViewById(R.id.profile_gender);
        profileAge = view.findViewById(R.id.profile_age);
        profileBlood = view.findViewById(R.id.profile_blood);
        profileAddress = view.findViewById(R.id.profile_address);


        // Stats section
        tripsCount = view.findViewById(R.id.trips_count);
        citiesCount = view.findViewById(R.id.cities_count);
        pointsCount = view.findViewById(R.id.points_count);

        // Upcoming trips section
        upcomingTripsRecycler = view.findViewById(R.id.upcoming_trips_recycler);
        noTripsText = view.findViewById(R.id.no_trips_text);
        viewAllTrips = view.findViewById(R.id.view_all_trips);

        // Preferences section
        paymentMethodsContainer = view.findViewById(R.id.payment_methods_container);
        travelPreferencesContainer = view.findViewById(R.id.travel_preferences_container);
        notificationsContainer = view.findViewById(R.id.notifications_container);
        darkModeContainer = view.findViewById(R.id.dark_mode_container);
        notificationsSwitch = view.findViewById(R.id.notifications_switch);
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);

        // Account section
        accountSettingsContainer = view.findViewById(R.id.account_settings_container);
        helpSupportContainer = view.findViewById(R.id.help_support_container);
        aboutContainer = view.findViewById(R.id.about_container);
        logoutButton = view.findViewById(R.id.logout_button);
    }

    private void loadUserDataFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        // Load profile data
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Create UserProfile from Firestore data
                        userProfile = createUserProfileFromDocument(documentSnapshot);

                        // Populate UI with user data
                        populateUserProfile();

                        // Load profile image if available
                        loadProfileImage();

                        // Load user preferences
                        loadUserPreferences(userId);
                    } else {
                        // Handle case where user document doesn't exist
                        Toast.makeText(getContext(), "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        // Load upcoming bookings data from the user document directly
        loadUpcomingBookingsFromFirebase(userId);
    }

    private UserProfile createUserProfileFromDocument(DocumentSnapshot document) {
        // Extract user data from Firestore document
        String name = document.getString("name");
        String profileImageBase64 = document.getString("profileImageBase64");
        String membershipStatus = document.getString("membershipStatus");
        String email = document.getString("email");
        String phone = document.getString("phone");
        String gender = document.getString("gender");
        String age = document.getString("age");
        String bloodGroup = document.getString("bloodGroup");
        String address = document.getString("address");

        // Get stats with default values if not present
        Long tripsCountLong = document.getLong("tripsCount");
        int tripsCount = tripsCountLong != null ? tripsCountLong.intValue() : 0;

        Long citiesCountLong = document.getLong("citiesCount");
        int citiesCount = citiesCountLong != null ? citiesCountLong.intValue() : 0;

        Long pointsCountLong = document.getLong("pointsCount");
        int pointsCount = pointsCountLong != null ? pointsCountLong.intValue() : 0;


        return new UserProfile.Builder()
                .setProfileImageBase64(profileImageBase64)
                .setName(name)
                .setMembershipStatus(membershipStatus)
                .setEmail(email)
                .setPhone(phone)
                .setGender(gender)
                .setAge(age)
                .setBloodGroup(bloodGroup)
                .setAddress(address)
                .setTripsCount(tripsCount)
                .setCitiesCount(citiesCount)
                .setPointsCount(pointsCount)
                .build();
    }

    private void loadProfileImage() {
        if (userProfile != null && userProfile.getProfileImageBase64() != null && !userProfile.getProfileImageBase64().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(userProfile.getProfileImageBase64(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                // Load default image if the base64 string is corrupted
                profileImage.setImageResource(R.drawable.profile);
            }
        } else {
            // Load default image if no base64 string
            profileImage.setImageResource(R.drawable.profile);
        }
    }

    private void loadUserPreferences(String userId) {
        db.collection("user_preferences").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Set notification switch
                        Boolean notificationsEnabled = documentSnapshot.getBoolean("notificationsEnabled");
                        if (notificationsEnabled != null) {
                            notificationsSwitch.setChecked(notificationsEnabled);
                        }

                        // Set dark mode switch
                        Boolean darkModeEnabled = documentSnapshot.getBoolean("darkModeEnabled");
                        if (darkModeEnabled != null) {
                            darkModeSwitch.setChecked(darkModeEnabled);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure silently
                });
    }

    private void loadUpcomingBookingsFromFirebase(String userId) {
        // Clear existing trips
        upcomingTrips = new ArrayList<>();

        // First get the user document to access the bookings array
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check if the user has any bookings
                        ArrayList<String> bookingsList = (ArrayList<String>) documentSnapshot.get("bookings");

                        if (bookingsList != null && !bookingsList.isEmpty()) {
                            // Process each booking ID in the list
                            for (String bookingId : bookingsList) {
                                // Get booking details from the bookings collection
                                db.collection("bookings").document(bookingId)
                                        .get()
                                        .addOnSuccessListener(bookingDoc -> {
                                            if (bookingDoc.exists()) {
                                                Trip trip = createTripFromBookingDocument(bookingDoc);
                                                upcomingTrips.add(trip);

                                                // Update the recycler view when a new trip is added
                                                if (tripAdapter != null) {
                                                    tripAdapter.notifyDataSetChanged();
                                                }

                                                // Show/hide no trips text based on data
                                                updateTripsVisibility();
                                            }
                                        });
                            }
                        } else {
                            // No bookings found
                            updateTripsVisibility();
                        }
                    } else {
                        // User document doesn't exist
                        updateTripsVisibility();
                    }

                    // Set up RecyclerView with the fetched data
                    setupUpcomingTripsRecyclerView();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(getContext(), "Failed to load upcoming trips: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Still setup RecyclerView but with empty list
                    setupUpcomingTripsRecyclerView();
                });
    }
    private void updateTripsVisibility() {
        if (noTripsText != null && upcomingTripsRecycler != null) {
            if (upcomingTrips.isEmpty()) {
                noTripsText.setVisibility(View.VISIBLE);
                upcomingTripsRecycler.setVisibility(View.GONE);
            } else {
                noTripsText.setVisibility(View.GONE);
                upcomingTripsRecycler.setVisibility(View.VISIBLE);
            }
        }
    }

    private Trip createTripFromBookingDocument(DocumentSnapshot document) {
        String route = document.getString("source") + " - " + document.getString("destination");

        // Extract and format the travel date
        String date = "Unknown Date";
        Timestamp timestamp = document.getTimestamp("travelDate");
        if (timestamp != null) {
            LocalDate localDate = timestamp.toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            date = localDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        }

        // Extract time from schedule
        String schedule = document.getString("schedule");
        String time = "Unknown Time";
        if (schedule != null && schedule.contains("(") && schedule.contains(")")) {
            time = schedule.substring(schedule.indexOf('(') + 1, schedule.indexOf(')')).trim(); // e.g., "10:00 PM - Abbottabad to Attock"
        }

        String busOperator = document.getString("vehicleType");

        // Handle selectedSeats array
        List<String> selectedSeatsList = (List<String>) document.get("selectedSeats");
        String seatInfo = (selectedSeatsList != null) ? String.join(", ", selectedSeatsList) : "None";

        return new Trip(route, date, time, busOperator, seatInfo, R.drawable.ic_bus);
    }

    private void setupUpcomingTripsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        upcomingTripsRecycler.setLayoutManager(layoutManager);

        tripAdapter = new TripAdapter(getContext(), upcomingTrips);
        upcomingTripsRecycler.setAdapter(tripAdapter);

        // Show/hide no trips text based on data
        updateTripsVisibility();
    }

    private void populateUserProfile() {
        if (userProfile == null) return;

        // Set user profile data to UI
        profileName.setText(userProfile.getName());
        profileMembership.setText(userProfile.getMembershipStatus());
        profileEmail.setText(userProfile.getEmail());
        profilePhone.setText(userProfile.getPhone());
        profileGender.setText(userProfile.getGender());
        profileAge.setText(userProfile.getAge());
        profileBlood.setText(userProfile.getBloodGroup());
        profileAddress.setText(userProfile.getAddress());

        // Set stats
        tripsCount.setText(String.valueOf(userProfile.getTripsCount()));
        citiesCount.setText(String.valueOf(userProfile.getCitiesCount()));
        pointsCount.setText(String.valueOf(userProfile.getPointsCount()));
    }

    private void setupClickListeners() {
        // Edit profile button
        editProfileBtn.setOnClickListener(v -> {
            navigateToEditProfile();
        });

        // View all trips
        viewAllTrips.setOnClickListener(v -> {
            navigateToAllTrips();
        });

        // Preference section clicks
        paymentMethodsContainer.setOnClickListener(v -> {
            navigateToPaymentMethods();
        });

        travelPreferencesContainer.setOnClickListener(v -> {
            navigateToTravelPreferences();
        });

        notificationsContainer.setOnClickListener(v -> {
            notificationsSwitch.toggle();
        });

        darkModeContainer.setOnClickListener(v -> {
            darkModeSwitch.toggle();
            toggleDarkMode(darkModeSwitch.isChecked());
        });

        // Account section clicks
        accountSettingsContainer.setOnClickListener(v -> {
            navigateToAccountSettings();
        });

        helpSupportContainer.setOnClickListener(v -> {
            navigateToHelpSupport();
        });

        aboutContainer.setOnClickListener(v -> {
            navigateToAbout();
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            logoutUser();
        });

        // Switch listeners
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationSettings(isChecked);
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleDarkMode(isChecked);
        });
    }

    // Navigation and action methods

    private void navigateToLogin() {
        // Navigate to login screen
        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToEditProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getActivity(), EditProfile.class);
            intent.putExtra("USER_ID", currentUser.getUid());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Please login to edit your profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAllTrips() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getActivity(), Home.class);
            intent.putExtra("USER_ID", currentUser.getUid());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Please login to view your trips", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToPaymentMethods() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getActivity(), Home.class);
            intent.putExtra("USER_ID", currentUser.getUid());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Please login to manage payment methods", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToTravelPreferences() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getActivity(), Home.class);
            intent.putExtra("USER_ID", currentUser.getUid());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Please login to manage travel preferences", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNotificationSettings(boolean enabled) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Update Firestore with the new notification setting
            db.collection("user_preferences").document(userId)
                    .update("notificationsEnabled", enabled)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Notifications " + (enabled ? "enabled" : "disabled"),
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // If update fails, revert the switch
                        notificationsSwitch.setChecked(!enabled);
                        Toast.makeText(getContext(), "Failed to update notification settings",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Please login to change notification settings",
                    Toast.LENGTH_SHORT).show();
            notificationsSwitch.setChecked(!enabled);
        }
    }

    private void toggleDarkMode(boolean darkMode) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Update Firestore with the new dark mode setting
            db.collection("user_preferences").document(userId)
                    .update("darkModeEnabled", darkMode)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Dark Mode " + (darkMode ? "enabled" : "disabled"),
                                Toast.LENGTH_SHORT).show();

                        // Apply dark mode change to the app
                        // This implementation depends on how you manage themes in your app
                        if (getActivity() instanceof ThemeManager) {
                            ((ThemeManager) getActivity()).setDarkMode(darkMode);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If update fails, revert the switch
                        darkModeSwitch.setChecked(!darkMode);
                        Toast.makeText(getContext(), "Failed to update dark mode settings",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Please login to change dark mode settings",
                    Toast.LENGTH_SHORT).show();
            darkModeSwitch.setChecked(!darkMode);
        }
    }

    private void navigateToAccountSettings() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getActivity(), Home.class);
            intent.putExtra("USER_ID", currentUser.getUid());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Please login to access account settings",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHelpSupport() {
        Intent intent = new Intent(getActivity(), Home.class);
        startActivity(intent);
    }

    private void navigateToAbout() {
        Intent intent = new Intent(getActivity(), Home.class);
        startActivity(intent);
    }

    private void logoutUser() {
        // Sign out from Firebase
        mAuth.signOut();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Interface for theme management
    public interface ThemeManager {
        void setDarkMode(boolean darkMode);
    }
}