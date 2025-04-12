package com.sanny_tech.carapp.taxi_utils;

public class FareCalculatorLocal {
    // Constants
    private static final double BASE_FARE_BODA = 50; // Base fare in Ksh for Boda Boda
    private static final double RATE_PER_KM_BODA = 30; // Rate per kilometer in Ksh for Boda Boda

    private static final double BASE_FARE_ECONOMY = 80; // Base fare in Ksh for Economy
    private static final double RATE_PER_KM_ECONOMY = 35; // Rate per kilometer in Ksh for Economy

    private static final double BASE_FARE_CLASSIC = 100; // Base fare in Ksh for Classic
    private static final double RATE_PER_KM_CLASSIC = 40; // Rate per kilometer in Ksh for Classic

    private static final double BASE_FARE_XL = 150; // Base fare in Ksh for XL
    private static final double RATE_PER_KM_XL = 50; // Rate per kilometer in Ksh for XL

    private static final double BASE_FARE_VIP = 200; // Base fare in Ksh for VIP
    private static final double RATE_PER_KM_VIP = 70; // Rate per kilometer in Ksh for VIP

    // Method to calculate fare
    public static int calculateFare(double distance, String category) {
        double fare = 0;

        switch (category.toLowerCase()) {
            case "boda boda":
                fare = BASE_FARE_BODA + (distance * RATE_PER_KM_BODA);
                break;
            case "economy":
                fare = BASE_FARE_ECONOMY + (distance * RATE_PER_KM_ECONOMY);
                break;
            case "classic":
                fare = BASE_FARE_CLASSIC + (distance * RATE_PER_KM_CLASSIC);
                break;
            case "extra large (xl)":
                fare = BASE_FARE_XL + (distance * RATE_PER_KM_XL);
                break;
            case "vip":
                fare = BASE_FARE_VIP + (distance * RATE_PER_KM_VIP);
                break;
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }

        return (int) Math.round(fare);
    }
}

