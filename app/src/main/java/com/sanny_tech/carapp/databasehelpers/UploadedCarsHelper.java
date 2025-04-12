package com.sanny_tech.carapp.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.sanny_tech.carapp.entities.Car;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadedCarsHelper extends SQLiteOpenHelper {
    // Define database properties
    private static final String DATABASE_NAME = "CarDatabase";
    private static final int DATABASE_VERSION = 1;

    // Define table name
    private static final String TABLE_NAME = "uploaded_cars";

    // Define column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MODEL = "model";
    private static final String COLUMN_CAR_ID = "car_id";
    private static final String COLUMN_OWNER_ID = "owner_id";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_AVAILABLE = "available";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_DOWNPAYMENT = "downpayment";
    private static final String COLUMN_IMAGES = "car_images";

    // Create table SQL statement
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_MODEL + " TEXT, "
            + COLUMN_CAR_ID + " TEXT, "
            + COLUMN_OWNER_ID + " TEXT, "
            + COLUMN_LOCATION + " TEXT, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_AVAILABLE + " TEXT, "
            + COLUMN_AMOUNT + " REAL, "
            + COLUMN_DOWNPAYMENT + " REAL, "
            + COLUMN_IMAGES + " TEXT"
            + ");";

    public UploadedCarsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This method is called when the database needs to be upgraded. You can implement it if needed.
    }

    // Method to insert a car into the database
    public long insertCar(Car car) {
        if (getCarById(car.getCar_id()) == null) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_MODEL, car.getModel());
            values.put(COLUMN_CAR_ID, car.getCar_id());
            values.put(COLUMN_OWNER_ID, car.getOwner_id());
            values.put(COLUMN_LOCATION, car.getLocation());
            values.put(COLUMN_DESCRIPTION, car.getDescription());
            values.put(COLUMN_AVAILABLE, car.getAvailable());
            values.put(COLUMN_AMOUNT, car.getDaily_amount());
            values.put(COLUMN_DOWNPAYMENT, car.getDaily_downpayment_amt());
            values.put(COLUMN_IMAGES, TextUtils.join(",", car.getCar_images()));

            long id = db.insert(TABLE_NAME, null, values);
            db.close();
            saveCarToFirestore(car);

            return id;
        }else {
            return (long) updateCar(car);
        }
    }

    // Method to retrieve all cars from the database
    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Car car = new Car();
                    car.setModel(cursor.getString(1));  // Use integer positions
                    car.setCar_id(cursor.getString(2));
                    car.setOwner_id(cursor.getString(3));
                    car.setLocation(cursor.getString(4));
                    car.setDescription(cursor.getString(5));
                    car.setAvailable(cursor.getString(6));

                    car.setDaily_amount(cursor.getDouble(7));
                    car.setDaily_downpayment_amt(cursor.getDouble(8));
                    String imageString = cursor.getString(9);
                    String[] images = imageString.split(",");
                    car.setCar_images(new ArrayList<>(Arrays.asList(images)));

                    cars.add(car);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return cars;
    }

    // Method to retrieve a car by its ID
    public Car getCarById(String carId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Car car = null;

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_CAR_ID + " = ?", new String[]{carId}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                car = new Car();
                car.setModel(cursor.getString(1));
                car.setCar_id(cursor.getString(2));
                car.setOwner_id(cursor.getString(3));
                car.setLocation(cursor.getString(4));
                car.setDescription(cursor.getString(5));
                car.setAvailable(cursor.getString(6));

                car.setDaily_amount(cursor.getDouble(7));
                car.setDaily_downpayment_amt(cursor.getDouble(8));

                String imageString = cursor.getString(9);
                String[] images = imageString.split(",");
                car.setCar_images(new ArrayList<>(Arrays.asList(images)));
            }
            cursor.close();
        }
        db.close();
        return car;
    }

    // Method to update a car's information
    public int updateCar(Car car) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MODEL, car.getModel());
        values.put(COLUMN_OWNER_ID, car.getOwner_id());
        values.put(COLUMN_LOCATION, car.getLocation());
        values.put(COLUMN_DESCRIPTION, car.getDescription());
        values.put(COLUMN_AVAILABLE, car.getAvailable());
        values.put(COLUMN_AMOUNT, car.getDaily_amount());
        values.put(COLUMN_DOWNPAYMENT, car.getDaily_downpayment_amt());
        values.put(COLUMN_IMAGES, TextUtils.join(",", car.getCar_images()));

        int rowsUpdated = db.update(TABLE_NAME, values, COLUMN_CAR_ID + " = ?", new String[]{car.getCar_id()});
        db.close();

        return rowsUpdated;
    }

    // Method to delete a car by its ID
    public void deleteCar(String carId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_CAR_ID + " = ?", new String[]{carId});
        db.close();
        deleteCarFromFirestore(carId);
    }
    public void saveCarToFirestore(Car car) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> carMap = new HashMap<>();
        carMap.put("model", car.getModel());
        carMap.put("car_id", car.getCar_id());
        carMap.put("owner_id", car.getOwner_id());
        carMap.put("location", car.getLocation());
        carMap.put("description", car.getDescription());
        carMap.put("available", car.getAvailable());
        carMap.put("amount", car.getDaily_amount());
        carMap.put("downpayment", car.getDaily_downpayment_amt());
        carMap.put("car_images", car.getCar_images()); // Assuming getCar_images() returns a List<String>

        db.collection("uploaded_cars").document(car.getCar_id())
                .set(carMap)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
    }
    public void deleteCarFromFirestore(String carId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("uploaded_cars").document(carId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error deleting document", e));
    }

}

