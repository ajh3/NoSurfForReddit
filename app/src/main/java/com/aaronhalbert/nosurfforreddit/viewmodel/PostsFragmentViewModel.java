package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.repository.Repository;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class PostsFragmentViewModel extends ViewModel {
    private final Repository repository;

    PostsFragmentViewModel(Repository repository) {
        this.repository = repository;
    }

    public void fetchAllPostsASync() {
        repository.fetchAllPostsASync();
    }

    public void fetchSubscribedPostsASync() {
        repository.fetchSubscribedPostsASync();
    }

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return repository.getAllPostsViewStateLiveData();
    }

    public LiveData<PostsViewState> getSubscribedPostsViewStateLiveData() {
        return repository.getSubscribedPostsViewStateLiveData();
    }
}
