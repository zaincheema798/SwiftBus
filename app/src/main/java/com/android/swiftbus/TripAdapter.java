package com.android.swiftbus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

// Trip adapter for RecyclerView
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private List<Trip> trips;
    private Context context;

    public TripAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_trip, parent, false);
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.routeTextView.setText(trip.getRoute());
        holder.dateTextView.setText(trip.getDate());
        holder.timeTextView.setText(trip.getTime());
        holder.busOperatorTextView.setText(trip.getBusOperator());
        holder.seatInfoTextView.setText(trip.getSeatInfo());
        holder.iconImageView.setImageResource(trip.getIconResId());

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            navigateToTripDetails(trip);
        });
    }

    private void navigateToTripDetails(Trip trip) {
            Intent intent = new Intent(context, Home.class);
            intent.putExtra("TRIP_ROUTE", trip.getRoute());
            intent.putExtra("TRIP_DATE", trip.getDate());
            intent.putExtra("TRIP_TIME", trip.getTime());
            intent.putExtra("TRIP_BUS_OPERATOR", trip.getBusOperator());
            intent.putExtra("TRIP_SEAT_INFO", trip.getSeatInfo());
            context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public class TripViewHolder extends RecyclerView.ViewHolder {
        public TextView routeTextView, dateTextView, timeTextView, busOperatorTextView, seatInfoTextView;
        public ImageView iconImageView;

        public TripViewHolder(View view) {
            super(view);
            routeTextView = view.findViewById(R.id.trip_route);
            dateTextView = view.findViewById(R.id.trip_date);
            timeTextView = view.findViewById(R.id.trip_time);
            busOperatorTextView = view.findViewById(R.id.trip_operator);
            seatInfoTextView = view.findViewById(R.id.trip_seat);
            iconImageView = view.findViewById(R.id.trip_icon);
        }
    }
}
