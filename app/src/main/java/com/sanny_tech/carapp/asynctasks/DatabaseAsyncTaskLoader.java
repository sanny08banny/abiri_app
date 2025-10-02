package com.sanny_tech.carapp.asynctasks;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sanny_tech.carapp.enums.DatabaseAction;
import com.sanny_tech.carapp.fun_utils.SpaceDest;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DatabaseAsyncTaskLoader extends AsyncTaskLoader<Boolean> {

    private final DatabaseReference databaseReference;
    private final DatabaseAction action;
    private final SpaceDest item;
    private final List<File> imageFiles;

    private static final String IMGBB_API_KEY = "ffadf6ed38ede8b5176e4e4b9d500f22";
    private static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";

    public DatabaseAsyncTaskLoader(@NonNull Context context, DatabaseAction action, SpaceDest item, @Nullable List<File> imageFiles) {
        super(context);
        this.databaseReference = FirebaseDatabase.getInstance().getReference("destinations");
        this.action = action;
        this.item = item;
        this.imageFiles = imageFiles;
    }

    @Nullable
    @Override
    public Boolean loadInBackground() {
        try {
            return switch (action) {
                case SAVE -> saveItem();
                case UPDATE -> updateItem();
                case DELETE -> deleteItem();
                default -> false;
            };
        } catch (Exception e) {
            Log.e("DatabaseAsyncTaskLoader", "Error: ", e);
            return false;
        }
    }

    private Boolean saveItem() {
        String itemId = databaseReference.push().getKey();
        item.setId(itemId);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            return uploadImagesToIMGBBAndSave(itemId);
        } else {
            databaseReference.child(itemId).setValue(item);
            return true;
        }
    }

    private Boolean updateItem() {
        String itemId = item.getId();
        if (itemId == null || itemId.isEmpty()) return false;

        if (imageFiles != null && !imageFiles.isEmpty()) {
            return uploadImagesToIMGBBAndSave(itemId);
        } else {
            databaseReference.child(itemId).setValue(item);
            return true;
        }
    }

    private Boolean uploadImagesToIMGBBAndSave(String itemId) {
        List<String> imageUrls = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(imageFiles.size());

        for (File file : imageFiles) {
            new Thread(() -> {
                try {
                    if (!file.exists()) {
                        Log.e("IMGBB", "File not found: " + file.getAbsolutePath());
                        latch.countDown();
                        return;
                    }

                    String base64Image = encodeImageToBase64(file);
                    if (base64Image == null) {
                        latch.countDown();
                        return;
                    }

                    OkHttpClient client = new OkHttpClient();
                    RequestBody formBody = new FormBody.Builder()
                            .add("key", IMGBB_API_KEY)
                            .add("image", base64Image)
                            .build();

                    Request request = new Request.Builder()
                            .url(IMGBB_UPLOAD_URL)
                            .post(formBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        String imageUrl = json.getJSONObject("data").getString("url");
                        synchronized (imageUrls) {
                            imageUrls.add(imageUrl);
                        }
                    } else {
                        Log.e("IMGBB", "Upload failed: " + response.message());
                    }
                } catch (Exception e) {
                    Log.e("IMGBB", "Upload exception", e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Log.e("IMGBB", "Latch interrupted", e);
            return false;
        }

        item.setImages_urls(imageUrls);
        databaseReference.child(itemId).setValue(item);
        return true;
    }

    private Boolean deleteItem() {
        String itemId = item.getId();
        if (itemId == null || itemId.isEmpty()) return false;
        databaseReference.child(itemId).removeValue();
        return true;
    }

    private String encodeImageToBase64(File imageFile) {
        try {
            byte[] bytes = readFileToByteArray(imageFile);
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IOException e) {
            Log.e("EncodeBase64", "Failed to encode file", e);
            return null;
        }
    }
    private byte[] readFileToByteArray(File file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        fis.close();
        return baos.toByteArray();
    }


    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}


