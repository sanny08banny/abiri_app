package com.sanny_tech.carapp.enums;

public enum VehicleCategory {
    Motorbike, Lite, Standard, Comfort, Plus;

    public static double getPassengerCapacity(String categoryName) {
        try {
            VehicleCategory category = VehicleCategory.valueOf(categoryName);
            switch (category) {
                case Motorbike: return 1.0;
                case Lite: return 3.0;
                case Standard:
                case Comfort: return 4.0;
                case Plus: return 6.0;
                default: return 0.0;
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            return 0.0; // Safe fallback for invalid input
        }
    }

}

