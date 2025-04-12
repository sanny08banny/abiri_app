package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sanny_tech.carapp.databasehelpers.UploadedCarsHelper;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.CarRequest;
import com.sanny_tech.carapp.entities.DeleteCar;
import com.sanny_tech.carapp.entities.NewBookingRequest;
import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.services.CarApiService;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.taxi_utils.TaxiInitRequest;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CarUploadLoader extends AsyncTaskLoader<String> {
    private static final String TAG = CarUploadLoader.class.getSimpleName();

    private final Car car;
    private final List<File> imageFiles;
    private String baseUrl;
    private CarActions carActions;
    private TaxiInit taxiInit;

    public CarUploadLoader(Context context, Car car, List<File> imageFiles,
                           CarActions carActions, TaxiInit taxiInit) {
        super(context);
        this.car = car;
        this.imageFiles = imageFiles;
        this.baseUrl = IpAddressManager.getIpAddress(context) + "/";
        this.carActions = carActions;
        this.taxiInit = taxiInit;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                       String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

// Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            // Create a Retrofit instance
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
            CarApiService carApiService = retrofit.create(CarApiService.class);

            if (carActions == CarActions.UPLOAD) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("logs");
                CarRequest carRequest = new CarRequest("car_hire",car.getOwner_id(), car.getCar_id(), car.getModel(), car.getLocation(),
                        car.getDescription(), String.valueOf(car.getDaily_amount()),
                        String.valueOf(car.getDaily_downpayment_amt()));
                MultipartBody.Part[] parts = filesToMultipartBodyParts(imageFiles);
                // Make the API call to create a house
                Log.d("parts", String.valueOf(parts.length));
                Call<Void> call = carApiService.newCar(carRequest.getCategory(),carRequest.getAdmin_id(), carRequest.getCar_id(),
                        carRequest.getModel(), carRequest.getLocation(), carRequest.getDescription(),
                        car.getDaily_amount(), car.getDaily_downpayment_amt(),parts);

                // Execute the call and get the response
                Response<Void> response = call.execute();

                if (response.isSuccessful()) {
                    // API call was successful
//                    UploadedCarsHelper uploadedCarsHelper = new UploadedCarsHelper(getContext());
//                    uploadedCarsHelper.insertCar(car);
                    reference.child("car_uploader").setValue("API call failed with HTTP status code: " + response.code() +
                            response.message());
                    return "success";
                } else {
                    // API call failed
                    Log.e(TAG, "API call failed with HTTP status code: " + response.code());
                    reference.child("car_uploader").setValue("API call failed with HTTP status code: " + response.code() +
                            response.message());
                    return null;
                }
            } else if(carActions == CarActions.INIT) {
                if (taxiInit.getCategory().equals("Boda Boda")){
                    taxiInit.setCategory("BodaBoda");
                } else if (taxiInit.getCategory().equals("Extra Large (XL)")) {
                    taxiInit.setCategory("Xl");
                }
                TaxiInitRequest initRequest = new TaxiInitRequest(taxiInit.getDriver_id(),
                        taxiInit.getModel(),taxiInit.getColor(),taxiInit.getManufacturer(),taxiInit.getPlate_number(),
                        taxiInit.getCategory());
                Call<String> call = carApiService.initCar(initRequest);
                // Execute the call and get the response
                Response<String> response = call.execute();

                if (response.isSuccessful()) {
                    String rawJson = response.body();
                    Log.d(TAG, "Raw JSON response: " + rawJson);
                    return response.body();
                } else {
                    // API call failed
                    Log.e(TAG, "API call failed with HTTP status code: " + response.code()
                    + response.message());
                    return null;
                }
            }else if (carActions == CarActions.DELETE)  {
                DeleteCar deleteCar = new DeleteCar(car.getCar_id(), getCurrentAccountId());
                Call<Void> call = carApiService.deleteCar(deleteCar);
                Response<Void> response = call.execute();
                if (response.isSuccessful()) {
                    // Handle the successful response for booking
                    return "success";
                } else {
                    Log.e(TAG, "Error: " + response.code() + " - " + response.message());
                    // Handle the error response for booking
                    return "failed";
                }
            }
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            Log.e(TAG, "Error making API call: " + e.getMessage());
            return null;
        }
        return null;
    }
    public static MultipartBody.Part[] filesToMultipartBodyParts(List<File> files) {
        MultipartBody.Part[] parts = new MultipartBody.Part[files.size()];

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file" + i, file.getName(), requestFile);
            parts[i] = part;
        }

        return parts;
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}


