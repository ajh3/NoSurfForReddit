package com.aaronhalbert.nosurfforreddit;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.aaronhalbert.nosurfforreddit.db.ReadPostId;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.List;

public class NoSurfViewModel extends AndroidViewModel {
    private static final String KEY_USER_ACCESS_REFRESH_TOKEN = "userAccessRefreshToken";

    private NoSurfRepository repository = NoSurfRepository.getInstance(getApplication());

    public NoSurfViewModel(@NonNull Application application) {
        super(application);
    }

    private final LiveData<Listing> allPostsLiveData = repository.getAllPostsLiveData();
    private final LiveData<Listing> homePostsLiveData = repository.getHomePostsLiveData();
    private final LiveData<String> userOAuthRefreshTokenLiveData = repository.getUserOAuthRefreshTokenLiveData();
    private final SingleLiveEvent<List<Listing>> commentsSingleLiveEvent = repository.getCommentsSingleLiveEvent();

    public LiveData<Listing> getAllPostsLiveData() {
        return allPostsLiveData;
    }

    public LiveData<Listing> getHomePostsLiveData() {
        return homePostsLiveData;
    }

    public LiveData<String> getUserOAuthRefreshTokenLiveData() {
        return userOAuthRefreshTokenLiveData;
    }

    public SingleLiveEvent<List<Listing>> getCommentsSingleLiveEvent() {
        return commentsSingleLiveEvent;
    }

    void initApp() {
        repository.initializeTokensFromSharedPrefs();

        if (isUserLoggedIn()) {
            requestAllSubredditsListing();
            requestHomeSubredditsListing();
        } else {
            repository.requestAppOnlyOAuthToken("requestAllSubredditsListing", null);
        }
    }

    public boolean isUserLoggedIn() {
        String userOAuthRefreshToken = repository.getUserOAuthRefreshTokenLiveData().getValue();    // get straight from repository because the switchMap transformation seems to be asynchronous

        return ((userOAuthRefreshToken != null) && !(userOAuthRefreshToken.equals("")));
    }

    public void requestAllSubredditsListing() {
        repository.requestAllSubredditsListing(isUserLoggedIn());
    }

    public void requestHomeSubredditsListing() {
        repository.requestHomeSubredditsListing(isUserLoggedIn());
    }

    void requestPostCommentsListing(String id) {
        repository.requestPostCommentsListing(id, isUserLoggedIn());
    }

    void requestUserOAuthToken(String code) {
        repository.requestUserOAuthToken(code);
    }

    public void logout() {
        repository.logout();
    }

    void insertReadPostId(String id) {
        repository.insertReadPostId(new ReadPostId(id));
    }

    public LiveData<List<ReadPostId>> getReadPostIdLiveData() {
        return repository.getReadPostIdLiveData();
    }

    public void setCommentsLiveDataToNull() {
        repository.setCommentsLiveDataToNull();
    }
}
