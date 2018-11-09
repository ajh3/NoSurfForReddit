package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final NoSurfRepository noSurfRepository;

    public ViewModelFactory(NoSurfRepository noSurfRepository) {
        this.noSurfRepository = noSurfRepository;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass == NoSurfViewModel.class) {
            return (T) new NoSurfViewModel(noSurfRepository);
        } else {
            throw new RuntimeException("invalid ViewModel class" + modelClass);
        }
    }
}
