package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.repository.Repository;

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
        } else if (modelClass == SplashActivityViewModel.class) {
            return (T) new SplashActivityViewModel(repository);
        }  else if (modelClass == PostFragmentViewModel.class) {
            return (T) new PostFragmentViewModel(repository);
        } else if (modelClass == PostsFragmentViewModel.class) {
            return (T) new PostsFragmentViewModel(repository);
        } else if (modelClass == ViewPagerFragmentViewModel.class) {
            return (T) new ViewPagerFragmentViewModel(repository);
        } else if (modelClass == NoSurfWebViewFragmentViewModel.class) {
            return (T) new NoSurfWebViewFragmentViewModel(repository);
        } else {
            throw new RuntimeException("invalid ViewModel class" + modelClass);
        }
    }
}
