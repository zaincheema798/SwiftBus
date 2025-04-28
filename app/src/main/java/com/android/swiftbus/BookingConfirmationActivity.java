package com.android.swiftbus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookingConfirmationActivity extends AppCompatActivity {

    private TextView textBookingNumber;
    private TextView textDestination;
    private TextView textSource;
    private TextView textSchedule;
    private TextView textSeatType;
    private TextView textSelectedSeats;
    private TextView textFare;
    private TextView textTravelDate;
    private TextView textVehicleType;
    private TextView textTimeRemaining;
    private TextView textBookingStatus;
    private Button buttonPayWithEasypaisa;
    private Button buttonUploadProof;
    private Button buttonSubmitPayment;
    private Button buttonCancelBooking;
    private Button buttonCopyBookingId;
    private ImageView imagePaymentProof;
    private CardView paymentProofCard;
    private CardView waitingMessageCard;
    private CardView congratulationsCard;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBarRefresh;
    private FirebaseFirestore db;
    private Uri imageUri;
    private String bookingId;
    private String userId;
    private CountDownTimer bookingTimer;
    private int totalFare;
    private static final long TIMEOUT_DURATION = 30 * 60 * 1000; // 30 minutes in milliseconds
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height for resizing
    private static final int REFRESH_INTERVAL = 30 * 1000; // 30 seconds in milliseconds
    private static final String PREF_NAME = "BookingPrefs";
    private static final String KEY_ACTIVE_BOOKING = "activeBookingId";
    private static final String KEY_EXPIRY_TIME = "bookingExpiryTime";
    private Handler refreshHandler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private boolean isRefreshing = false;
    private SharedPreferences prefs;
    private OnBackPressedCallback backPressedCallback;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    imagePaymentProof.setImageURI(imageUri);
                    imagePaymentProof.setVisibility(View.VISIBLE);
                    buttonSubmitPayment.setEnabled(true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        // Initialize Firebase services
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize UI elements
        initializeViews();

        // Handle back press
        setupBackPressHandler();

        // Retrieve booking ID from Intent or SharedPreferences
        retrieveBookingId();

        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Error: Booking information not found!", Toast.LENGTH_LONG).show();
            clearActiveBooking();
            goToMainActivity();
            return;
        }

        // Fetch and display booking data
        fetchBookingData(bookingId);

        // Start the countdown timer for booking expiration
        startBookingExpirationTimer();

        // Set up button click listeners
        setupButtonListeners();

        // Setup auto refresh
        setupAutoRefresh();
    }

    private void initializeViews() {
        textBookingNumber = findViewById(R.id.text_booking_number);
        textDestination = findViewById(R.id.text_destination);
        textSource = findViewById(R.id.text_source);
        textSchedule = findViewById(R.id.text_schedule);
        textSeatType = findViewById(R.id.text_seat_type);
        textSelectedSeats = findViewById(R.id.text_selected_seats);
        textFare = findViewById(R.id.text_fare);
        textTravelDate = findViewById(R.id.text_travel_date);
        textVehicleType = findViewById(R.id.text_vehicle_type);
        textTimeRemaining = findViewById(R.id.text_time_remaining);
        textBookingStatus = findViewById(R.id.text_booking_status);
        buttonPayWithEasypaisa = findViewById(R.id.button_pay_easypaisa);
        buttonUploadProof = findViewById(R.id.button_upload_proof);
        buttonSubmitPayment = findViewById(R.id.button_submit_payment);
        buttonCancelBooking = findViewById(R.id.button_cancel_booking);
        buttonCopyBookingId = findViewById(R.id.button_copy_booking_id);
        imagePaymentProof = findViewById(R.id.image_payment_proof);
        paymentProofCard = findViewById(R.id.card_payment_proof);
        waitingMessageCard = findViewById(R.id.card_waiting_message);
        congratulationsCard = findViewById(R.id.card_congratulations);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        progressBarRefresh = findViewById(R.id.progress_bar_refresh);

        // Initially hide some elements
        imagePaymentProof.setVisibility(View.GONE);
        waitingMessageCard.setVisibility(View.GONE);
        congratulationsCard.setVisibility(View.GONE);
        progressBarRefresh.setVisibility(View.GONE);
        buttonSubmitPayment.setEnabled(false);
        buttonCopyBookingId.setVisibility(View.GONE);

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshBookingStatus);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.secondary);
    }

    private void setupBackPressHandler() {
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check booking status before showing cancel dialog
                db.collection("bookings").document(bookingId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String status = documentSnapshot.getString("status");
                                String paymentStatus = documentSnapshot.getString("paymentStatus");

                                // Allow direct exit if booking is already paid or verified
                                if ("PAID".equals(status) || "VERIFIED".equals(paymentStatus)) {
                                    clearActiveBooking();
                                    goToMainActivity();
                                } else {
                                    // Show dialog to confirm exit or cancel
                                    new AlertDialog.Builder(BookingConfirmationActivity.this)
                                            .setTitle("Confirm Action")
                                            .setMessage("Your booking is still pending payment. Do you want to cancel this booking?")
                                            .setPositiveButton("Cancel Booking", (dialog, which) -> confirmCancelBooking())
                                            .setNegativeButton("Stay Here", (dialog, which) -> dialog.dismiss())
                                            .setCancelable(false)
                                            .show();
                                }
                            } else {
                                // If booking not found, just go back
                                clearActiveBooking();
                                goToMainActivity();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // If error fetching status, show the dialog as fallback
                            new AlertDialog.Builder(BookingConfirmationActivity.this)
                                    .setTitle("Confirm Action")
                                    .setMessage("Do you want to cancel this booking?")
                                    .setPositiveButton("Cancel Booking", (dialog, which) -> confirmCancelBooking())
                                    .setNegativeButton("Stay Here", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(false)
                                    .show();
                        });
            }
        };

        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    private void retrieveBookingId() {
        Intent intent = getIntent();
        bookingId = intent.getStringExtra("bookingId");

        // If no booking ID in intent, check shared preferences
        if (bookingId == null || bookingId.isEmpty()) {
            bookingId = prefs.getString(KEY_ACTIVE_BOOKING, "");
        } else {
            // If we got booking ID from intent, save it to preferences
            saveActiveBooking(bookingId);
        }
    }

    private void saveActiveBooking(String bookingId) {
        long expiryTime = System.currentTimeMillis() + TIMEOUT_DURATION;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACTIVE_BOOKING, bookingId);
        editor.putLong(KEY_EXPIRY_TIME, expiryTime);
        editor.apply();
    }

    private void clearActiveBooking() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_ACTIVE_BOOKING);
        editor.remove(KEY_EXPIRY_TIME);
        editor.apply();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchBookingData(String bookingId) {
        progressBarRefresh.setVisibility(View.VISIBLE);

        db.collection("bookings").document(bookingId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBarRefresh.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (documentSnapshot.exists()) {
                        // Get all booking details
                        String destination = documentSnapshot.getString("destination");
                        String source = documentSnapshot.getString("source");
                        String schedule = documentSnapshot.getString("schedule");
                        String seatType = documentSnapshot.getString("seatType");
                        String selectedSeats = formatSelectedSeats(documentSnapshot.get("selectedSeats"));
                        totalFare = documentSnapshot.getLong("totalFare") != null ?
                                documentSnapshot.getLong("totalFare").intValue() : 0;
                        String travelDate = formatTimestamp(documentSnapshot.getTimestamp("travelDate"));
                        String vehicleType = documentSnapshot.getString("vehicleType");
                        String status = documentSnapshot.getString("status");
                        String paymentStatus = documentSnapshot.getString("paymentStatus");

                        // Set data in TextViews
                        textBookingNumber.setText("Booking ID: " + bookingId);
                        textDestination.setText("Destination: " + destination);
                        textSource.setText("From: " + source);
                        textSchedule.setText("Schedule: " + schedule);
                        textSeatType.setText("Seat Type: " + seatType);
                        textSelectedSeats.setText("Selected Seats: " + selectedSeats);
                        textFare.setText("Total Fare: " + totalFare + " PKR");
                        textTravelDate.setText("Travel Date: " + travelDate);
                        textVehicleType.setText("Vehicle: " + vehicleType);
                        textBookingStatus.setText("Status: " + status);

                        // Check payment status and update UI accordingly
                        String paymentProofBase64 = documentSnapshot.getString("paymentProofBase64");

                        if ("PAID".equals(status) || "VERIFIED".equals(paymentStatus)) {
                            handlePaidBooking();
                            stopAutoRefresh();
                            // Do NOT clear active booking here to allow user to copy booking ID
                        } else if ("VERIFICATION_PENDING".equals(status)) {
                            if (paymentProofBase64 != null && !paymentProofBase64.isEmpty()) {
                                displayBase64Image(paymentProofBase64);
                            }
                            showVerificationPendingUI();
                        } else if ("EXPIRED".equals(status) || "CANCELLED".equals(status)) {
                            handleExpiredBooking();
                            stopAutoRefresh();
                            clearActiveBooking();
                        } else {
                            // Display payment proof if exists but state is still PENDING
                            if (paymentProofBase64 != null && !paymentProofBase64.isEmpty()) {
                                displayBase64Image(paymentProofBase64);
                            }
                        }
                    } else {
                        Toast.makeText(BookingConfirmationActivity.this,
                                "Booking not found!", Toast.LENGTH_SHORT).show();
                        clearActiveBooking();
                        goToMainActivity();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBarRefresh.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(BookingConfirmationActivity.this,
                            "Error fetching booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void refreshBookingStatus() {
        if (isRefreshing) return;

        isRefreshing = true;
        fetchBookingData(bookingId);

        // Reset refreshing flag after a delay to prevent spam refreshes
        new Handler(Looper.getMainLooper()).postDelayed(() -> isRefreshing = false, 1000);
    }

    private void setupAutoRefresh() {
        refreshRunnable = () -> {
            if (!isFinishing() && !isDestroyed()) {
                refreshBookingStatus();
                refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
            }
        };

        // Start the periodic refresh
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    private void stopAutoRefresh() {
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    private void displayBase64Image(String base64String) {
        try {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imagePaymentProof.setImageBitmap(bitmap);
            imagePaymentProof.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Error displaying image", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatSelectedSeats(Object selectedSeatsObj) {
        if (selectedSeatsObj instanceof java.util.List) {
            java.util.List<?> seats = (java.util.List<?>) selectedSeatsObj;
            StringBuilder seatsStr = new StringBuilder();
            for (Object seat : seats) {
                seatsStr.append(seat.toString()).append(", ");
            }
            if (seatsStr.length() > 0) {
                seatsStr.delete(seatsStr.length() - 2, seatsStr.length());
            }
            return seatsStr.toString();
        }
        return "N/A";
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private void startBookingExpirationTimer() {
        // Check if we have a saved expiry time
        long expiryTime = prefs.getLong(KEY_EXPIRY_TIME, 0);
        long currentTime = System.currentTimeMillis();
        long timeLeft;

        if (expiryTime > 0) {
            timeLeft = expiryTime - currentTime;
            // If timer expired while app was closed
            if (timeLeft <= 0) {
                markBookingAsExpired();
                return;
            }
        } else {
            // No saved time, use default timeout
            timeLeft = TIMEOUT_DURATION;
            // Save the new expiry time
            expiryTime = currentTime + timeLeft;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(KEY_EXPIRY_TIME, expiryTime);
            editor.apply();
        }

        if (bookingTimer != null) {
            bookingTimer.cancel();
        }

        bookingTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes);
                textTimeRemaining.setText(String.format(Locale.getDefault(),
                        "Time remaining: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                textTimeRemaining.setText("Time expired!");
                markBookingAsExpired();
            }
        }.start();
    }

    private void markBookingAsExpired() {
        // Update the booking status to EXPIRED
        DocumentReference bookingRef = db.collection("bookings").document(bookingId);
        bookingRef.update("status", "EXPIRED")
                .addOnSuccessListener(aVoid -> {
                    // Refresh the UI to show expired status
                    Toast.makeText(this, "Booking expired due to payment timeout", Toast.LENGTH_LONG).show();
                    handleExpiredBooking();
                    clearActiveBooking();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating expired status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleExpiredBooking() {
        // Update UI for expired booking
        buttonPayWithEasypaisa.setVisibility(View.GONE);
        buttonUploadProof.setVisibility(View.GONE);
        buttonSubmitPayment.setVisibility(View.GONE);
        paymentProofCard.setVisibility(View.GONE);
        waitingMessageCard.setVisibility(View.GONE);
        congratulationsCard.setVisibility(View.GONE);
        textBookingStatus.setText("Status: EXPIRED");
        textBookingStatus.setTextColor(getResources().getColor(R.color.error_dark, null));

        // Show expired message
        TextView textExpiredMessage = findViewById(R.id.text_expired_message);
        textExpiredMessage.setVisibility(View.VISIBLE);

        // Show go to main button
        Button buttonGoToMain = findViewById(R.id.button_go_to_main);
        buttonGoToMain.setVisibility(View.VISIBLE);
        buttonGoToMain.setOnClickListener(v -> goToMainActivity());

        // Stop timer
        if (bookingTimer != null) {
            bookingTimer.cancel();
        }
        textTimeRemaining.setText("Booking expired");
    }

    private void setupButtonListeners() {
        // Easypaisa payment button
        buttonPayWithEasypaisa.setOnClickListener(v -> {
            // Try to open Easypaisa app
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.easypaisa.easypaisa");
            if (intent != null) {
                Toast.makeText(this, "Opening Easypaisa...", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            } else {
                // If app not installed, show dialog with instructions
                new AlertDialog.Builder(this)
                        .setTitle("Easypaisa Not Found")
                        .setMessage("Please install Easypaisa app from Play Store or make the payment " +
                                "via Easypaisa mobile account. After payment, upload the screenshot.")
                        .setPositiveButton("OK", null)
                        .show();

            }
        });

        // Upload proof button
        buttonUploadProof.setOnClickListener(v ->
                getContent.launch("image/*"));

        // Submit payment button
        buttonSubmitPayment.setOnClickListener(v -> {
            if (imageUri != null) {
                convertImageToBase64AndUpload();
            }
        });

        // Cancel booking button
        buttonCancelBooking.setOnClickListener(v -> {
            confirmCancelBooking();
        });

        // Copy booking ID button
        buttonCopyBookingId.setOnClickListener(v -> {
            // Use clipboard service to copy the booking ID
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Booking ID", bookingId);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Booking ID copied to clipboard", Toast.LENGTH_SHORT).show();
        });
    }

    private void confirmCancelBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelBooking())
                .setNegativeButton("No, Keep Booking", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void cancelBooking() {
        // Update the booking status to CANCELLED
        progressBarRefresh.setVisibility(View.VISIBLE);
        DocumentReference bookingRef = db.collection("bookings").document(bookingId);

        bookingRef.update(
                        "status", "CANCELLED",
                        "cancelledTimestamp", FieldValue.serverTimestamp())
                .addOnSuccessListener(aVoid -> {
                    progressBarRefresh.setVisibility(View.GONE);
                    Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                    clearActiveBooking();
                    goToMainActivity();
                })
                .addOnFailureListener(e -> {
                    progressBarRefresh.setVisibility(View.GONE);
                    Toast.makeText(this, "Error cancelling booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void convertImageToBase64AndUpload() {
        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Processing")
                .setMessage("Please wait while we process your payment proof...")
                .setCancelable(false)
                .show();

        try {
            // Get input stream from URI
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

            // Resize bitmap if it's too large
            bitmap = resizeBitmap(bitmap);

            // Convert bitmap to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Update Firestore with the Base64 string - using document ID instead of email
            db.collection("bookings").document(bookingId)
                    .update(
                            "paymentProofBase64", base64Image,
                            "paymentStatus", "VERIFICATION_PENDING",
                            "status", "VERIFICATION_PENDING",
                            "paymentTimestamp", FieldValue.serverTimestamp()
                    )
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        showVerificationPendingUI();

                        // Cancel the timer as payment is submitted
                        if (bookingTimer != null) {
                            bookingTimer.cancel();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(BookingConfirmationActivity.this,
                                "Error updating payment status: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (FileNotFoundException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: Cannot read the image file", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float aspectRatio = (float) width / height;

        if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
            if (width > height) {
                width = MAX_IMAGE_SIZE;
                height = Math.round(width / aspectRatio);
            } else {
                height = MAX_IMAGE_SIZE;
                width = Math.round(height * aspectRatio);
            }
            return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
        }

        return originalBitmap;
    }

    private void showVerificationPendingUI() {
        // Show waiting message and hide payment options
        paymentProofCard.setVisibility(View.GONE);
        buttonPayWithEasypaisa.setVisibility(View.GONE);
        buttonUploadProof.setVisibility(View.GONE);
        buttonSubmitPayment.setVisibility(View.GONE);
        buttonCancelBooking.setVisibility(View.GONE);
        waitingMessageCard.setVisibility(View.VISIBLE);
        congratulationsCard.setVisibility(View.GONE);

        // Update status text
        textBookingStatus.setText("Status: VERIFICATION_PENDING");
        textTimeRemaining.setText("Payment proof submitted");
    }

    private void handlePaidBooking() {
        // Hide payment elements
        buttonPayWithEasypaisa.setVisibility(View.GONE);
        buttonUploadProof.setVisibility(View.GONE);
        buttonSubmitPayment.setVisibility(View.GONE);
        buttonCancelBooking.setVisibility(View.GONE);
        paymentProofCard.setVisibility(View.GONE);
        waitingMessageCard.setVisibility(View.GONE);

        // Show congratulations card instead of text message
        congratulationsCard.setVisibility(View.VISIBLE);

        // Update text in congratulations card
        TextView textCongratulationsMessage = findViewById(R.id.text_congratulations_message);
        textCongratulationsMessage.setText("Congratulations! Your booking has been confirmed. " +
                "Please keep your booking ID for future reference.");

        // Show and enable copy booking ID button
        buttonCopyBookingId.setVisibility(View.VISIBLE);

        // Show go to main button
        Button buttonGoToMain = findViewById(R.id.button_go_to_main);
        buttonGoToMain.setVisibility(View.VISIBLE);
        buttonGoToMain.setOnClickListener(v -> {
            clearActiveBooking();
            goToMainActivity();
        });

        // Stop timer
        if (bookingTimer != null) {
            bookingTimer.cancel();
        }
        textTimeRemaining.setText("Payment confirmed");

        // Update back press callback to not show cancel dialog
        backPressedCallback.setEnabled(false);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                clearActiveBooking();
                goToMainActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookingId != null && !bookingId.isEmpty()) {
            refreshBookingStatus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the timer to prevent memory leaks
        if (bookingTimer != null) {
            bookingTimer.cancel();
        }
        // Stop the auto-refresh
        stopAutoRefresh();
    }
}