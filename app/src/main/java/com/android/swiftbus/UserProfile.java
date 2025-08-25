package com.android.swiftbus;

public class UserProfile {
    private String profileImageBase64;
    private String name;
    private String membershipStatus;
    private String email;
    private String phone;
    private String gender;
    private String age;
    private String bloodGroup;
    private String address;
    private int tripsCount;
    private int citiesCount;
    private int pointsCount;

    public UserProfile() {
    }

    public UserProfile(String profileImageBase64, String name, String membershipStatus, String email, String phone,
                       String gender, String age, String bloodGroup, String address,
                       int tripsCount, int citiesCount, int pointsCount) {
        this.profileImageBase64 = profileImageBase64;
        this.name = name;
        this.membershipStatus = membershipStatus;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.age = age;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.tripsCount = tripsCount;
        this.citiesCount = citiesCount;
        this.pointsCount = pointsCount;
    }

    // Getters
    public String getProfileImageBase64() {
        return profileImageBase64;
    }

    public String getName() {
        return name;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public int getTripsCount() {
        return tripsCount;
    }

    public int getCitiesCount() {
        return citiesCount;
    }

    public int getPointsCount() {
        return pointsCount;
    }


    // Builder class
    public static class Builder {
        private String profileImageBase64 = "";
        private String name = "";
        private String membershipStatus = "";
        private String email = "";
        private String phone = "";
        private String gender = "";
        private String age = "";
        private String bloodGroup = "";
        private String address = "";
        private int tripsCount = 0;
        private int citiesCount = 0;
        private int pointsCount = 0;

        public Builder setProfileImageBase64(String profileImageBase64) {
            this.profileImageBase64 = profileImageBase64;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMembershipStatus(String membershipStatus) {
            this.membershipStatus = membershipStatus;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder setAge(String age) {
            this.age = age;
            return this;
        }

        public Builder setBloodGroup(String bloodGroup) {
            this.bloodGroup = bloodGroup;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Builder setTripsCount(int tripsCount) {
            this.tripsCount = tripsCount;
            return this;
        }

        public Builder setCitiesCount(int citiesCount) {
            this.citiesCount = citiesCount;
            return this;
        }

        public Builder setPointsCount(int pointsCount) {
            this.pointsCount = pointsCount;
            return this;
        }

    }
}