package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.repository.Repository;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class NoSurfWebViewFragmentViewModel extends ViewModel {
    private final Repository repository;

    NoSurfWebViewFragmentViewModel(Repository repository) {
        this.repository = repository;
    }

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return repository.getAllPostsViewStateLiveData();
    }

    public LiveData<PostsViewState> getSubscribedPostsViewStateLiveData() {
        return repository.getSubscribedPostsViewStateLiveData();
    }
}
