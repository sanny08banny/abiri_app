package com.sanny_tech.carapp.asynctasks;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.loader.content.AsyncTaskLoader;
import com.google.firebase.database.*;
import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.enums.FunActions;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.database.*;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.*;

public class FunSpaceLoader extends AsyncTaskLoader<List<FunSpace>> {

    private static final String IMGBB_API_KEY = "ffadf6ed38ede8b5176e4e4b9d500f22"; // Replace with your key
    private static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";

    private FunActions action;
    private FunSpace funSpace;
    private String filter;

    private DatabaseReference mDatabase;

    private List<File> imageFiles;

    public FunSpaceLoader(Context context, FunActions action, FunSpace funSpace,
                          List<File> imageFiles, String filter) {
        super(context);
        this.action = action;
        this.funSpace = funSpace;
        this.imageFiles = imageFiles;
        this.filter = filter;
        mDatabase = FirebaseDatabase.getInstance().getReference("fun_spaces");
    }


    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<FunSpace> loadInBackground() {
        switch (action) {
            case SAVE:
                saveFunSpaceItem(funSpace, imageFiles);
                return null;
            case DELETE:
                deleteFunSpaceItem(funSpace);
                break;
            case UPDATE:
                updateFunSpaceItem(funSpace, imageFiles);
                break;
            case FETCH_ALL:
                return fetchAllFunSpaceItems();
            case FETCH_BY_DESTINATION:
                return fetchFunSpaceItemsByDestination(filter);
            case FETCH_BY_EXPIRY:
                return fetchFunSpaceItemsByExpiry(filter);
        }
        return null;
    }

    private void saveFunSpaceItem(FunSpace funSpaceItem, List<File> imageFiles) {
        Log.d("FunSpaceSave", "Starting to save FunSpace item...");

        if (funSpaceItem.getId() == null || funSpaceItem.getId().length() == 0) {
            String itemId = mDatabase.child("funSpaceItems").push().getKey();
            funSpaceItem.setId(itemId);
            Log.d("FunSpaceSave", "Generated new ID: " + itemId);
        }

        List<String> imageUrls = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(imageFiles.size());

        for (File file : imageFiles) {
            Log.d("FunSpaceSave", "Processing file: " + file.getAbsolutePath());

            new Thread(() -> {
                try {
                    String base64Image = encodeImageToBase64(file);
                    if (base64Image == null) {
                        Log.e("FunSpaceSave", "Base64 encoding failed for: " + file.getName());
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

                    Log.d("FunSpaceSave", "Uploading to IMGBB: " + file.getName());
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        String imageUrl = json.getJSONObject("data").getString("url");
                        Log.d("FunSpaceSave", "Uploaded successfully: " + imageUrl);

                        synchronized (imageUrls) {
                            imageUrls.add(imageUrl);
                        }
                    } else {
                        Log.e("FunSpaceSave", "IMGBB upload failed [" + file.getName() + "]: " + response.message());
                        Log.e("FunSpaceSave", "Response code: " + response.code());
                        Log.e("FunSpaceSave", "Response body: " + response.body().string());
                    }
                } catch (Exception e) {
                    Log.e("FunSpaceSave", "Exception during upload: " + e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        try {
            latch.await();
            Log.d("FunSpaceSave", "All image uploads completed.");
        } catch (InterruptedException e) {
            Log.e("FunSpaceSave", "Latch interrupted: " + e.getMessage(), e);
        }

        funSpaceItem.setImages(imageUrls);
        Log.d("FunSpaceSave", "Final image URLs: " + imageUrls);

        mDatabase.child(funSpaceItem.getId()).setValue(funSpaceItem)
                .addOnSuccessListener(aVoid ->
                        Log.d("FunSpaceSave", "Saved FunSpace item to Firebase with ID: " + funSpaceItem.getId()))
                .addOnFailureListener(e ->
                        Log.e("FunSpaceSave", "Failed to save FunSpace item to Firebase: " + e.getMessage(), e));
    }

    private String encodeImageToBase64(File imageFile) {
        try {
            FileInputStream inputStream = new FileInputStream(imageFile);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();

            byte[] imageBytes = outputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void deleteFunSpaceItem(FunSpace funSpaceItem) {
        if (funSpaceItem.getId() != null) {
            // Note: IMGBB does not support deletion by URL unless you store the delete token
            // So we just remove from the database
            mDatabase.child(funSpaceItem.getId()).removeValue();
        }
    }

    private void updateFunSpaceItem(FunSpace funSpaceItem, List<File> imageFiles) {
        if (funSpaceItem.getId() != null) {
            if (imageFiles != null && !imageFiles.isEmpty()) {
                funSpaceItem.setImages(new ArrayList<>());
                saveFunSpaceItem(funSpaceItem, imageFiles);
            } else {
                mDatabase.child(funSpaceItem.getId()).setValue(funSpaceItem);
            }
        }
    }


    private List<FunSpace> fetchAllFunSpaceItems() {
        List<FunSpace> funSpaceList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FunSpace funSpace = snapshot.getValue(FunSpace.class);
                    funSpaceList.add(funSpace);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return funSpaceList;
    }

    private List<FunSpace> fetchFunSpaceItemsByDestination(String destination) {
        List<FunSpace> funSpaceList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        mDatabase.orderByChild("destination").equalTo(destination)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            FunSpace funSpace = snapshot.getValue(FunSpace.class);
                            funSpaceList.add(funSpace);
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return funSpaceList;
    }

    private List<FunSpace> fetchFunSpaceItemsByExpiry(String expiryDate) {
        List<FunSpace> funSpaceList = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        mDatabase.orderByChild("expiry_date").equalTo(expiryDate)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            FunSpace funSpace = snapshot.getValue(FunSpace.class);
                            funSpaceList.add(funSpace);
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return funSpaceList;
    }
}
