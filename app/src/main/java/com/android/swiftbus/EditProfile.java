package com.android.swiftbus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private CircleImageView profileImageView;
    private FloatingActionButton changePhotoBtn;
    private TextInputEditText editName, editEmail, editPhone, editAge, editAddress;
    private TextInputEditText editEmergencyName, editEmergencyPhone, editEmergencyRelation;
    private TextInputEditText editCurrentPassword, editNewPassword, editConfirmPassword;
    private AutoCompleteTextView editGender, editBloodGroup;
    private MaterialButton saveButton, cancelButton;
    private FloatingActionButton fabSave;
    private CircleImageView coverImageView; // Add a UI element for cover image
    private FloatingActionButton changeCoverBtn; // Add a button to change cover image

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;

    // Variables
    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;
    private static final int PICK_COVER_IMAGE_REQUEST = 2;
    private Uri profileImageUri;
    private Uri coverImageUri;
    private ProgressDialog progressDialog;
    private boolean isProfileImageChanged = false;
    private boolean isCoverImageChanged = false;
    private String userId;
    private UserProfile currentUserProfile;
    private String membershipStatus;
    private int tripsCount, citiesCount, pointsCount;
    private String profileImageBase64;
    private String coverImageBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If user not logged in, redirect to login
            startActivity(new Intent(EditProfile.this, Login.class));
            finish();
            return;
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        userId = currentUser.getUid();
        userDocRef = db.collection("Users").document(userId);

        // Initialize UI components
        initializeUI();

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Load user data
        loadUserData();

        // Setup dropdown menus
        setupDropdownMenus();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        profileImageView = findViewById(R.id.edit_profile_image);
        changePhotoBtn = findViewById(R.id.change_photo_btn);

        // Cover image UI components - make sure these are in your layout
        // coverImageView = findViewById(R.id.edit_cover_image);
        // changeCoverBtn = findViewById(R.id.change_cover_btn);

        // Personal info fields
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editGender = findViewById(R.id.edit_gender);
        editAge = findViewById(R.id.edit_age);
        editBloodGroup = findViewById(R.id.edit_blood_group);
        editAddress = findViewById(R.id.edit_address);

        // Emergency contact fields
        editEmergencyName = findViewById(R.id.edit_emergency_name);
        editEmergencyPhone = findViewById(R.id.edit_emergency_phone);
        editEmergencyRelation = findViewById(R.id.edit_emergency_relation);

        // Password fields
        editCurrentPassword = findViewById(R.id.edit_current_password);
        editNewPassword = findViewById(R.id.edit_new_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);

        // Buttons
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        fabSave = findViewById(R.id.fab_save);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
    }

    private void setupDropdownMenus() {
        // Gender dropdown
        String[] genders = new String[]{"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, genders);
        editGender.setAdapter(genderAdapter);

        // Blood group dropdown
        String[] bloodGroups = new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, bloodGroups);
        editBloodGroup.setAdapter(bloodGroupAdapter);
    }

    private void setupClickListeners() {
        // Change profile photo
        changePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser(PICK_PROFILE_IMAGE_REQUEST);
            }
        });

        // Change cover photo - assuming you have this UI element
        // changeCoverBtn.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         openImageChooser(PICK_COVER_IMAGE_REQUEST);
        //     }
        // });

        // Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        // Floating save button (does the same as save button)
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        // Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openImageChooser(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, requestCode == PICK_PROFILE_IMAGE_REQUEST ?
                "Select Profile Image" : "Select Cover Image"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_PROFILE_IMAGE_REQUEST) {
                profileImageUri = data.getData();
                isProfileImageChanged = true;

                try {
                    // Convert to Base64
                    profileImageBase64 = convertImageUriToBase64(profileImageUri);

                    // Load selected image into ImageView
                    Glide.with(this)
                            .load(profileImageUri)
                            .into(profileImageView);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == PICK_COVER_IMAGE_REQUEST) {
                coverImageUri = data.getData();
                isCoverImageChanged = true;

                try {
                    // Convert to Base64
                    coverImageBase64 = convertImageUriToBase64(coverImageUri);

                    // Load selected image into ImageView
                    // Glide.with(this)
                    //        .load(coverImageUri)
                    //        .into(coverImageView);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String convertImageUriToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        // Resize the bitmap to reduce storage requirements (adjust dimensions as needed)
        Bitmap resizedBitmap = resizeBitmap(bitmap, 500, 500); // Resize to max 500x500

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream); // 70% quality
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float aspectRatio = (float) width / (float) height;

        if (width > height) {
            // Landscape
            width = maxWidth;
            height = Math.round(width / aspectRatio);
        } else {
            // Portrait
            height = maxHeight;
            width = Math.round(height * aspectRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void loadBase64Image(String base64String, CircleImageView imageView) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                // Load default image if the base64 string is corrupted
                imageView.setImageResource(R.drawable.profile);
            }
        } else {
            // Load default image if no base64 string
            imageView.setImageResource(R.drawable.profile);
        }
    }

    private void loadUserData() {
        progressDialog.show();

        // Load basic user info from Firebase Auth
        if (currentUser != null) {
            if (currentUser.getDisplayName() != null) {
                editName.setText(currentUser.getDisplayName());
            }
            if (currentUser.getEmail() != null) {
                editEmail.setText(currentUser.getEmail());
            }
        }

        // Load additional user info from Firestore if it exists, otherwise initialize with default values
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, get user profile data
                        String name = document.getString("name");
                        membershipStatus = document.getString("membershipStatus");
                        String email = document.getString("email");
                        String phone = document.getString("phone");
                        String gender = document.getString("gender");
                        String age = document.getString("age");
                        String bloodGroup = document.getString("bloodGroup");
                        String address = document.getString("address");

                        // Get Base64 image strings
                        profileImageBase64 = document.getString("profileImageBase64");
                        coverImageBase64 = document.getString("coverImageBase64");

                        // Get counts (with fallback to 0 if not found)
                        if (document.getLong("tripsCount") != null) {
                            tripsCount = document.getLong("tripsCount").intValue();
                        }
                        if (document.getLong("citiesCount") != null) {
                            citiesCount = document.getLong("citiesCount").intValue();
                        }
                        if (document.getLong("pointsCount") != null) {
                            pointsCount = document.getLong("pointsCount").intValue();
                        }

                        // Fill the form fields
                        if (name != null) editName.setText(name);
                        if (email != null) editEmail.setText(email);
                        if (phone != null) editPhone.setText(phone);
                        if (gender != null) editGender.setText(gender);
                        if (age != null) editAge.setText(age);
                        if (bloodGroup != null) editBloodGroup.setText(bloodGroup);
                        if (address != null) editAddress.setText(address);

                        // Load emergency contact info if available
                        Map<String, Object> emergency = (Map<String, Object>) document.get("emergency");
                        if (emergency != null) {
                            String emergencyName = (String) emergency.get("name");
                            String emergencyPhone = (String) emergency.get("phone");
                            String emergencyRelation = (String) emergency.get("relation");

                            if (emergencyName != null) editEmergencyName.setText(emergencyName);
                            if (emergencyPhone != null) editEmergencyPhone.setText(emergencyPhone);
                            if (emergencyRelation != null) editEmergencyRelation.setText(emergencyRelation);
                        }

                        // Load profile image from Base64 string
                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            loadBase64Image(profileImageBase64, profileImageView);
                        } else if (currentUser.getPhotoUrl() != null) {
                            // Fallback to Auth photo URL if no Base64 image
                            Glide.with(EditProfile.this)
                                    .load(currentUser.getPhotoUrl())
                                    .placeholder(R.drawable.profile)
                                    .into(profileImageView);
                        }

                        // Load cover image from Base64 string
                        // if (coverImageBase64 != null && !coverImageBase64.isEmpty() && coverImageView != null) {
                        //     loadBase64Image(coverImageBase64, coverImageView);
                        // }
                    } else {
                        // Document doesn't exist, we'll create it when user saves
                        membershipStatus = "Standard";
                        tripsCount = 0;
                        citiesCount = 0;
                        pointsCount = 0;

                        // Set some default values from Firebase Auth if available
                        if (currentUser.getDisplayName() != null) {
                            editName.setText(currentUser.getDisplayName());
                        }
                        if (currentUser.getEmail() != null) {
                            editEmail.setText(currentUser.getEmail());
                        }

                        Toast.makeText(EditProfile.this, "No profile found. Please update your information.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error loading document
                    Toast.makeText(EditProfile.this, "Failed to load user data: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();

                    // Set default values
                    membershipStatus = "Standard";
                    tripsCount = 0;
                    citiesCount = 0;
                    pointsCount = 0;
                }
                progressDialog.dismiss();
            }
        });
    }

    private void saveUserData() {
        progressDialog.show();

        // Get values from input fields
        String name = Objects.requireNonNull(editName.getText()).toString().trim();
        String email = Objects.requireNonNull(editEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(editPhone.getText()).toString().trim();
        String gender = editGender.getText().toString().trim();
        String age = Objects.requireNonNull(editAge.getText()).toString().trim();
        String bloodGroup = editBloodGroup.getText().toString().trim();
        String address = Objects.requireNonNull(editAddress.getText()).toString().trim();

        String emergencyName = Objects.requireNonNull(editEmergencyName.getText()).toString().trim();
        String emergencyPhone = Objects.requireNonNull(editEmergencyPhone.getText()).toString().trim();
        String emergencyRelation = Objects.requireNonNull(editEmergencyRelation.getText()).toString().trim();

        String currentPassword = Objects.requireNonNull(editCurrentPassword.getText()).toString().trim();
        String newPassword = Objects.requireNonNull(editNewPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(editConfirmPassword.getText()).toString().trim();

        // Validate input
        if (TextUtils.isEmpty(name)) {
            editName.setError("Name is required");
            progressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email is required");
            progressDialog.dismiss();
            return;
        }

        // Create a map for user data update
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("email", email);
        userUpdates.put("phone", phone);
        userUpdates.put("gender", gender);
        userUpdates.put("age", age);
        userUpdates.put("bloodGroup", bloodGroup);
        userUpdates.put("address", address);
        userUpdates.put("membershipStatus", membershipStatus != null ? membershipStatus : "Standard");
        userUpdates.put("tripsCount", tripsCount);
        userUpdates.put("citiesCount", citiesCount);
        userUpdates.put("pointsCount", pointsCount);

        // Emergency contact info
        Map<String, Object> emergencyUpdates = new HashMap<>();
        emergencyUpdates.put("name", emergencyName);
        emergencyUpdates.put("phone", emergencyPhone);
        emergencyUpdates.put("relation", emergencyRelation);
        userUpdates.put("emergency", emergencyUpdates);

        // Add profile image Base64 string if changed
        if (isProfileImageChanged && profileImageBase64 != null) {
            userUpdates.put("profileImageBase64", profileImageBase64);
        } else if (profileImageBase64 != null) {
            // Preserve existing profile image if not changed
            userUpdates.put("profileImageBase64", profileImageBase64);
        }

        // Add cover image Base64 string if changed
        if (isCoverImageChanged && coverImageBase64 != null) {
            userUpdates.put("coverImageBase64", coverImageBase64);
        } else if (coverImageBase64 != null) {
            // Preserve existing cover image if not changed
            userUpdates.put("coverImageBase64", coverImageBase64);
        }

        // Update user data in Firestore
        updateUserDataInFirestore(userUpdates);

        // Handle password change if requested
        if (!TextUtils.isEmpty(currentPassword) && !TextUtils.isEmpty(newPassword)) {
            if (!newPassword.equals(confirmPassword)) {
                editConfirmPassword.setError("Passwords do not match");
                progressDialog.dismiss();
                return;
            }

            // Update password
            updatePassword(currentPassword, newPassword);
        }
    }

    private void updateUserDataInFirestore(Map<String, Object> userUpdates) {
        // Update display name in Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(Objects.requireNonNull(editName.getText()).toString().trim())
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update email if changed
                            String newEmail = Objects.requireNonNull(editEmail.getText()).toString().trim();
                            if (!newEmail.equals(currentUser.getEmail())) {
                                currentUser.updateEmail(newEmail)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(EditProfile.this, "Failed to update email: " +
                                                                    Objects.requireNonNull(task.getException()).getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });

        // Check if document exists and then update or create
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, update it
                        userDocRef.update(userUpdates)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProfile.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Document doesn't exist, create it
                        userDocRef.set(userUpdates)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProfile.this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(EditProfile.this, "Failed to create profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    // Error checking document existence
                    progressDialog.dismiss();
                    Toast.makeText(EditProfile.this, "Error accessing database: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePassword(String currentPassword, final String newPassword) {
        // Re-authenticate user before changing password
        AuthCredential credential = EmailAuthProvider.getCredential(
                Objects.requireNonNull(currentUser.getEmail()), currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // User re-authenticated, now change password
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditProfile.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(EditProfile.this, "Failed to update password: " +
                                                                Objects.requireNonNull(task.getException()).getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            editCurrentPassword.setError("Current password is incorrect");
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}