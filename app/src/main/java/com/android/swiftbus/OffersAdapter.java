package com.android.swiftbus;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.OfferViewHolder> {
    private List<OfferModel> offersList;
    private OnOfferClickListener onOfferClickListener;

    public interface OnOfferClickListener {
        void onOfferClick(OfferModel offer);
    }

    public OffersAdapter(List<OfferModel> offersList, OnOfferClickListener listener) {
        this.offersList = offersList;
        this.onOfferClickListener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        OfferModel offer = offersList.get(position);
        holder.bind(offer);
    }

    @Override
    public int getItemCount() {
        return offersList.size();
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView offerIcon;
        private TextView titleText;
        private TextView descriptionText;
        private TextView discountText;
        private TextView validityText;
        private TextView offerCodeText;
        private TextView termsText;
        private Button claimButton;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_offer);
            offerIcon = itemView.findViewById(R.id.image_offer_icon);
            titleText = itemView.findViewById(R.id.text_offer_title);
            descriptionText = itemView.findViewById(R.id.text_offer_description);
            discountText = itemView.findViewById(R.id.text_discount);
            validityText = itemView.findViewById(R.id.text_validity);
            offerCodeText = itemView.findViewById(R.id.text_offer_code);
            termsText = itemView.findViewById(R.id.text_terms);
            claimButton = itemView.findViewById(R.id.button_claim_offer);
        }

        public void bind(OfferModel offer) {
            titleText.setText(offer.getTitle());
            descriptionText.setText(offer.getDescription());

            // Set discount text
            String discountStr;
            if ("PERCENTAGE".equals(offer.getDiscountType())) {
                discountStr = offer.getDiscountPercentage() + "% OFF";
                if (offer.getMaxDiscountAmount() > 0) {
                    discountStr += "\nUp to PKR " + offer.getMaxDiscountAmount();
                }
            } else {
                discountStr = "PKR " + offer.getDiscountAmount() + " OFF";
            }
            discountText.setText(discountStr);

            // Set offer code
            offerCodeText.setText("Code: " + offer.getOfferCode());

            // Set validity
            if (offer.getValidUntil() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                validityText.setText("Valid until: " + dateFormat.format(offer.getValidUntil()));
            } else {
                validityText.setText("Limited time offer");
            }

            // Set terms based on offer type
            String terms = getTermsText(offer);
            termsText.setText(terms);

            // Set offer icon based on type
            setOfferIcon(offer);

            // Set card color based on offer type
            setCardColor(offer);

            // Set click listener for claim button
            claimButton.setOnClickListener(v -> {
                if (onOfferClickListener != null) {
                    onOfferClickListener.onOfferClick(offer);
                }
            });

            // Set click listener for entire card
            cardView.setOnClickListener(v -> {
                if (onOfferClickListener != null) {
                    onOfferClickListener.onOfferClick(offer);
                }
            });
        }

        private String getTermsText(OfferModel offer) {
            StringBuilder terms = new StringBuilder();

            if (offer.getMinimumFare() > 0) {
                terms.append("Minimum booking: PKR ").append(offer.getMinimumFare()).append(" • ");
            }

            if (offer.getMinimumPassengers() > 0) {
                terms.append("Min ").append(offer.getMinimumPassengers()).append(" passengers • ");
            }

            if (offer.getAdvanceBookingDays() > 0) {
                terms.append("Book ").append(offer.getAdvanceBookingDays()).append(" days in advance • ");
            }

            if (offer.getSourceCity() != null && offer.getDestinationCity() != null) {
                terms.append(offer.getSourceCity()).append(" to ").append(offer.getDestinationCity()).append(" • ");
            }

            if (offer.getApplicableSeatType() != null) {
                terms.append(offer.getApplicableSeatType()).append(" seats only • ");
            }

            if (offer.getApplicableVehicleType() != null) {
                terms.append(offer.getApplicableVehicleType()).append(" vehicles only • ");
            }

            // Remove trailing " • "
            String result = terms.toString();
            if (result.endsWith(" • ")) {
                result = result.substring(0, result.length() - 3);
            }

            if (result.isEmpty()) {
                result = "Terms and conditions apply";
            }

            return result;
        }

        private void setOfferIcon(OfferModel offer) {
            int iconResource;

            switch (offer.getOfferType()) {
                case "ROUTE_SPECIFIC":
                    iconResource = R.drawable.ic_route; // You need to add this icon
                    break;
                case "FIRST_TIME":
                    iconResource = R.drawable.ic_star; // You need to add this icon
                    break;
                case "GROUP_BOOKING":
                    iconResource = R.drawable.ic_group; // You need to add this icon
                    break;
                case "EARLY_BOOKING":
                    iconResource = R.drawable.ic_clock; // You need to add this icon
                    break;
                case "SEAT_TYPE":
                    iconResource = R.drawable.ic_seat; // You need to add this icon
                    break;
                case "VEHICLE_TYPE":
                    iconResource = R.drawable.ic_bus; // You need to add this icon
                    break;
                default:
                    iconResource = R.drawable.ic_offer; // You need to add this icon
                    break;
            }

            // Try to set the icon, fallback to a default if not found
            try {
                offerIcon.setImageResource(iconResource);
            } catch (Exception e) {
                // Use default icon or hide the image view
                offerIcon.setVisibility(View.GONE);
            }
        }

        private void setCardColor(OfferModel offer) {
            int cardColor;

            switch (offer.getOfferType()) {
                case "ROUTE_SPECIFIC":
                    cardColor = Color.parseColor("#E3F2FD"); // Light blue
                    break;
                case "FIRST_TIME":
                    cardColor = Color.parseColor("#FFF3E0"); // Light orange
                    break;
                case "GROUP_BOOKING":
                    cardColor = Color.parseColor("#E8F5E8"); // Light green
                    break;
                case "EARLY_BOOKING":
                    cardColor = Color.parseColor("#F3E5F5"); // Light purple
                    break;
                case "SEAT_TYPE":
                    cardColor = Color.parseColor("#FFEBEE"); // Light red
                    break;
                case "VEHICLE_TYPE":
                    cardColor = Color.parseColor("#E0F2F1"); // Light teal
                    break;
                default:
                    cardColor = Color.parseColor("#F5F5F5"); // Light gray
                    break;
            }

            cardView.setCardBackgroundColor(cardColor);
        }
    }
}