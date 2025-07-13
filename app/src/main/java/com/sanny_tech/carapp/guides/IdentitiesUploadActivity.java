package com.sanny_tech.carapp.guides;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.firestore.QuerySnapshot;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.OptionItemAdapter;
import com.sanny_tech.carapp.asynctasks.CarUploadLoader;
import com.sanny_tech.carapp.databinding.ActivityIdentitiesUploadBinding;
import com.sanny_tech.carapp.entities.OptionItem;
import com.sanny_tech.carapp.entities.TaxiCategory;
import com.sanny_tech.carapp.enums.CarActions;
import com.sanny_tech.carapp.taxi_utils.TaxiInit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog;

public class IdentitiesUploadActivity extends AppCompatActivity implements OptionItemAdapter.OnItemClickListener {
    private ActivityIdentitiesUploadBinding binding;
    private long pageCount = 0;
    private OptionItemAdapter adapter;
    private List<OptionItem> itemList;
    private OptionItem selectedOption;
    private boolean isStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_identities_upload);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchUserProgress(getCurrentAccountId());
        if (getCurrentUserName() != null){
            binding.nameEdittext.setText(getCurrentAccountUserName());
        }

        itemList = new ArrayList<>();
        itemList.add(new OptionItem("Boda Boda", "Maximum of 1 Passenger"));
        itemList.add(new OptionItem("Economy", "Maximum of 3 Passengers"));
        itemList.add(new OptionItem("Classic", "Maximum of 4 Passengers"));
        itemList.add(new OptionItem("Extra Large (XL)", "Maximum of 7 Passengers"));

        adapter = new OptionItemAdapter(itemList);
        adapter.setOnItemClickListener(this);
        binding.recyclerView.setAdapter(adapter);
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageCount++;
                setUpPage();
            }
        });
        binding.previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageCount--;
                setUpPage();
            }
        });
    }

    private void setUpPage() {
        if (pageCount == 1) {
            String name = binding.nameEdittext.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(IdentitiesUploadActivity.this,
                        "Please fill all the fields", Toast.LENGTH_SHORT).show();
                pageCount--;
            }else {
                binding.namesLt.setVisibility(View.GONE);
                binding.selectRideLt.setVisibility(View.VISIBLE);
                binding.aboutCarLt.setVisibility(View.GONE);
            }
        }else if (pageCount == 2){
            if (selectedOption != null) {
                binding.aboutCarLt.setVisibility(View.VISIBLE);
                binding.selectRideLt.setVisibility(View.GONE);
                binding.namesLt.setVisibility(View.GONE);
            } else {
                pageCount--;
                Toast.makeText(this, "No ride selected.", Toast.LENGTH_SHORT).show();
            }
        }else if (pageCount == 3) {
            binding.aboutCarLt.setVisibility(View.VISIBLE);
            binding.selectRideLt.setVisibility(View.GONE);
            binding.namesLt.setVisibility(View.GONE);
        } else if(pageCount == 4){
            String manufacturer = binding.manufacturerEdittext.getText().toString();
            String model = binding.modelEdittext.getText().toString();
            String plate = binding.plateEdittext.getText().toString();
            String defaultColor = binding.colorEdittext.getText().toString();
            if (manufacturer.isEmpty() || model.isEmpty() ||
                    plate.isEmpty() || defaultColor.isEmpty()) {
                pageCount--;
                Toast.makeText(IdentitiesUploadActivity.this,
                        "Please fill all the fields", Toast.LENGTH_SHORT).show();
//                            createAccountBinding.profileNameEditText.setError("Username cannot be empty");
            }else {
                TaxiInit taxiInit = new TaxiInit(getCurrentAccountId(),model,
                        defaultColor,manufacturer,
                        plate, TaxiCategory.getMainCategory(selectedOption.getTitle()));
                CarUploadLoader carUploadLoader = new CarUploadLoader(this,null,null,
                        CarActions.INIT,taxiInit);
                binding.progressLt.setVisibility(View.VISIBLE);
                carUploadLoader.forceLoad();
                carUploadLoader.registerListener(5, new Loader.OnLoadCompleteListener<String>() {
                    @Override
                    public void onLoadComplete(@NonNull Loader<String> loader, @Nullable String data) {
                        if (data!= null){
                            taxiInit.setTaxi_id(data);
                            taxiInit.setCategory(selectedOption.getTitle());
                            taxiInit.setTaxi_images(new ArrayList<>());
                            saveTaxiInit(taxiInit);
                            binding.progressLt.setVisibility(View.GONE);
                            updateUserProgress(getCurrentAccountId(),"taxi_init",true);
                            Intent intent = new Intent(IdentitiesUploadActivity.this,
                                    CarDetailsActivity.class);
                            intent.putExtra("init",taxiInit);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(IdentitiesUploadActivity.this, "Failed. Something went wrong. " + data, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }else if (pageCount < 0){
            finish();
        }
    }
    @Override
    public void onItemClick(OptionItem item) {
        selectedOption = item;
    }
    public void updateUserProgress(String userId, String step, boolean isCompleted) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProgressRef = database.getReference("drivers").child(userId);

        userProgressRef.child(step).setValue(isCompleted).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Handle success
                Log.d("RealtimeDatabase", "Step " + step + " " +
                        "updated successfully for user " + userId);
            } else {
                // Handle failure
                Log.w("RealtimeDatabase", "Error updating step " + step + " for user " + userId, task.getException());
            }
        });
    }
    public void fetchUserProgress(String userId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userProgressRef = database.getReference("drivers").child(userId);

        userProgressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Parse the user progress data
                    Map<String, Boolean> userProgress = new HashMap<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String step = snapshot.getKey();
                        Boolean isCompleted = snapshot.getValue(Boolean.class);
                        userProgress.put(step, isCompleted);
                    }
                    if (!userProgress.isEmpty()){
                        showDeleteConfirmationDialog(userProgress.size());
                    }
                } else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void showDeleteConfirmationDialog(int size) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Draft")
                .setMessage("Continue setting up your account.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(IdentitiesUploadActivity.this,
                                "Fetching progress", Toast.LENGTH_SHORT).show();
                        getAllTaxiInitForUser(getCurrentAccountId());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTaxiInitByDriverId(getCurrentAccountId());
                        dialog.dismiss();
                    }
                })
                .show();
    }
    public void getAllTaxiInitForUser(String userId) {
        List<TaxiInit> myInits = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("taxi_inits")
                .whereEqualTo("driver_id", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                TaxiInit taxiInit = document.toObject(TaxiInit.class);
                                myInits.add(taxiInit);
                                // Handle each TaxiInit object
                                Log.e("Firestore", "TaxiInit: " + taxiInit.toString());
                            }
                            if (myInits.get(0) != null) {
                                Intent intent = new Intent(IdentitiesUploadActivity.this,
                                        CarDetailsActivity.class);
                                intent.putExtra("init", myInits.get(0));
                                startActivity(intent);
                                finish();
                            }
                        }else {
                            Log.e("FireStore","Not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error getting documents.", e);
                    }
                });
    }
    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    public String getCurrentPassword() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserPassword", null);
    }

    public String getCurrentEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserEmail", null);
    }

    public String getCurrentAccountUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }

    public String getCurrentAccountType() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentAccountType", null);
    }
    public void saveTaxiInit(TaxiInit taxiInit) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Generate a new document reference with a unique ID
        DocumentReference documentReference = db.collection("taxi_inits").document();
        String documentId = documentReference.getId();

        // Set the document ID to the TaxiInit object
        taxiInit.setId(documentId);

        // Save the TaxiInit object to Firestore with the specified ID
        documentReference.set(taxiInit)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentId);
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding document", e);
                });
    }
    public void deleteTaxiInitByDriverId(String driverId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the collection to find documents where the driver_id matches
        db.collection("taxi_inits")
                .whereEqualTo("driver_id", driverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Get the document ID and delete the document
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "DocumentSnapshot successfully deleted with ID: " + document.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firestore", "Error deleting document", e);
                                    });
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    public String getCurrentUserName() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("currentUserName", null);
    }
}