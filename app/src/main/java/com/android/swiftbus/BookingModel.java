package com.android.swiftbus;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class BookingModel {
    private String documentId;
    private String bookingId;
    private String destination;
    private String source;
    private String status;
    private String schedule;
    private String seatType;
    private String ticketType;
    private String tripType;
    private String userId;
    private String vehicleType;
    private int passengers;
    private double totalFare;
    private Timestamp timestamp;
    private Timestamp travelDate;
    private List<String> selectedSeats;
    private Map<String, Object> options;

    // Default constructor required for Firestore
    public BookingModel() {}

    // Constructor with parameters
    public BookingModel(String bookingId, String destination, String source, String status,
                        String schedule, String seatType, String ticketType, String tripType,
                        String userId, String vehicleType, int passengers, double totalFare,
                        Timestamp timestamp, Timestamp travelDate, List<String> selectedSeats,
                        Map<String, Object> options) {
        this.bookingId = bookingId;
        this.destination = destination;
        this.source = source;
        this.status = status;
        this.schedule = schedule;
        this.seatType = seatType;
        this.ticketType = ticketType;
        this.tripType = tripType;
        this.userId = userId;
        this.vehicleType = vehicleType;
        this.passengers = passengers;
        this.totalFare = totalFare;
        this.timestamp = timestamp;
        this.travelDate = travelDate;
        this.selectedSeats = selectedSeats;
        this.options = options;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getTripType() {
        return tripType;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getPassengers() {
        return passengers;
    }

    public void setPassengers(int passengers) {
        this.passengers = passengers;
    }

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(Timestamp travelDate) {
        this.travelDate = travelDate;
    }

    public List<String> getSelectedSeats() {
        return selectedSeats;
    }

    public void setSelectedSeats(List<String> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    // Helper methods
    public String getFormattedBookingId() {
        if (bookingId != null && bookingId.length() > 8) {
            return "#" + bookingId.substring(0, 8);
        }
        return bookingId != null ? "#" + bookingId : "";
    }

    public String getFormattedSeats() {
        if (selectedSeats != null && !selectedSeats.isEmpty()) {
            String seats = String.join(", ", selectedSeats);
            return seats + " (" + ticketType + ")";
        }
        return ticketType != null ? ticketType : "";
    }

    public String getFormattedFare() {
        return String.format("PKR %,.0f", totalFare);
    }

    public String getShortSchedule() {
        if (schedule != null && schedule.contains("(")) {
            int start = schedule.indexOf("(");
            int end = schedule.indexOf(")", start);
            if (end > start) {
                return schedule.substring(start + 1, end);
            }
        }
        return schedule;
    }
}