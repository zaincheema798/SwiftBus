// LocationData.java - Updated to handle coordinates as HashMap
package com.android.swiftbus;

import java.util.HashMap;
import java.util.Map;

public class LocationData {
    private String code;
    private String address;
    private String name;
    private Map<String, Double> coordinates;

    // Required empty constructor for Firestore
    public LocationData() {
        coordinates = new HashMap<>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Map<String, Double> coordinates) {
        this.coordinates = coordinates;
    }

    public double getLatitude() {
        if (coordinates != null && coordinates.containsKey("latitude")) {
            return coordinates.get("latitude");
        }
        return 0;
    }

    public double getLongitude() {
        if (coordinates != null && coordinates.containsKey("longitude")) {
            return coordinates.get("longitude");
        }
        return 0;
    }
}