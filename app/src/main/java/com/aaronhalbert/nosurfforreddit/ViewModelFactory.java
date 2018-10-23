package com.aaronhalbert.nosurfforreddit;

import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final NoSurfRepository noSurfRepository;

    public ViewModelFactory(NoSurfRepository noSurfRepository) {
        this.noSurfRepository = noSurfRepository;
    }

    // need to understand syntax here much better
    @SuppressWarnings("unchecked") // ???
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == NoSurfViewModel.class) {
            return (T) new NoSurfViewModel(noSurfRepository);
        } else {
            throw new RuntimeException("invalid ViewModel class" + modelClass);
        }
    }
}
