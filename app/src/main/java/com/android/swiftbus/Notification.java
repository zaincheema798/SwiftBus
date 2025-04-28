package com.android.swiftbus;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Notification {
    String text;
    String tag;
    Timestamp time;
    public Notification() {}
    public Notification(String text, String tag, Timestamp time) {
        this.tag = tag;
        this.text = text;
        this.time = time;
    }
    public String getText() {
        return text;
    }
    public String getTag() {
        return tag;
    }
    public Timestamp getTime() {
        return time;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public void setTime(Timestamp time) {
        this.time = time;
    }
}
