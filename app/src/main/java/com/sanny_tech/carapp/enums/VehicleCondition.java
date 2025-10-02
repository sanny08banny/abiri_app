package com.sanny_tech.carapp.enums;

public enum VehicleCondition {
    New,
    Excellent,
    Good,
    Fair,
    Damaged;

    public static VehicleCondition getConditionFromString(String conditionName) {
        try {
            return VehicleCondition.valueOf(conditionName.trim());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null; // or a default like Good or Fair
        }
    }
}

