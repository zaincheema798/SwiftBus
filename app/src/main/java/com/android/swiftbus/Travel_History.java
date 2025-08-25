package com.android.swiftbus;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Travel_History extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private LinearLayout layoutEmptyState;
    private ImageView btnBack;

    private BookingHistoryAdapter adapter;
    private List<BookingModel> bookingsList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_history);

        initViews();
        initFirebase();
        setupRecyclerView();
        loadBookingHistory();
    }

    private void initViews() {
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupRecyclerView() {
        bookingsList = new ArrayList<>();
        adapter = new BookingHistoryAdapter(this, bookingsList);

        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);
        recyclerViewHistory.setHasFixedSize(true);
    }

    private void loadBookingHistory() {
        String userEmail = auth.getCurrentUser() != null ?
                auth.getCurrentUser().getEmail() : "husnain.liaqat.56@gmail.com";

        db.collection("bookings")
                .whereEqualTo("userId", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingsList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingModel booking = document.toObject(BookingModel.class);
                        booking.setDocumentId(document.getId());
                        bookingsList.add(booking);
                    }

                    adapter.notifyDataSetChanged();
                    showRecyclerView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load booking history: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        recyclerViewHistory.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showRecyclerView() {
        layoutEmptyState.setVisibility(View.GONE);
        recyclerViewHistory.setVisibility(View.VISIBLE);
    }
}