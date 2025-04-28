package com.android.swiftbus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Booking extends Fragment {
    private static final String TAG = "BookingFragment";

    // Firebase
    private FirebaseFirestore db;

    // UI elements
    private Button datePickerButton;
    private TextView selectedDateText;
    private Spinner ticketTypeSpinner;
    private Spinner passengersSpinner;
    private Spinner tripTypeSpinner;
    private Spinner vehicleTypeSpinner;
    private Spinner sourceSpinner;
    private Spinner destSpinner;
    private Spinner seatTypeSpinner;
    private Spinner schedulesSpinner;
    private GridLayout seatMapGrid;
    private ImageView vehicleDiagramImage;
    private TextView selectedSeatsText;
    private EditText specialRequests;
    private Button bookTicketButton;
    private TextView fareText;
    private TextView availabilityText;
    private TextView optionsCostText;
    private TextView taxesText;
    private TextView totalFareText;
    private TextView conditionsText;
    private TextView messageText;

    // Checkboxes
    private CheckBox blanketSeatCheck;
    private CheckBox windowSeatCheck;
    private CheckBox mealOnRideCheck;
    private CheckBox luggageCheck;
    private CheckBox wifiCheck;
    private CheckBox powerOutletCheck;

    // Pricing variables
    private int baseFare = 2500; // Default value, will be replaced by route price
    private int optionsCost = 0;
    private final int taxRate = 10; // 10% tax
    private int totalFare = 0;

    // Selected values
    private String selectedTicketType = "";
    private int selectedPassengers = 1;
    private String selectedTripType = "";
    private String selectedVehicleType = "";
    private String selectedSource = "";
    private String selectedDestination = "";
    private String selectedSeatType = "";
    private Date selectedDate = null;
    private String selectedSchedule = "";
    private List<String> selectedSeats = new ArrayList<>();

    // Data lists
    private List<String> ticketTypes = new ArrayList<>();
    private List<String> vehicleTypes = new ArrayList<>();
    private List<String> seatTypes = new ArrayList<>();
    private List<String> sources = new ArrayList<>();
    private List<String> destinations = new ArrayList<>();
    private Map<String, List<String>> schedules = new HashMap<>();

    // Pricing maps
    private Map<String, Integer> ticketTypePrices = new HashMap<>();
    private Map<String, Integer> seatTypePrices = new HashMap<>();
    private Map<String, Integer> vehicleTypePrices = new HashMap<>();
    private Map<String, Integer> optionPrices = new HashMap<>();
    private Map<String, String> ticketConditions = new HashMap<>();

    // Route data
    private RecentRoute route;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Check if we have a route from arguments
        if (getArguments() != null && getArguments().containsKey("route")) {
            route = (RecentRoute) getArguments().getSerializable("route");
            if (route != null) {
                // Use route price as base fare
                baseFare = Integer.parseInt(route.getPrice());
            }
        }

        // Initialize UI elements
        initializeUI(view);

        // Load data from Firestore
        loadDataFromFirestore();

        // Setup date picker
        setupDatePicker();

        // Setup checkboxes
        setupCheckboxes();

        // Setup special requests
        setupSpecialRequests();

        // Setup book ticket button
        setupBookButton();

        return view;
    }

    private void initializeUI(View view) {
        // Initialize spinners
        ticketTypeSpinner = view.findViewById(R.id.spinner_ticket_type);
        passengersSpinner = view.findViewById(R.id.spinner_no_of_passengers);
        tripTypeSpinner = view.findViewById(R.id.spinner_trip_type);
        vehicleTypeSpinner = view.findViewById(R.id.spinner_vehicle_type);
        sourceSpinner = view.findViewById(R.id.spinner_source);
        destSpinner = view.findViewById(R.id.spinner_destination);
        seatTypeSpinner = view.findViewById(R.id.spinner_seat_type);
        schedulesSpinner = view.findViewById(R.id.spinner_schedules);

        // Initialize seat map elements
        seatMapGrid = view.findViewById(R.id.grid_seat_map);
        vehicleDiagramImage = view.findViewById(R.id.image_vehicle_diagram);
        selectedSeatsText = view.findViewById(R.id.text_selected_seats);

        // Initialize text views
        fareText = view.findViewById(R.id.text_fare);
        availabilityText = view.findViewById(R.id.text_availability);
        optionsCostText = view.findViewById(R.id.text_options_cost);
        taxesText = view.findViewById(R.id.text_taxes);
        totalFareText = view.findViewById(R.id.text_total_fare);
        conditionsText = view.findViewById(R.id.text_conditions);
        messageText = view.findViewById(R.id.text_message);

        // Initialize date picker
        datePickerButton = view.findViewById(R.id.button_pick_date);
        selectedDateText = view.findViewById(R.id.text_selected_date);

        // Initialize special requests
        specialRequests = view.findViewById(R.id.edit_special_requests);

        // Initialize checkboxes
        blanketSeatCheck = view.findViewById(R.id.checkbox_blanket_seat);
        windowSeatCheck = view.findViewById(R.id.checkbox_window_seat);
        mealOnRideCheck = view.findViewById(R.id.checkbox_meal_on_ride);
        luggageCheck = view.findViewById(R.id.checkbox_luggage);
        wifiCheck = view.findViewById(R.id.checkbox_wifi);
        powerOutletCheck = view.findViewById(R.id.checkbox_power_outlet);

        // Initialize book button
        bookTicketButton = view.findViewById(R.id.button_book_ticket);
    }

    private void loadDataFromFirestore() {
        // Load ticket types
        db.collection("ticketType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ticketTypes.clear();
                            ticketTypes.add("Select Ticket Type");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String type = document.getString("name");
                                ticketTypes.add(type);

                                // Store price adjustment (as percentage)
                                if (document.contains("priceAdjustment")) {
                                    ticketTypePrices.put(type, document.getLong("priceAdjustment").intValue());
                                }

                                // Store conditions
                                if (document.contains("conditions")) {
                                    ticketConditions.put(type, document.getString("conditions"));
                                }
                            }

                            setupTicketTypeSpinner();
                        } else {
                            Log.w(TAG, "Error getting ticket types", task.getException());
                        }
                    }
                });

        // Load vehicle types
        db.collection("vehicleType")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            vehicleTypes.clear();
                            vehicleTypes.add("Select Vehicle Type");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String type = document.getString("name");
                                vehicleTypes.add(type);

                                // Store price adjustment
                                if (document.contains("priceAdjustment")) {
                                    vehicleTypePrices.put(type, document.getLong("priceAdjustment").intValue());
                                }
                            }

                            setupVehicleTypeSpinner();
                        } else {
                            Log.w(TAG, "Error getting vehicle types", task.getException());
                        }
                    }
                });

        // Load seat types
        db.collection("seatTypes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            seatTypes.clear();
                            seatTypes.add("Select Seat Type");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String type = document.getString("name");
                                seatTypes.add(type);

                                // Store price adjustment
                                if (document.contains("priceAdjustment")) {
                                    seatTypePrices.put(type, document.getLong("priceAdjustment").intValue());
                                }
                            }

                            setupSeatTypeSpinner();
                        } else {
                            Log.w(TAG, "Error getting seat types", task.getException());
                        }
                    }
                });

        // Load additional options prices
        db.collection("additionalOptions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String option = document.getId();
                                int price = document.getLong("price").intValue();
                                optionPrices.put(option, price);
                            }
                        } else {
                            Log.w(TAG, "Error getting additional options", task.getException());
                        }
                    }
                });

        // Load sources and destinations
        if (route != null) {
            // If we have a route, use its source/destination
            sources.clear();
            sources.add(route.getSource());
            selectedSource = route.getSource();

            destinations.clear();
            destinations.add(route.getDestination());
            selectedDestination = route.getDestination();

            // Make spinners non-editable when route is provided
            sourceSpinner.setEnabled(false);
            destSpinner.setEnabled(false);

            setupLocationSpinners();
        } else {
            // Load locations from Firestore
            db.collection("locations")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                sources.clear();
                                sources.add("Select Source");

                                destinations.clear();
                                destinations.add("Select Destination");

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String location = document.getString("name");
                                    sources.add(location);
                                    destinations.add(location);
                                }

                                setupLocationSpinners();
                            } else {
                                Log.w(TAG, "Error getting locations", task.getException());
                            }
                        }
                    });
        }

        // Setup other spinners with default values
        setupPassengersSpinner();
        setupTripTypeSpinner();
    }

    private void setupTicketTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ticketTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ticketTypeSpinner.setAdapter(adapter);

        ticketTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    selectedTicketType = ticketTypes.get(position);

                    // Update conditions text
                    if (ticketConditions.containsKey(selectedTicketType)) {
                        conditionsText.setText(ticketConditions.get(selectedTicketType));
                    } else {
                        conditionsText.setText("By proceeding with booking, you agree to our terms and conditions.");
                    }

                    updateFareDisplay();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupPassengersSpinner() {
        List<String> passengerOptions = new ArrayList<>();
        passengerOptions.add("Select");
        for (int i = 1; i <= 5; i++) {
            passengerOptions.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, passengerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        passengersSpinner.setAdapter(adapter);

        passengersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    selectedPassengers = Integer.parseInt(passengerOptions.get(position));
                    updateFareDisplay();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupTripTypeSpinner() {
        List<String> tripTypes = new ArrayList<>();
        tripTypes.add("Select Trip Type");
        tripTypes.add("One Way");
        tripTypes.add("Round Trip");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, tripTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tripTypeSpinner.setAdapter(adapter);

        tripTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    selectedTripType = tripTypes.get(position);
                    updateFareDisplay();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupVehicleTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, vehicleTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleTypeSpinner.setAdapter(adapter);

        vehicleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    selectedVehicleType = vehicleTypes.get(position);

                    // Load seat map based on vehicle type
                    loadSeatMap(selectedVehicleType);

                    updateFareDisplay();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupLocationSpinners() {
        // Setup source spinner
        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, sources);
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sourceSpinner.setAdapter(sourceAdapter);

        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSource = sources.get(position);
                updateAvailableSchedules();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup destination spinner
        ArrayAdapter<String> destAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, destinations);
        destAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destSpinner.setAdapter(destAdapter);

        destSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDestination = destinations.get(position);
                updateAvailableSchedules();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupSeatTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, seatTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seatTypeSpinner.setAdapter(adapter);

        seatTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    selectedSeatType = seatTypes.get(position);
                    updateFareDisplay();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateAvailableSchedules() {
        // Check if we have valid source and destination
        if (selectedSource.isEmpty() || selectedSource.equals("Select Source") ||
                selectedDestination.isEmpty() || selectedDestination.equals("Select Destination")) {

            List<String> defaultSchedules = new ArrayList<>();
            defaultSchedules.add("Select a source and destination first");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, defaultSchedules);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            schedulesSpinner.setAdapter(adapter);
            return;
        }

        // Load schedules from Firestore based on source and destination
        String routeId = selectedSource + "-" + selectedDestination;
        db.collection("routes").document(routeId)
                .collection("schedules")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> availableSchedules = new ArrayList<>();
                            availableSchedules.add("Select Schedule");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String departure = document.getString("departure");
                                String arrival = document.getString("arrival");
                                String schedule = departure + " - " + arrival + " (" + selectedSource + " to " + selectedDestination + ")";
                                availableSchedules.add(schedule);
                            }

                            if (availableSchedules.size() == 1) {
                                // No schedules found, show default options
                                availableSchedules.add("Morning (6:00 AM - " + selectedSource + " to " + selectedDestination + ")");
                                availableSchedules.add("Afternoon (12:00 PM - " + selectedSource + " to " + selectedDestination + ")");
                                availableSchedules.add("Evening (6:00 PM - " + selectedSource + " to " + selectedDestination + ")");
                                availableSchedules.add("Night (10:00 PM - " + selectedSource + " to " + selectedDestination + ")");
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                    android.R.layout.simple_spinner_item, availableSchedules);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            schedulesSpinner.setAdapter(adapter);

                            // Setup listener for schedule selection
                            schedulesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (position != 0) {
                                        selectedSchedule = availableSchedules.get(position);

                                        // Update availability
                                        checkAvailability();
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    // Do nothing
                                }
                            });
                        } else {
                            Log.w(TAG, "Error getting schedules", task.getException());
                        }
                    }
                });
    }

    private void loadSeatMap(String vehicleType) {
        // Clear existing views
        seatMapGrid.removeAllViews();
        selectedSeats.clear();
        selectedSeatsText.setText("Selected seats: None");

        // Load vehicle diagram image based on vehicle type
        int imageResource = getResources().getIdentifier(
                "vehicle_" + vehicleType.toLowerCase().replace(" ", "_"),
                "drawable",
                requireContext().getPackageName());

        if (imageResource != 0) {
            vehicleDiagramImage.setImageResource(imageResource);
        } else {
            // Use default image
            vehicleDiagramImage.setImageResource(R.drawable.default_vehicle);
        }

        // Load seat map from Firestore
        db.collection("vehicleType").document(vehicleType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.contains("seatLayout")) {
                                Map<String, Object> seatLayout = (Map<String, Object>) document.get("seatLayout");
                                int rows = ((Long) seatLayout.get("rows")).intValue();
                                int cols = ((Long) seatLayout.get("columns")).intValue();

                                // Get unavailable seats if any
                                List<String> unavailableSeats = new ArrayList<>();
                                if (document.contains("unavailableSeats")) {
                                    unavailableSeats = (List<String>) document.get("unavailableSeats");
                                }

                                // Create seat map
                                createSeatMap(rows, cols, unavailableSeats);
                            } else {
                                // Create default seat map
                                createSeatMap(6, 5, new ArrayList<>());
                            }
                        } else {
                            Log.w(TAG, "Error getting seat layout", task.getException());
                            // Create default seat map
                            createSeatMap(6, 5, new ArrayList<>());
                        }
                    }
                });
    }

    private void createSeatMap(int rows, int cols, List<String> unavailableSeats) {
        seatMapGrid.setColumnCount(cols);

        // Listener for seat selection
        View.OnClickListener seatClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String seatId = (String) v.getTag();

                if (v.isSelected()) {
                    // Deselect seat
                    v.setSelected(false);
                    v.setBackgroundResource(R.drawable.seat_available);
                    selectedSeats.remove(seatId);
                } else {
                    // Select seat (if we haven't already selected max passengers)
                    if (selectedSeats.size() < selectedPassengers) {
                        v.setSelected(true);
                        v.setBackgroundResource(R.drawable.seat_selected);
                        selectedSeats.add(seatId);
                    } else {
                        Toast.makeText(requireContext(),
                                "You can only select " + selectedPassengers + " seat(s)",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                // Update selected seats text
                updateSelectedSeatsText();
            }
        };

        // Create seats
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String seatId = String.format("%c%d", 'A' + row, col + 1);

                // Check if this is an aisle position
                boolean isAisle = (col == cols / 2);

                if (isAisle) {
                    // Add an empty view for aisle
                    View aisleView = new View(requireContext());
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = getResources().getDimensionPixelSize(R.dimen.seat_width);
                    params.height = getResources().getDimensionPixelSize(R.dimen.seat_height);
                    params.setMargins(8, 8, 8, 8);
                    aisleView.setLayoutParams(params);
                    seatMapGrid.addView(aisleView);
                } else {
                    // Add seat button
                    Button seatButton = new Button(requireContext());
                    seatButton.setText(seatId);
                    seatButton.setTag(seatId);

                    // Set seat status (available or unavailable)
                    if (unavailableSeats.contains(seatId)) {
                        seatButton.setBackgroundResource(R.drawable.seat_unavailable);
                        seatButton.setEnabled(false);
                    } else {
                        seatButton.setBackgroundResource(R.drawable.seat_available);
                        seatButton.setOnClickListener(seatClickListener);
                    }

                    // Add to grid
                    seatMapGrid.addView(seatButton);
                }
            }
        }
    }

    private void updateSelectedSeatsText() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsText.setText("Selected seats: None");
        } else {
            StringBuilder seatsText = new StringBuilder("Selected seats: ");
            for (int i = 0; i < selectedSeats.size(); i++) {
                seatsText.append(selectedSeats.get(i));
                if (i < selectedSeats.size() - 1) {
                    seatsText.append(", ");
                }
            }
            selectedSeatsText.setText(seatsText.toString());
        }
    }

    private void checkAvailability() {
        // In a real app, this would check with the backend/Firestore
        // For now, just simulate availability
        int randomPercent = (int) (Math.random() * 100);

        if (randomPercent < 80) {
            availabilityText.setText("Availability: Available");
            bookTicketButton.setEnabled(true);
        } else {
            availabilityText.setText("Availability: Limited seats");
            bookTicketButton.setEnabled(true);
        }
    }

    private void setupDatePicker() {
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(year, month, dayOfMonth);
                                selectedDate = calendar.getTime();

                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                                String formattedDate = dateFormat.format(selectedDate);
                                selectedDateText.setText(formattedDate);
                            }
                        },
                        year, month, day
                );

                // Set minimum date to today
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });
    }

    private void setupCheckboxes() {
        CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateOptionsCost();
            }
        };

        blanketSeatCheck.setOnCheckedChangeListener(checkListener);
        windowSeatCheck.setOnCheckedChangeListener(checkListener);
        mealOnRideCheck.setOnCheckedChangeListener(checkListener);
        luggageCheck.setOnCheckedChangeListener(checkListener);
        wifiCheck.setOnCheckedChangeListener(checkListener);
        powerOutletCheck.setOnCheckedChangeListener(checkListener);
    }
    private void updateOptionsCost() {
        optionsCost = 0;

        // Calculate cost based on selected options
        if (blanketSeatCheck.isChecked()) {
            optionsCost += optionPrices.getOrDefault("blanket", 150);
        }
        if (windowSeatCheck.isChecked()) {
            optionsCost += optionPrices.getOrDefault("windowSeat", 200);
        }
        if (mealOnRideCheck.isChecked()) {
            optionsCost += optionPrices.getOrDefault("meal", 350);
        }
        if (luggageCheck.isChecked()) {
            optionsCost += optionPrices.getOrDefault("luggage", 250);
        }
        if (wifiCheck.isChecked()) {
            optionsCost += optionPrices.getOrDefault("wifi", 100);
        }
        if (powerOutletCheck.isChecked()) {
            optionsCost += optionPrices.getOrDefault("powerOutlet", 50);
        }

        updateFareDisplay();
    }

    private void setupSpecialRequests() {
        specialRequests.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Validate text if needed
                if (s.length() > 200) {
                    specialRequests.setError("Maximum 200 characters allowed");
                }
            }
        });
    }

    private void updateFareDisplay() {
        // Calculate the base fare
        int calculatedBaseFare = baseFare;

        // Apply ticket type adjustment
        if (!selectedTicketType.isEmpty() && ticketTypePrices.containsKey(selectedTicketType)) {
            int percentage = ticketTypePrices.get(selectedTicketType);
            calculatedBaseFare = calculatedBaseFare + (calculatedBaseFare * percentage / 100);
        }

        // Apply seat type adjustment
        if (!selectedSeatType.isEmpty() && seatTypePrices.containsKey(selectedSeatType)) {
            int percentage = seatTypePrices.get(selectedSeatType);
            calculatedBaseFare = calculatedBaseFare + (calculatedBaseFare * percentage / 100);
        }

        // Apply vehicle type adjustment
        if (!selectedVehicleType.isEmpty() && vehicleTypePrices.containsKey(selectedVehicleType)) {
            int percentage = vehicleTypePrices.get(selectedVehicleType);
            calculatedBaseFare = calculatedBaseFare + (calculatedBaseFare * percentage / 100);
        }

        // Apply round trip discount
        if (selectedTripType.equals("Round Trip")) {
            calculatedBaseFare = calculatedBaseFare * 2; // Double the fare
            calculatedBaseFare = calculatedBaseFare - (calculatedBaseFare * 10 / 100); // 10% discount
        }

        // Apply multiplier for number of passengers
        calculatedBaseFare = calculatedBaseFare * selectedPassengers;

        // Calculate taxes
        int taxes = (calculatedBaseFare + optionsCost) * taxRate / 100;

        // Calculate total fare
        totalFare = calculatedBaseFare + optionsCost + taxes;

        // Update UI
        fareText.setText(String.format(Locale.getDefault(), "Base Fare: PKR %,d", calculatedBaseFare));
        optionsCostText.setText(String.format(Locale.getDefault(), "Options: PKR %,d", optionsCost));
        taxesText.setText(String.format(Locale.getDefault(), "Taxes (%d%%): PKR %,d", taxRate, taxes));
        totalFareText.setText(String.format(Locale.getDefault(), "Total: PKR %,d", totalFare));
    }

    private void setupBookButton() {
        bookTicketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate required fields
                if (validateBookingFields()) {
                    // Create booking
                    createBooking();
                }
            }
        });
    }

    private boolean validateBookingFields() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder("Please fill in the following: \n");

        // Check ticket type
        if (selectedTicketType.isEmpty() || selectedTicketType.equals("Select Ticket Type")) {
            errorMessage.append("- Ticket Type\n");
            isValid = false;
        }

        // Check trip type
        if (selectedTripType.isEmpty() || selectedTripType.equals("Select Trip Type")) {
            errorMessage.append("- Trip Type\n");
            isValid = false;
        }

        // Check vehicle type
        if (selectedVehicleType.isEmpty() || selectedVehicleType.equals("Select Vehicle Type")) {
            errorMessage.append("- Vehicle Type\n");
            isValid = false;
        }

        // Check seat type
        if (selectedSeatType.isEmpty() || selectedSeatType.equals("Select Seat Type")) {
            errorMessage.append("- Seat Type\n");
            isValid = false;
        }

        // Check source and destination
        if (selectedSource.isEmpty() || selectedSource.equals("Select Source")) {
            errorMessage.append("- Source\n");
            isValid = false;
        }

        if (selectedDestination.isEmpty() || selectedDestination.equals("Select Destination")) {
            errorMessage.append("- Destination\n");
            isValid = false;
        }

        // Check date
        if (selectedDate == null) {
            errorMessage.append("- Travel Date\n");
            isValid = false;
        }

        // Check schedule
        if (selectedSchedule.isEmpty() || selectedSchedule.equals("Select Schedule")) {
            errorMessage.append("- Schedule\n");
            isValid = false;
        }

        // Check seat selection
        if (selectedSeats.isEmpty()) {
            errorMessage.append("- Select at least one seat\n");
            isValid = false;
        }

        // If not valid, show error message
        if (!isValid) {
            messageText.setText(errorMessage.toString());
            messageText.setVisibility(View.VISIBLE);
        } else {
            messageText.setVisibility(View.GONE);
        }

        return isValid;
    }

    private void createBooking() {
        bookTicketButton.setEnabled(false);
        messageText.setText("Processing your booking...");
        messageText.setVisibility(View.VISIBLE);

        // Get user info
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Step 1: Get destination location code
        db.collection("locations")
                .whereEqualTo("name", selectedDestination)
                .get()
                .addOnCompleteListener(taskDest -> {
                    if (taskDest.isSuccessful() && !taskDest.getResult().isEmpty()) {
                        String destinationCode = taskDest.getResult().getDocuments().get(0).getString("code");

                        // Step 2: Get source location code
                        db.collection("locations")
                                .whereEqualTo("name", selectedSource)
                                .get()
                                .addOnCompleteListener(taskSource -> {
                                    if (taskSource.isSuccessful() && !taskSource.getResult().isEmpty()) {
                                        String sourceCode = taskSource.getResult().getDocuments().get(0).getString("code");

                                        // Generate unique bookingId
                                        String timestamp = String.valueOf(System.currentTimeMillis()); // for uniqueness
                                        String bookingId = sourceCode + "-" + destinationCode + "-" + timestamp;

                                        // Now prepare booking data
                                        Map<String, Object> bookingData = new HashMap<>();
                                        bookingData.put("bookingId", bookingId);
                                        bookingData.put("userId", userId);
                                        bookingData.put("timestamp", new Date());
                                        bookingData.put("source", selectedSource);
                                        bookingData.put("destination", selectedDestination);
                                        bookingData.put("travelDate", selectedDate);
                                        bookingData.put("schedule", selectedSchedule);
                                        bookingData.put("ticketType", selectedTicketType);
                                        bookingData.put("tripType", selectedTripType);
                                        bookingData.put("vehicleType", selectedVehicleType);
                                        bookingData.put("seatType", selectedSeatType);
                                        bookingData.put("passengers", selectedPassengers);
                                        bookingData.put("selectedSeats", selectedSeats);
                                        bookingData.put("totalFare", totalFare);
                                        bookingData.put("status", "PENDING");

                                        // Optional: special requests
                                        String specialRequestsText = specialRequests.getText().toString().trim();
                                        if (!specialRequestsText.isEmpty()) {
                                            bookingData.put("specialRequests", specialRequestsText);
                                        }

                                        // Selected options
                                        Map<String, Boolean> options = new HashMap<>();
                                        options.put("blanket", blanketSeatCheck.isChecked());
                                        options.put("windowSeat", windowSeatCheck.isChecked());
                                        options.put("meal", mealOnRideCheck.isChecked());
                                        options.put("luggage", luggageCheck.isChecked());
                                        options.put("wifi", wifiCheck.isChecked());
                                        options.put("powerOutlet", powerOutletCheck.isChecked());
                                        bookingData.put("options", options);

                                        // Save booking to Firestore
                                        db.collection("bookings")
                                                .document(bookingId)
                                                .set(bookingData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Booking created with ID: " + bookingId);
                                                    updateUserBookings(userId, bookingId);
                                                    resetForm();
                                                    navigateToBookingConfirmation(bookingId);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w(TAG, "Error creating booking", e);
                                                    messageText.setText("Failed to create booking. Please try again.");
                                                    bookTicketButton.setEnabled(true);
                                                });

                                    } else {
                                        messageText.setText("Source location not found.");
                                        bookTicketButton.setEnabled(true);
                                    }
                                });

                    } else {
                        messageText.setText("Destination location not found.");
                        bookTicketButton.setEnabled(true);
                    }
                });
    }

    private void updateUserBookings(String userId, String bookingId) {
        // Get reference to user document
        DocumentReference userRef = db.collection("Users").document(userId);

        // Update user's bookings array
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        // Get current bookings list
                        List<String> bookings = new ArrayList<>();
                        if (document.contains("bookings")) {
                            bookings = (List<String>) document.get("bookings");
                        }

                        // Add new booking
                        if (bookings == null) {
                            bookings = new ArrayList<>();
                        }
                        bookings.add(bookingId);

                        // Update user document
                        userRef.update("bookings", bookings)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "User bookings updated successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error updating user bookings", e);
                                    }
                                });
                        if(document.contains("tripsCount")) {
                            int tripsCount = ((Long) document.get("tripsCount")).intValue();
                            tripsCount++;
                            userRef.update("tripsCount", tripsCount);
                        }
                        if(document.contains("citiesCount")) {
                            int citiesCount = ((Long) document.get("citiesCount")).intValue();
                            citiesCount++;
                            userRef.update("citiesCount", citiesCount);
                        }
                        if(document.contains("pointsCount")) {
                            int pointsCount = ((Long) document.get("pointsCount")).intValue();
                            pointsCount++;
                            userRef.update("pointsCount", pointsCount);
                        }
                    } else {
                        // Create new user document
                        Map<String, Object> userData = new HashMap<>();
                        List<String> bookings = new ArrayList<>();
                        bookings.add(bookingId);
                        userData.put("bookings", bookings);

                        userRef.set(userData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "User document created with booking");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error creating user document", e);
                                    }
                                });
                    }
                } else {
                    Log.w(TAG, "Error getting user document", task.getException());
                }
            }
        });
    }

    private void resetForm() {
        // Reset spinners
        ticketTypeSpinner.setSelection(0);
        passengersSpinner.setSelection(0);
        tripTypeSpinner.setSelection(0);
        vehicleTypeSpinner.setSelection(0);
        seatTypeSpinner.setSelection(0);

        if (route == null) {
            sourceSpinner.setSelection(0);
            destSpinner.setSelection(0);
        }

        // Reset date
        selectedDate = null;
        selectedDateText.setText("Select Date");

        // Reset seat selection
        selectedSeats.clear();
        seatMapGrid.removeAllViews();
        selectedSeatsText.setText("Selected seats: None");

        // Reset checkboxes
        blanketSeatCheck.setChecked(false);
        windowSeatCheck.setChecked(false);
        mealOnRideCheck.setChecked(false);
        luggageCheck.setChecked(false);
        wifiCheck.setChecked(false);
        powerOutletCheck.setChecked(false);

        // Reset special requests
        specialRequests.setText("");

        // Reset fare display
        fareText.setText("Base Fare: KES 0");
        optionsCostText.setText("Options: KES 0");
        taxesText.setText("Taxes (10%): KES 0");
        totalFareText.setText("Total: KES 0");

        // Reset message
        messageText.setVisibility(View.GONE);

        // Reset button
        bookTicketButton.setEnabled(true);
    }

    private void navigateToBookingConfirmation(String bookingId) {
        // Create intent for booking confirmation activity
        Intent intent = new Intent(requireContext(), BookingConfirmationActivity.class);
        intent.putExtra("bookingId", bookingId);
        startActivity(intent);
    }
}