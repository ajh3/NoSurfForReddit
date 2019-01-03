package com.aaronhalbert.nosurfforreddit.utils;

import com.aaronhalbert.nosurfforreddit.data.repository.network.Repository;
import com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Repository repository;

    public ViewModelFactory(Repository repository) {
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass == MainActivityViewModel.class) {
            return (T) new MainActivityViewModel(repository);
        } else {
            throw new RuntimeException("invalid ViewModel class" + modelClass);
        }
    }
}
