package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.repository.Repository;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.viewstate.CommentsViewState;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class PostFragmentViewModel extends ViewModel {
    private final Repository repository;

    PostFragmentViewModel(Repository repository) {
        this.repository = repository;
    }

    public void fetchPostCommentsASync(String id) {
        repository.fetchPostCommentsASync(id);
    }

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return repository.getAllPostsViewStateLiveData();
    }

    public LiveData<PostsViewState> getSubscribedPostsViewStateLiveData() {
        return repository.getSubscribedPostsViewStateLiveData();
    }

    public LiveData<CommentsViewState> getCommentsViewStateLiveData() {
        return repository.getCommentsViewStateLiveData();
    }

    public void insertClickedPostId(String id) {
        repository.insertClickedPostId(new ClickedPostId(id));
    }
}
