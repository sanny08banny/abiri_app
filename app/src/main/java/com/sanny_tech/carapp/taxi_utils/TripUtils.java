package com.sanny_tech.carapp.taxi_utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TripUtils {

    // Method to get trips for the current day, sorted from new to old, and calculate total charges
    public static DayTripsAndCharges getTripsAndTotalCharges(List<Trip> trips) {
        List<Trip> filteredTrips = new ArrayList<>();
        double totalCharges = 0.0;

        // Determine current date as the target date
        Date currentDate = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String targetDate = dateFormat.format(currentDate);

        // Sort trips by start_time (new to old)
        Collections.sort(trips, new Comparator<Trip>() {
            @Override
            public int compare(Trip trip1, Trip trip2) {
                try {
                    // Convert the start_time from Unix timestamp to Date
                    Date startTime1 = new Date(Long.parseLong(trip1.getStart_time()));
                    Date startTime2 = new Date(Long.parseLong(trip2.getStart_time()));
                    // Sort descending (new to old)
                    return startTime2.compareTo(startTime1);
                } catch (NumberFormatException e) {
                    e.printStackTrace(); // Handle number format exception as needed
                }
                return 0;
            }
        });

        // Filter trips for the targetDate (current day)
        for (Trip trip : trips) {
            Date startTime = new Date(Long.parseLong(trip.getStart_time()));
            String tripDate = dateFormat.format(startTime);
            if (tripDate.equals(targetDate)) {
                filteredTrips.add(trip);
                // Assuming charges is a String that needs to be parsed to double
                if (trip.getCharges() != null && !trip.getCharges().isEmpty()) {
                    totalCharges += Double.parseDouble(trip.getCharges());
                }
            }
        }

        return new DayTripsAndCharges(filteredTrips, totalCharges);
    }

    // Helper class to hold filtered trips and total charges for a day
    public static class DayTripsAndCharges {
        private List<Trip> trips;
        private double totalCharges;

        public DayTripsAndCharges(List<Trip> trips, double totalCharges) {
            this.trips = trips;
            this.totalCharges = totalCharges;
        }

        public List<Trip> getTrips() {
            return trips;
        }

        public double getTotalCharges() {
            return totalCharges;
        }
    }
}


