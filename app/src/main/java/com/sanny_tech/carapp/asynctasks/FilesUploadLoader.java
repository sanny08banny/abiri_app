package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.entities.DeleteCar;
import com.sanny_tech.carapp.enums.UploadActions;
import com.sanny_tech.carapp.services.CarApiService;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;
import com.sanny_tech.carapp.utils.IpAddressManager;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
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

public class FilesUploadLoader extends AsyncTaskLoader<String> {
    private static final String TAG = FilesUploadLoader.class.getSimpleName();
    private static final int MAX_RETRIES = 3;

    private final Car car;
    private final Map<String, File> imageFiles;
    private String baseUrl;
    private UploadActions uploadActions;
    private TaxiInit taxiInit;
    private MultipartBody.Part idBackPart;
    private MultipartBody.Part idFrontPart;
    private List<File> files;

    public FilesUploadLoader(Context context, Car car, Map<String, File> imageFiles,
                             UploadActions uploadActions, TaxiInit taxiInit, List<File> files) {
        super(context);
        this.car = car;
        this.imageFiles = imageFiles;
        this.baseUrl = IpAddressManager.getIpAddress(context) + "/";
        this.uploadActions = uploadActions;
        this.taxiInit = taxiInit;
        this.files = files;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            attempt++;
            try {
                // Create a Retrofit instance
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(new OkHttpClient.Builder()
                                .connectTimeout(90, TimeUnit.SECONDS)
                                .readTimeout(90, TimeUnit.SECONDS)
                                .build())
                        .build();
                CarApiService carApiService = retrofit.create(CarApiService.class);

                if (uploadActions == UploadActions.UPLOAD_DOCS) {
                    prepareDocsParts();
                    Call<Void> call = carApiService.uploadDocs("taxi", taxiInit.getDriver_id(),
                            taxiInit.getTaxi_id(), idFrontPart, idBackPart);
                    Response<Void> response = call.execute();

                    if (response.isSuccessful()) {
                        logError(response);
                        return "success";
                    } else {
                        logError(response);
                        return null;
                    }
                } else if (uploadActions == UploadActions.UPLOAD_ONE_DOC) {
                    prepareSingleDocPart();
                    Call<Void> call = carApiService.uploadSingleDoc("taxi", taxiInit.getDriver_id(),
                            taxiInit.getTaxi_id(), idFrontPart);
                    Response<Void> response = call.execute();

                    if (response.isSuccessful()) {
                        return "success";
                    } else {
                        logError(response);
                        return null;
                    }
                } else if (uploadActions == UploadActions.UPLOAD_DOCS2 || uploadActions == UploadActions.UPLOAD_DOCS3) {
                    MultipartBody.Part[] parts = filesToMultipartBodyPartsDocs(imageFiles);
                    Call<Void> call = carApiService.uploadDocs("taxi", taxiInit.getDriver_id(),
                            taxiInit.getTaxi_id(), parts[0], parts[1]);
                    Response<Void> response = call.execute();

                    if (response.isSuccessful()) {
                        return "success";
                    } else {
                        logError(response);
                        return null;
                    }
                } else if (uploadActions == UploadActions.UPLOAD_IMAGES) {
                    Log.e(TAG, "API call size: " + getTotalFileSize(files));
                    String category = "";
                    String user_id = "";
                    String car_id = "";
                    if (taxiInit != null) {
                        category = "taxi";
                        user_id = taxiInit.getDriver_id();
                        car_id = taxiInit.getTaxi_id();
                    } else if (car != null) {
                        category = "car_hire";
                        user_id = car.getOwner_id();
                        car_id = car.getCar_id();
                    }
                    Call<Void> call = carApiService.uploadCarImages(category, user_id,
                            car_id, filesToMultipartBodyParts(files));
                    Response<Void> response = call.execute();

                    if (response.isSuccessful()) {
                        return "success";
                    } else {
                        logError(response);
                    }
                } else if (uploadActions == UploadActions.DELETE) {
                    DeleteCar deleteCar = new DeleteCar(car.getCar_id(), getCurrentAccountId());
                    Call<Void> call = carApiService.deleteCar(deleteCar);
                    Response<Void> response = call.execute();
                    if (response.isSuccessful()) {
                        return "success";
                    } else {
                        logError(response);
                        return "failed";
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Attempt " + attempt + " - Error making API call: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Attempt " + attempt + " - Unexpected error: " + e.getMessage(), e);
            }

            try {
                Thread.sleep(2000); // Wait before retrying
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Retry interrupted: " + e.getMessage());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("logs");
                reference.child(String.valueOf(System.currentTimeMillis())).setValue(
                        "Error: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    private void prepareDocsParts() {
        for (Map.Entry<String, File> entry : imageFiles.entrySet()) {
            String desc = entry.getKey();
            File image = entry.getValue();
            if (image != null && desc.equals("national_id_front")) {
                RequestBody idFrontRequestBody = RequestBody.create(
                        MediaType.parse("image/*"), image);
                idFrontPart = MultipartBody.Part.createFormData("national_id_front",
                        image.getName(), idFrontRequestBody);
            } else if (image != null && desc.equals("national_id_back")) {
                RequestBody idBackRequestBody = RequestBody.create(
                        MediaType.parse("image/*"), image);
                idBackPart = MultipartBody.Part.createFormData("national_id_back",
                        image.getName(), idBackRequestBody);
            }
        }
    }

    private void prepareSingleDocPart() {
        for (Map.Entry<String, File> entry : imageFiles.entrySet()) {
            String desc = entry.getKey();
            Log.e("FilesUploadSingle", desc);
            File image = entry.getValue();
            RequestBody idFrontRequestBody = RequestBody.create(
                    MediaType.parse("image/*"), image);
            idFrontPart = MultipartBody.Part.createFormData(desc,
                    image.getName(), idFrontRequestBody);
        }
    }

    private void logError(Response<Void> response) {
        Log.e(TAG, "Error: " + response.code() + " - " + response.message());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("logs");
        reference.child(String.valueOf(System.currentTimeMillis())).setValue(
                "Error: " + response.code() + " - " + response.message());
    }

    public static long getTotalFileSize(List<File> fileList) {
        long totalSize = 0;
        for (File file : fileList) {
            totalSize += file.length(); // Add the size of each file to the total
        }
        return totalSize;
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

    public static MultipartBody.Part[] filesToMultipartBodyPartsDocs(Map<String, File> files) {
        Log.e("FileUploader", "filesToMultipartBodyPartsDocs called");
        if (files == null || files.isEmpty()) {
            Log.e("FileUploader", "files map is empty or null");
            return new MultipartBody.Part[0];
        }

        MultipartBody.Part[] parts = new MultipartBody.Part[files.size()];
        int i = 0;

        for (Map.Entry<String, File> entry : files.entrySet()) {
            String desc = entry.getKey();
            Log.e("FileUploader", desc);
            File image = entry.getValue();
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), image);
            MultipartBody.Part part = MultipartBody.Part.createFormData(desc, image.getName(), requestBody);
            parts[i] = part;
            i++;
        }

        return parts;
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}

