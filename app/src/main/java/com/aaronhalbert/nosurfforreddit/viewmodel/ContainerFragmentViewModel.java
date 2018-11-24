package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.repository.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class ContainerFragmentViewModel extends ViewModel {
    private final Repository repository;

    ContainerFragmentViewModel(Repository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return repository.getIsUserLoggedInLiveData();
    }
}
