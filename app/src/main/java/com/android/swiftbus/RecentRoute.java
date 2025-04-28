package com.android.swiftbus;

import java.io.Serializable;

public class RecentRoute implements Serializable {
    private String title;
    private String image;
    private String source;
    private String destination;
    private String distance;
    private String time;
    private String price;

    public RecentRoute() {
    }

    public RecentRoute(String title, String image, String source, String destination, String distance, String time, String price) {
        this.title = title;
        this.image = image;
        this.distance = distance;
        this.source = source;
        this.destination = destination;
        this.time = time;
        this.price = price;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }

    public String getDistance() {
        return distance;
    }

    public String getPrice() {
        return price;
    }

    public String getTime() {
        return time;
    }
}
