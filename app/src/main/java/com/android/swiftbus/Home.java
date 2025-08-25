package com.android.swiftbus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView recyclerQuick, recyclerRecent, recyclerNotification;
    private List<QuickAccess> quickAccessList;
    private List<RecentRoute> recentRouteList;
    private List<Notification> notificationList;
    private RecentRouteAdapter recentRouteAdapter;
    private NotificationAdapter notificationAdapter;
    private QuickAccessAdapter quickAccessAdapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Quick Access RecyclerView
        recyclerQuick = view.findViewById(R.id.recycler_quick);
        recyclerNotification = view.findViewById(R.id.recyclerNotification);
        recyclerRecent = view.findViewById(R.id.recent_route);
        recyclerNotification.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
        );
        recyclerQuick.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRecent.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        quickAccessList = new ArrayList<>();
        quickAccessList.add(new QuickAccess("Book a Ticket", R.drawable.ic_booking_q));
        quickAccessList.add(new QuickAccess("Offers & Discounts", R.drawable.ic_booking_q));
        quickAccessList.add(new QuickAccess("Travel History", R.drawable.ic_booking_q));
        quickAccessList.add(new QuickAccess("Help & Support", R.drawable.ic_booking_q));
        quickAccessAdapter = new QuickAccessAdapter(getContext(), quickAccessList);
        recyclerQuick.setAdapter(quickAccessAdapter);

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        recyclerNotification.setAdapter(notificationAdapter);


        recentRouteList = new ArrayList<>();
        recentRouteAdapter = new RecentRouteAdapter(getContext(), recentRouteList);
        recyclerRecent.setAdapter(recentRouteAdapter);

        db = FirebaseFirestore.getInstance();
        // 3. Load data from Firestore
        loadRoutesFromFirestore();
        loadNotificationsFromFirestore();

        return view;
    }

    /**
     * Fetches documents from the "Route" collection in Firestore
     * and updates the RecentRouteAdapter.
     */
    private void loadRoutesFromFirestore() {
        db.collection("Route") // Firestore collection name
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert each document into a RecentRoute object.
                        // Make sure Firestore fields match RecentRoute fields (title, image)
                        RecentRoute route = doc.toObject(RecentRoute.class);
                        if (route != null) {
                            recentRouteList.add(route);
                            Log.d(TAG, "Loaded route: " + route.getTitle() + ", " + route.getImage());
                        }
                    }
                    recentRouteAdapter.updateData(recentRouteList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading routes from Firestore", e);
                });
    }

    void loadNotificationsFromFirestore() {
        db.collection("Notifications")
                .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(documentSnapchot -> {
                    notificationList.clear();
                    for (DocumentSnapshot doc : documentSnapchot.getResult()) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            notificationList.add(notification);
                        }
                    }
                    notificationAdapter.updateData(notificationList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications from Firestore", e);
                });
    }
}
