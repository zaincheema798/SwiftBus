package com.android.swiftbus;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Tracking extends Fragment implements OnMapReadyCallback {

    private TextInputEditText trackingIdEditText;
    private MaterialButton trackButton;
    private ImageView placeholderImage;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private MaterialCardView infoCard;
    private CircularProgressIndicator loadingIndicator;

    // Bus information TextViews
    private TextView sourceTextView;
    private TextView destinationTextView;
    private TextView currentLocationTextView;
    private TextView statusTextView;
    private TextView dispatchTimeTextView;
    private TextView arrivalTimeTextView;

    // Firestore reference
    private FirebaseFirestore db;

    // Cache for location data
    private Map<String, LocationData> locationCache = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        trackingIdEditText = view.findViewById(R.id.busNumberEditText);
        trackButton = view.findViewById(R.id.trackButton);
        placeholderImage = view.findViewById(R.id.placeholderImage);
        infoCard = view.findViewById(R.id.infoCard);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);

        // Initialize TextViews
        sourceTextView = view.findViewById(R.id.sourceTextView);
        destinationTextView = view.findViewById(R.id.destinationTextView);
        currentLocationTextView = view.findViewById(R.id.currentLocationTextView);
        statusTextView = view.findViewById(R.id.statusTextView);
        dispatchTimeTextView = view.findViewById(R.id.dispatchTimeTextView);
        arrivalTimeTextView = view.findViewById(R.id.arrivalTimeTextView);

        // Initialize map fragment
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.googleMap, mapFragment)
                    .commit();
        }
        mapFragment.getMapAsync(this);

        // Set up track button click listener
        trackButton.setOnClickListener(v -> {
            String inputId = trackingIdEditText.getText().toString().trim();
            if (!inputId.isEmpty()) {
                // First check if it's a booking ID
                trackBooking(inputId);
            } else {
                Toast.makeText(getContext(), "Please enter a booking ID or route ID", Toast.LENGTH_SHORT).show();
            }
        });

        // Pre-load locations data for better performance
        loadLocations();

        return view;
    }

    private void loadLocations() {
        db.collection("locations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        LocationData locationData = document.toObject(LocationData.class);
                        if (locationData.getName() != null) {
                            locationCache.put(locationData.getName(), locationData);
                        }
                        if (locationData.getCode() != null) {
                            locationCache.put(locationData.getCode(), locationData);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading locations: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Initial setup of map
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    private void trackBooking(String bookingId) {
        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);

        // Query Firestore for booking details
        db.collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Hide loading indicator
                        loadingIndicator.setVisibility(View.GONE);

                        // Hide placeholder image and show map
                        placeholderImage.setVisibility(View.GONE);
                        if (mapFragment.getView() != null) {
                            mapFragment.getView().setVisibility(View.VISIBLE);
                        }
                        infoCard.setVisibility(View.VISIBLE);

                        // Get booking data
                        String source = documentSnapshot.getString("source");
                        String destination = documentSnapshot.getString("destination");
                        String status = documentSnapshot.getString("status");
                        String schedule = documentSnapshot.getString("schedule");
                        String travelDate = String.valueOf(documentSnapshot.getDate("travelDate"));
                        String vehicleType = documentSnapshot.getString("vehicleType");

                        // Update UI with booking info
                        updateBookingInfo(source, destination, status, schedule, travelDate);

                        // Display source and destination on map
                        fetchAndDisplayLocations(source, destination);
                    } else {
                        // If not a booking ID, try as a route ID
                        trackRoute(bookingId);
                    }
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error retrieving booking: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void trackRoute(String routeId) {
        // Query Firestore for route details
        db.collection("Route")
                .document(routeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Hide loading indicator
                    loadingIndicator.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        // Hide placeholder image and show map
                        placeholderImage.setVisibility(View.GONE);
                        if (mapFragment.getView() != null) {
                            mapFragment.getView().setVisibility(View.VISIBLE);
                        }
                        infoCard.setVisibility(View.VISIBLE);

                        // Get route data
                        RecentRoute routeData = documentSnapshot.toObject(RecentRoute.class);
                        if (routeData != null) {
                            updateRouteInfo(routeData);
                            fetchAndDisplayLocations(routeData.getSource(), routeData.getDestination());
                        }
                    } else {
                        // Show error message if route not found
                        Toast.makeText(getContext(), "No booking or route found with this ID.",
                                Toast.LENGTH_SHORT).show();

                        // Reset view to placeholder
                        placeholderImage.setVisibility(View.VISIBLE);
                        if (mapFragment.getView() != null) {
                            mapFragment.getView().setVisibility(View.GONE);
                        }
                        infoCard.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Hide loading indicator
                    loadingIndicator.setVisibility(View.GONE);

                    // Show error message
                    Toast.makeText(getContext(), "Failed to retrieve data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBookingInfo(String source, String destination, String status, String schedule, String travelDate) {
        // Update TextViews with booking information
        sourceTextView.setText(source);
        destinationTextView.setText(destination);
        currentLocationTextView.setText("En Route"); // Default or could be determined based on status
        statusTextView.setText(status != null ? status : "PENDING");

        // Parse schedule for dispatch time if available
        if (schedule != null && schedule.contains("(") && schedule.contains(")")) {
            String timeInfo = schedule.substring(schedule.indexOf("(") + 1, schedule.indexOf(")"));
            dispatchTimeTextView.setText(timeInfo);
        } else {
            dispatchTimeTextView.setText(schedule);
        }

        // Format travel date for arrival time
        arrivalTimeTextView.setText(travelDate != null ? travelDate : "Not available");
    }

    private void updateRouteInfo(RecentRoute routeData) {
        // Update TextViews with route information
        sourceTextView.setText(routeData.getSource());
        destinationTextView.setText(routeData.getDestination());
        currentLocationTextView.setText("N/A"); // Not available in route data
        statusTextView.setText("Scheduled"); // Default status
        dispatchTimeTextView.setText(routeData.getTime());
        arrivalTimeTextView.setText("N/A"); // Not available in route data
    }

    private void fetchAndDisplayLocations(String source, String destination) {
        if (mMap != null) {
            // Clear previous markers
            mMap.clear();

            // Try to get locations from cache first
            LocationData sourceLocation = locationCache.get(source);
            LocationData destLocation = locationCache.get(destination);

            // Track whether we need to fetch locations
            boolean needToFetchSource = (sourceLocation == null);
            boolean needToFetchDest = (destLocation == null);

            // If we have both locations from cache, display them
            if (!needToFetchSource && !needToFetchDest) {
                addSourceMarker(sourceLocation);
                addDestinationMarker(destLocation);
                fitMapToMarkers(sourceLocation, destLocation);
                return;
            }

            // Otherwise, fetch missing locations
            if (needToFetchSource) {
                fetchLocation(source, "source", destination, destLocation);
            }

            if (needToFetchDest) {
                fetchLocation(destination, "destination", source, sourceLocation);
            }
        }
    }

    private void fetchLocation(String locationCode, String type, String otherLocationCode, LocationData otherLocation) {
        db.collection("locations")
                .whereEqualTo("code", locationCode)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        LocationData locationData = queryDocumentSnapshots.getDocuments().get(0)
                                .toObject(LocationData.class);

                        if (locationData != null) {
                            // Cache the location data
                            locationCache.put(locationCode, locationData);
                            locationCache.put(locationData.getName(), locationData);

                            if ("source".equals(type)) {
                                addSourceMarker(locationData);

                                // If we now have both locations, fit map to markers
                                if (otherLocation != null) {
                                    fitMapToMarkers(locationData, otherLocation);
                                }
                            } else {
                                addDestinationMarker(locationData);

                                // If we now have both locations, fit map to markers
                                if (otherLocation != null) {
                                    fitMapToMarkers(otherLocation, locationData);
                                }
                            }
                        }
                    } else {
                        // Try by name if code doesn't work
                        fetchLocationByName(locationCode, type, otherLocationCode, otherLocation);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching location: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void fetchLocationByName(String locationName, String type, String otherLocationCode, LocationData otherLocation) {
        db.collection("locations")
                .whereEqualTo("name", locationName)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        LocationData locationData = queryDocumentSnapshots.getDocuments().get(0)
                                .toObject(LocationData.class);

                        if (locationData != null) {
                            // Cache the location data
                            locationCache.put(locationName, locationData);
                            if (locationData.getCode() != null) {
                                locationCache.put(locationData.getCode(), locationData);
                            }

                            if ("source".equals(type)) {
                                addSourceMarker(locationData);

                                // If we now have both locations, fit map to markers
                                if (otherLocation != null) {
                                    fitMapToMarkers(locationData, otherLocation);
                                }
                            } else {
                                addDestinationMarker(locationData);

                                // If we now have both locations, fit map to markers
                                if (otherLocation != null) {
                                    fitMapToMarkers(otherLocation, locationData);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching location: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void addSourceMarker(LocationData locationData) {
        LatLng position = new LatLng(locationData.getLatitude(), locationData.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Source: " + locationData.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Move camera to source location if it's the only marker
        if (mMap.getCameraPosition().zoom < 7) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));
        }
    }

    private void addDestinationMarker(LocationData locationData) {
        LatLng position = new LatLng(locationData.getLatitude(), locationData.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Destination: " + locationData.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void addMidpointMarker(LatLng midPoint) {
        mMap.addMarker(new MarkerOptions()
                .position(midPoint)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void fitMapToMarkers(LocationData source, LocationData dest) {
        LatLng sourceLoc = new LatLng(source.getLatitude(), source.getLongitude());
        LatLng destLoc = new LatLng(dest.getLatitude(), dest.getLongitude());

        // Calculate the midpoint for camera position and add a marker there
        LatLng midPoint = new LatLng(
                (sourceLoc.latitude + destLoc.latitude) / 2,
                (sourceLoc.longitude + destLoc.longitude) / 2
        );

        // Add midpoint marker for current location
        addMidpointMarker(midPoint);

        // Calculate appropriate zoom level based on distance
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                sourceLoc.latitude, sourceLoc.longitude,
                destLoc.latitude, destLoc.longitude,
                results);

        float distance = results[0]; // distance in meters
        float zoom = calculateZoomLevel(distance);

        // Move camera to show both points
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midPoint, zoom));
    }

    private float calculateZoomLevel(float distance) {
        // Convert distance to appropriate zoom level
        // 1: World, 5: Landmass/continent, 10: City, 15: Streets, 20: Buildings
        double scale = distance / 500; // 500m fits nicely at zoom level 15
        double zoom = 15 - Math.log(scale) / Math.log(2);
        zoom = Math.max(Math.min(zoom, 15), 9); // Constrain between zoom levels 9-15
        return (float) zoom;
    }
}