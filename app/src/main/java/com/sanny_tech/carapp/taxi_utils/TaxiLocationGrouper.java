package com.sanny_tech.carapp.taxi_utils;

import com.sanny_tech.carapp.entities.TaxiLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxiLocationGrouper {

    public static Map<String, List<TaxiLocation>> groupBySeatCount(List<TaxiLocation> taxiLocations) {
        Map<String, List<TaxiLocation>> groupedLocations = new HashMap<>();

        for (TaxiLocation location : taxiLocations) {
            String category = location.getTaxiInit().getCategory();
            if (!groupedLocations.containsKey(category)) {
                groupedLocations.put(category, new ArrayList<>());
            }
            groupedLocations.get(category).add(location);
        }

        return groupedLocations;
    }
}
