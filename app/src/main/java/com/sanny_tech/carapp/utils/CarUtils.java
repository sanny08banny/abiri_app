package com.sanny_tech.carapp.utils;

import com.sanny_tech.carapp.entities.Car;

import java.util.List;
import java.util.Random;

public class CarUtils {

    public static Car getRandomCar(List<Car> carList) {
        if (carList == null || carList.isEmpty()) {
            return null; // Return null if the list is empty or null
        }

        Random random = new Random();
        int randomIndex = random.nextInt(carList.size());

        return carList.get(randomIndex);
    }
}

