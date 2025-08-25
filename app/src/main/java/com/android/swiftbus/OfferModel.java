package com.android.swiftbus;

import java.io.Serializable;
import java.util.Date;

public class OfferModel implements Serializable {
    private String offerId;
    private String title;
    private String description;
    private String offerCode;
    private String discountType; // "PERCENTAGE" or "FIXED"
    private int discountPercentage;
    private int discountAmount;
    private int maxDiscountAmount;
    private String offerType; // "ROUTE_SPECIFIC", "FIRST_TIME", "GROUP_BOOKING", "EARLY_BOOKING", "SEAT_TYPE", "VEHICLE_TYPE", "GENERAL"
    private int minimumFare;
    private int minimumPassengers;
    private int advanceBookingDays;
    private String sourceCity;
    private String destinationCity;
    private String applicableSeatType;
    private String applicableVehicleType;
    private Date validFrom;
    private Date validUntil;
    private boolean isActive;
    private String termsAndConditions;
    private String imageUrl;

    // Default constructor (required for Firestore)
    public OfferModel() {
    }

    // Constructor with essential fields
    public OfferModel(String offerId, String title, String description, String offerCode,
                      String discountType, String offerType) {
        this.offerId = offerId;
        this.title = title;
        this.description = description;
        this.offerCode = offerCode;
        this.discountType = discountType;
        this.offerType = offerType;
        this.isActive = true;
    }

    // Getters and Setters
    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(int maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public String getOfferType() {
        return offerType;
    }

    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    public int getMinimumFare() {
        return minimumFare;
    }

    public void setMinimumFare(int minimumFare) {
        this.minimumFare = minimumFare;
    }

    public int getMinimumPassengers() {
        return minimumPassengers;
    }

    public void setMinimumPassengers(int minimumPassengers) {
        this.minimumPassengers = minimumPassengers;
    }

    public int getAdvanceBookingDays() {
        return advanceBookingDays;
    }

    public void setAdvanceBookingDays(int advanceBookingDays) {
        this.advanceBookingDays = advanceBookingDays;
    }

    public String getSourceCity() {
        return sourceCity;
    }

    public void setSourceCity(String sourceCity) {
        this.sourceCity = sourceCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getApplicableSeatType() {
        return applicableSeatType;
    }

    public void setApplicableSeatType(String applicableSeatType) {
        this.applicableSeatType = applicableSeatType;
    }

    public String getApplicableVehicleType() {
        return applicableVehicleType;
    }

    public void setApplicableVehicleType(String applicableVehicleType) {
        this.applicableVehicleType = applicableVehicleType;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Utility methods
    public boolean isValidOffer() {
        if (!isActive) return false;

        Date currentDate = new Date();
        if (validFrom != null && currentDate.before(validFrom)) return false;
        if (validUntil != null && currentDate.after(validUntil)) return false;

        return true;
    }

    public int calculateDiscount(int baseFare) {
        if (!isValidOffer()) return 0;

        int discount = 0;

        if ("PERCENTAGE".equals(discountType)) {
            discount = (baseFare * discountPercentage) / 100;
            if (maxDiscountAmount > 0 && discount > maxDiscountAmount) {
                discount = maxDiscountAmount;
            }
        } else if ("FIXED".equals(discountType)) {
            discount = discountAmount;
        }

        // Ensure discount doesn't exceed base fare
        if (discount > baseFare) {
            discount = baseFare;
        }

        return discount;
    }

    @Override
    public String toString() {
        return "OfferModel{" +
                "offerId='" + offerId + '\'' +
                ", title='" + title + '\'' +
                ", offerCode='" + offerCode + '\'' +
                ", discountType='" + discountType + '\'' +
                ", discountPercentage=" + discountPercentage +
                ", discountAmount=" + discountAmount +
                '}';
    }
}