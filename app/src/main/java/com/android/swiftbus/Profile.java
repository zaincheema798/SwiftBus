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

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Components
    private CircleImageView profileImage;
    private ImageView editProfileBtn;
    private TextView profileName, profileMembership, profileEmail, profilePhone, profileGender, profileAge, profileBlood, profileAddress;
    private Button logoutButton;

    // User data
    private UserProfile userProfile;

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

        // Account section
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
                        userProfile = documentSnapshot.toObject(UserProfile.class);

                        // Populate UI with user data
                        populateUserProfile();

                        // Load profile image if available
                        loadProfileImage();

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
                profileImage.setImageResource(R.drawable.profile_user_svgrepo_com);
            }
        } else {
            // Load default image if no base64 string
            profileImage.setImageResource(R.drawable.profile_user_svgrepo_com);
        }
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
    }

    private void setupClickListeners() {
        // Edit profile button
        editProfileBtn.setOnClickListener(v -> {
            navigateToEditProfile();
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            logoutUser();
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

    private void logoutUser() {
        // Sign out from Firebase
        mAuth.signOut();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        Intent intent = new Intent(getActivity(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}