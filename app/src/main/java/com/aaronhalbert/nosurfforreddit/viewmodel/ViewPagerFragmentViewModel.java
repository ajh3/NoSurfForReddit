package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.repository.Repository;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class ViewPagerFragmentViewModel extends ViewModel {
    private final Repository repository;

    ViewPagerFragmentViewModel(Repository repository) {
        this.repository = repository;
    }

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return repository.getAllPostsViewStateLiveData();
    }

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return repository.getIsUserLoggedInLiveData();
    }
}
