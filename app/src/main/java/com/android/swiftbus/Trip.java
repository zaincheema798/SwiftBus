package com.android.swiftbus;

public class Trip {
    private String route;
    private String date;
    private String time;
    private String busOperator;
    private String seatInfo;
    private int iconResId;

    public Trip(String route, String date, String time, String busOperator, String seatInfo, int iconResId) {
        this.route = route;
        this.date = date;
        this.time = time;
        this.busOperator = busOperator;
        this.seatInfo = seatInfo;
        this.iconResId = iconResId;
    }

    // Getters
    public String getRoute() { return route; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getBusOperator() { return busOperator; }
    public String getSeatInfo() { return seatInfo; }
    public int getIconResId() { return iconResId; }
}

