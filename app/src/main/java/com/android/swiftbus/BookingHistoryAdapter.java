package com.android.swiftbus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.BookingViewHolder> {

    private Context context;
    private List<BookingModel> bookingsList;
    private SimpleDateFormat dateFormat;

    public BookingHistoryAdapter(Context context, List<BookingModel> bookingsList) {
        this.context = context;
        this.bookingsList = bookingsList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.booking_history_item, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel booking = bookingsList.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookingsList.size();
    }

    public class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBookingId;
        private TextView tvStatus;
        private TextView tvSource;
        private TextView tvDestination;
        private TextView tvTravelDate;
        private TextView tvSchedule;
        private TextView tvVehicleType;
        private TextView tvSeat;
        private TextView tvTotalFare;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvTravelDate = itemView.findViewById(R.id.tvTravelDate);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvSeat = itemView.findViewById(R.id.tvSeat);
            tvTotalFare = itemView.findViewById(R.id.tvTotalFare);
        }

        public void bind(BookingModel booking) {
            // Set booking ID
            tvBookingId.setText(booking.getFormattedBookingId());

            // Set and style status
            tvStatus.setText(booking.getStatus());
            setStatusBackground(tvStatus, booking.getStatus());

            // Set route information
            tvSource.setText(booking.getSource());
            tvDestination.setText(booking.getDestination());

            // Set travel date
            if (booking.getTravelDate() != null) {
                String formattedDate = dateFormat.format(booking.getTravelDate().toDate());
                tvTravelDate.setText(formattedDate);
            }

            // Set schedule (extract time if available)
            tvSchedule.setText(booking.getShortSchedule());

            // Set vehicle type
            tvVehicleType.setText(booking.getVehicleType());

            // Set seat information
            tvSeat.setText(booking.getFormattedSeats());

            // Set total fare
            tvTotalFare.setText(booking.getFormattedFare());
        }

        private void setStatusBackground(TextView textView, String status) {
            int backgroundRes;
            int textColorRes = android.R.color.white;

            switch (status.toUpperCase()) {
                case "CONFIRMED":
                    backgroundRes = R.drawable.status_confirmed_bg;
                    break;
                case "COMPLETED":
                    backgroundRes = R.drawable.status_completed_bg;
                    break;
                case "CANCELLED":
                    backgroundRes = R.drawable.status_cancelled_bg;
                    break;
                case "PENDING":
                default:
                    backgroundRes = R.drawable.status_pending_bg;
                    break;
            }

            textView.setBackgroundResource(backgroundRes);
            textView.setTextColor(ContextCompat.getColor(context, textColorRes));
        }
    }
}
