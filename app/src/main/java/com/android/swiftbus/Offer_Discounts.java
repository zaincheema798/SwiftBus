package com.android.swiftbus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Offer_Discounts extends AppCompatActivity implements OffersAdapter.OnOfferClickListener {
    private static final String TAG = "OfferDiscountActivity";

    private RecyclerView recyclerViewOffers;
    private OffersAdapter offersAdapter;
    private List<OfferModel> offersList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_discounts);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load offers from Firestore
        loadOffersFromFirestore();
    }

    private void initializeViews() {
        recyclerViewOffers = findViewById(R.id.recycler_view_offers);

        // Setup toolbar if you have one
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Offers & Discounts");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        offersList = new ArrayList<>();
        offersAdapter = new OffersAdapter(offersList, this);
        recyclerViewOffers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOffers.setAdapter(offersAdapter);
    }

    private void loadOffersFromFirestore() {
        db.collection("offers")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            offersList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                OfferModel offer = document.toObject(OfferModel.class);
                                offer.setOfferId(document.getId());

                                // Check if offer is still valid
                                Date currentDate = new Date();
                                if (offer.getValidUntil() == null || offer.getValidUntil().after(currentDate)) {
                                    offersList.add(offer);
                                }
                            }

                            // If no offers from Firestore, add some default offers
                            if (offersList.isEmpty()) {
                                addDefaultOffers();
                            }

                            offersAdapter.notifyDataSetChanged();
                        } else {
                            Log.w(TAG, "Error getting offers", task.getException());
                            // Add default offers on error
                            addDefaultOffers();
                            offersAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void addDefaultOffers() {
        // Create default offers
        OfferModel offer1 = new OfferModel();
        offer1.setOfferId("DEFAULT_01");
        offer1.setTitle("Weekend Special");
        offer1.setDescription("Get 25% off on weekend bookings for Lahore to Karachi route");
        offer1.setDiscountPercentage(25);
        offer1.setDiscountType("PERCENTAGE");
        offer1.setOfferType("ROUTE_SPECIFIC");
        offer1.setSourceCity("Lahore");
        offer1.setDestinationCity("Karachi");
        offer1.setMinimumFare(2000);
        offer1.setMaxDiscountAmount(500);
        offer1.setOfferCode("WEEKEND25");
        offer1.setActive(true);
        offersList.add(offer1);

        OfferModel offer2 = new OfferModel();
        offer2.setOfferId("DEFAULT_02");
        offer2.setTitle("First Time User");
        offer2.setDescription("Flat PKR 300 off on your first booking");
        offer2.setDiscountAmount(300);
        offer2.setDiscountType("FIXED");
        offer2.setOfferType("FIRST_TIME");
        offer2.setMinimumFare(1000);
        offer2.setOfferCode("FIRST300");
        offer2.setActive(true);
        offersList.add(offer2);

        OfferModel offer3 = new OfferModel();
        offer3.setOfferId("DEFAULT_03");
        offer3.setTitle("Group Booking Discount");
        offer3.setDescription("Book for 3 or more passengers and get 15% off");
        offer3.setDiscountPercentage(15);
        offer3.setDiscountType("PERCENTAGE");
        offer3.setOfferType("GROUP_BOOKING");
        offer3.setMinimumPassengers(3);
        offer3.setMaxDiscountAmount(750);
        offer3.setOfferCode("GROUP15");
        offer3.setActive(true);
        offersList.add(offer3);

        OfferModel offer4 = new OfferModel();
        offer4.setOfferId("DEFAULT_04");
        offer4.setTitle("Islamabad Express Deal");
        offer4.setDescription("Special 30% discount on Rawalpindi to Islamabad routes");
        offer4.setDiscountPercentage(30);
        offer4.setDiscountType("PERCENTAGE");
        offer4.setOfferType("ROUTE_SPECIFIC");
        offer4.setSourceCity("Rawalpindi");
        offer4.setDestinationCity("Islamabad");
        offer4.setMaxDiscountAmount(400);
        offer4.setOfferCode("ISBD30");
        offer4.setActive(true);
        offersList.add(offer4);

        OfferModel offer5 = new OfferModel();
        offer5.setOfferId("DEFAULT_05");
        offer5.setTitle("Early Bird Special");
        offer5.setDescription("Book 7 days in advance and save PKR 200");
        offer5.setDiscountAmount(200);
        offer5.setDiscountType("FIXED");
        offer5.setOfferType("EARLY_BOOKING");
        offer5.setAdvanceBookingDays(7);
        offer5.setMinimumFare(1500);
        offer5.setOfferCode("EARLY200");
        offer5.setActive(true);
        offersList.add(offer5);

        OfferModel offer6 = new OfferModel();
        offer6.setOfferId("DEFAULT_06");
        offer6.setTitle("Premium Seat Upgrade");
        offer6.setDescription("Get 20% off on premium seat bookings");
        offer6.setDiscountPercentage(20);
        offer6.setDiscountType("PERCENTAGE");
        offer6.setOfferType("SEAT_TYPE");
        offer6.setApplicableSeatType("Premium");
        offer6.setMaxDiscountAmount(600);
        offer6.setOfferCode("PREMIUM20");
        offer6.setActive(true);
        offersList.add(offer6);
    }

    @Override
    public void onOfferClick(OfferModel offer) {
        // Create intent to navigate to booking with offer data
        Intent intent = new Intent();

        // Determine the target activity/fragment
        // If you want to go to MainActivity with Booking fragment
        intent.setClass(this, Main.class);

        // Pass offer data
        intent.putExtra("navigate_to", "booking");
        intent.putExtra("offer_id", offer.getOfferId());
        intent.putExtra("offer_title", offer.getTitle());
        intent.putExtra("offer_code", offer.getOfferCode());
        intent.putExtra("offer_type", offer.getOfferType());
        intent.putExtra("discount_type", offer.getDiscountType());
        intent.putExtra("discount_percentage", offer.getDiscountPercentage());
        intent.putExtra("discount_amount", offer.getDiscountAmount());
        intent.putExtra("max_discount_amount", offer.getMaxDiscountAmount());
        intent.putExtra("minimum_fare", offer.getMinimumFare());
        intent.putExtra("minimum_passengers", offer.getMinimumPassengers());
        intent.putExtra("advance_booking_days", offer.getAdvanceBookingDays());
        intent.putExtra("source_city", offer.getSourceCity());
        intent.putExtra("destination_city", offer.getDestinationCity());
        intent.putExtra("applicable_seat_type", offer.getApplicableSeatType());
        intent.putExtra("applicable_vehicle_type", offer.getApplicableVehicleType());

        // Start activity
        startActivity(intent);

        // Show toast
        Toast.makeText(this, "Offer applied: " + offer.getTitle(), Toast.LENGTH_SHORT).show();

        // Finish current activity
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}