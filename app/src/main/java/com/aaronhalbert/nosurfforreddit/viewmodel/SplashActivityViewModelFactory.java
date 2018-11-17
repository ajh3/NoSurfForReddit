package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.network.Repository;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SplashActivityViewModelFactory implements ViewModelProvider.Factory {
    private final Repository repository;

    public SplashActivityViewModelFactory(Repository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass == SplashActivityViewModel.class) {
            return (T) new SplashActivityViewModel(repository);
        } else {
            throw new RuntimeException("invalid ViewModel class" + modelClass);
        }
    }
}
