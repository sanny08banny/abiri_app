package com.sanny_tech.carapp.entities;

import java.util.HashMap;
import java.util.Map;

public class TaxiCategory {
    private static final Map<String, Integer> categorySeatsMap = new HashMap<>();
    private static final Map<String, String> normalizedCategoryMap = new HashMap<>();

    static {
        // Seat counts
        categorySeatsMap.put("Economy", 3);
        categorySeatsMap.put("Classic", 4);
        categorySeatsMap.put("XL", 7);
        categorySeatsMap.put("Xl", 7);
        categorySeatsMap.put("Extra Large (XL)", 7);
        categorySeatsMap.put("Boda Boda", 1);
        categorySeatsMap.put("BodaBoda", 1);

        // Normalized mappings
        normalizedCategoryMap.put("economy", "Economy");
        normalizedCategoryMap.put("classic", "Classic");
        normalizedCategoryMap.put("xl", "Xl");
        normalizedCategoryMap.put("extra large (xl)", "Xl");
        normalizedCategoryMap.put("boda boda", "Boda Boda");
        normalizedCategoryMap.put("bodaboda", "Boda Boda");
    }

    public static int getNumberOfSeats(String category) {
        Integer seats = categorySeatsMap.get(category);
        if (seats != null) {
            return seats;
        } else {
            return 0;
        }
    }

    public static String getMainCategory(String category) {
        if (category == null) return "Unknown";

        String normalized = category.trim().toLowerCase();
        String mainCategory = normalizedCategoryMap.get(normalized);

        return mainCategory != null ? mainCategory : "Unknown";
    }
}
