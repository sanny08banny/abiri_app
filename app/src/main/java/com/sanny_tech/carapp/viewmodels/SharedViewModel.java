package com.sanny_tech.carapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> fabClicked = new MutableLiveData<>();

    public void onFabClicked() {
        fabClicked.setValue(true);
    }

    public LiveData<Boolean> isFabClicked() {
        return fabClicked;
    }

    public void resetFabClick() {
        fabClicked.setValue(false);
    }
}

