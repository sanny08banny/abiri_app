package com.sanny_tech.carapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.FunSpacesAdapter;
import com.sanny_tech.carapp.databinding.ActivityMySpacesBinding;
import com.sanny_tech.carapp.entities.FunSpace;
import com.sanny_tech.carapp.utils.AdminManager;

import java.util.ArrayList;
import java.util.List;

public class MySpacesActivity extends AppCompatActivity implements AdminManager.AdminStatusCallback {
    private ActivityMySpacesBinding spacesBinding;
    private FunSpacesAdapter funSpacesAdapter;
    private List<FunSpace> funSpaces;
    private DatabaseReference reference;
    private FirebaseDatabase database;
    private AdminManager adminManager;
    private boolean isMainAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spacesBinding = DataBindingUtil.setContentView(this,R.layout.activity_my_spaces);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        adminManager = new AdminManager();
        adminManager.getAdminAccess(getCurrentAccountId(), this);

        spacesBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("fun_spaces");

        funSpaces = new ArrayList<>();
        funSpacesAdapter = new FunSpacesAdapter(this,funSpaces);
        spacesBinding.carsRecycler.setAdapter(funSpacesAdapter);
        spacesBinding.carsRecycler.setLayoutManager(new LinearLayoutManager(this));
        getFunSpaces();

        spacesBinding.roundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MySpacesActivity.this, CreateFunSpaceActivity.class);
                startActivity(intent);
            }
        });
        spacesBinding.retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFunSpaces();
            }
        });
        spacesBinding.verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adminManager.requestAdminAccess(getCurrentAccountId());
                Toast.makeText(MySpacesActivity.this,
                        "Successful",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public void getFunSpaces() {
        showProgressBar();
        // This method retrieves all available taxis nearby within a certain distance

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<FunSpace> funSpaceList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FunSpace funSpace = snapshot.getValue(FunSpace.class);
                    if (funSpace.getOwner_id().equals(getCurrentAccountId())) {
                        funSpaceList.add(funSpace);
                    }
                }
                if (!funSpaceList.isEmpty()) {
                    hideErrorLayout();
                    hideProgressBar();
                    funSpacesAdapter.setItems(funSpaceList);
                }else {
                    hideProgressBar();
                    showErrorLayout();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }
    private void showProgressBar() {
        spacesBinding.progressLt.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        spacesBinding.progressLt.setVisibility(View.GONE);
    }
    private void showErrorLayout() {
        spacesBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        spacesBinding.errorLayout.setVisibility(View.GONE);
    }

    public String getCurrentAccountId() {
        SharedPreferences sharedPreferences = getSharedPreferences("AccountPrefs",
                MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }

    /**
     * @param isAdmin
     */
    @Override
    public void onAdminStatusChecked(boolean isAdmin) {
        isMainAdmin = isAdmin;
        if (!isMainAdmin){
            spacesBinding.mainLt.setVisibility(View.GONE);
            spacesBinding.guideLt.setVisibility(View.VISIBLE);
        }
    }
}