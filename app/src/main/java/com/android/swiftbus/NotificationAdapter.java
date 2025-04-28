package com.android.swiftbus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NAViewHolder> {
    Context context;
    List<Notification> notificationList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationAdapter.NAViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_layout, parent, false);
        return new NAViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NAViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.textNotification.setText(notification.getText());
        holder.timeNotification.setText(formatTime(notification.getTime().toDate()));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NAViewHolder extends RecyclerView.ViewHolder {
        TextView textNotification;
        TextView timeNotification;

        public NAViewHolder(@NonNull View itemView) {
            super(itemView);
            textNotification = itemView.findViewById(R.id.textNotification);
            timeNotification = itemView.findViewById(R.id.timeNotification);
        }
    }
    private String formatTime(Date date) {
        // For recent dates, show relative time
        long diffInMillis = System.currentTimeMillis() - date.getTime();
        long diffInSeconds = diffInMillis / 1000;

        if (diffInSeconds < 60) {
            return "Just now";
        } else if (diffInSeconds < 3600) {
            long minutes = diffInSeconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (diffInSeconds < 86400) {
            long hours = diffInSeconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (diffInSeconds < 604800) { // Less than a week
            long days = diffInSeconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            // For older dates, show the actual date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }
}
