package com.sanny_tech.carapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sanny_tech.carapp.R;
import com.sanny_tech.carapp.adapters.SearchCarAdapter;
import com.sanny_tech.carapp.adapters.SearchHistoryAdapter;
import com.sanny_tech.carapp.asynctasks.SearchLoader;
import com.sanny_tech.carapp.databinding.ActivitySearchBinding;
import com.sanny_tech.carapp.entities.Car;
import com.sanny_tech.carapp.utils.SearchHistoryManager;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Car>>,
SearchCarAdapter.OnItemClickListener, SearchHistoryAdapter.OnItemClickListener{

    private static final int VOICE_REQUEST_CODE = 7;
    private ActivitySearchBinding searchBinding;
    private SearchHistoryAdapter searchHistoryAdapter;
    private SearchCarAdapter searchCarAdapter;
    private List<Car> cars;
    private List<String> searchHistory;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        searchBinding.expansionIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        searchHistory = SearchHistoryManager.getSearchHistory(this);
        cars = new ArrayList<>();
        searchCarAdapter = new SearchCarAdapter(cars, this);
        searchCarAdapter.setOnItemClickListener(this);
        searchHistoryAdapter = new SearchHistoryAdapter(searchHistory,this);
        searchHistoryAdapter.setOnItemClickListener(this);

        cars = new ArrayList<>();
        if (searchHistory != null) {
            searchBinding.searchResultsRecyclerView.setAdapter(searchHistoryAdapter);
        }else {
            searchBinding.searchResultsRecyclerView.setAdapter(null);
        }

        searchBinding.searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchBinding.voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        searchBinding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    query = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchBinding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (query!= null) {
                    fetchHouses(query);
                }
            }
        });
        searchBinding.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchHouses(query);
            }
        });
    }

    private void fetchHouses(String string) {
        if (string != null){
            query = string;
        }
        LoaderManager.getInstance(this).restartLoader(6,null,this);
        showProgressBar();
        hideErrorLayout();
    }
    private void showProgressBar() {
        searchBinding.customProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        searchBinding.customProgressBar.setVisibility(View.GONE);
    }
    private void showErrorLayout() {
        searchBinding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        searchBinding.errorLayout.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    public Loader<List<Car>> onCreateLoader(int id, @Nullable Bundle args) {
        return new SearchLoader(this,query);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Car>> loader, List<Car> data) {
        hideProgressBar();
        if (data != null && data.size() != 0){
            searchHistory.add(query);
            SearchHistoryManager.addQuery(this,query);
            searchCarAdapter.setItems(data);
            searchBinding.searchResultsRecyclerView.setAdapter(searchCarAdapter);
        }else {
            if (isNetworkConnected()) {
                showErrorLayout();
            }else {
                if (searchHistory != null) {
                    searchBinding.searchResultsRecyclerView.setAdapter(searchHistoryAdapter);
                }else {
                    showErrorLayout();
                }
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Car>> loader) {

    }
    private void setMargin(View view, int marginDp) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int marginPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                marginDp,
                getResources().getDisplayMetrics()
        );
        layoutParams.leftMargin = marginPx;
        layoutParams.rightMargin = marginPx;
        view.setLayoutParams(layoutParams);
    }

//    private void startVoiceRecognition() {
//        Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//
//        if (voiceIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(voiceIntent, VOICE_REQUEST_CODE);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> voiceResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (voiceResults != null && voiceResults.size() > 0) {
                String spokenText = voiceResults.get(0);
                searchBinding.searchEditText.setText(spokenText);
            }
        }
    }

    @Override
    public void onItemClick(Car item) {
        openAboutCar(item);
    }

    @Override
    public void onItemClick(String item) {
        if (item!= null) {
            fetchHouses(item);
        }
    }
    private void openAboutCar(Car car) {
        Intent intent = new Intent(SearchActivity.this, AboutCarActivity.class);
        intent.putExtra("selectedCar", car);
        startActivity(intent);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}