package com.sanny_tech.carapp.taxi_utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripAnalyzer {
    public List<String> getTopThreeVisitedPlaces(List<Trip> trips) {
        Map<String, Integer> placeCountMap = new HashMap<>();

        for (Trip trip : trips) {
            String pickUp = trip.getPick_up();
            String destination = trip.getDestination();

            Integer pickUpCount = placeCountMap.get(pickUp);
            if (pickUpCount != null) {
                placeCountMap.put(pickUp, pickUpCount + 1);
            } else {
                placeCountMap.put(pickUp, 1);
            }

            // Increment count for destination
            Integer destinationCount = placeCountMap.get(destination);
            if (destinationCount != null) {
                placeCountMap.put(destination, destinationCount + 1);
            } else {
                placeCountMap.put(destination, 1);
            }
        }
        List<Map.Entry<String, Integer>> sortedPlaces = new ArrayList<>(placeCountMap.entrySet());
        Collections.sort(sortedPlaces, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Get the top three visited places
        List<String> topThreeVisitedPlaces = new ArrayList<>();
        for (int i = 0; i < Math.min(2, sortedPlaces.size()); i++) {
            topThreeVisitedPlaces.add(sortedPlaces.get(i).getKey());
        }


        return topThreeVisitedPlaces;
    }
}

