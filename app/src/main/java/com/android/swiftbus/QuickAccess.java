package com.android.swiftbus;

public class QuickAccess {
    String title;
    int image;
    public QuickAccess() {
    }
    public QuickAccess(String title, int image) {
        this.title = title;
        this.image = image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImage() {
        return image;
    }
    public String getTitle() {
        return title;
    }
}
