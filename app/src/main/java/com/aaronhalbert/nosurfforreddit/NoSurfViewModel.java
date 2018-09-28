package com.aaronhalbert.nosurfforreddit;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.aaronhalbert.nosurfforreddit.db.ReadPostId;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.List;

public class NoSurfViewModel extends AndroidViewModel {
    private static final String KEY_USER_ACCESS_REFRESH_TOKEN = "userAccessRefreshToken";

    private NoSurfRepository repository = NoSurfRepository.getInstance(getApplication());

    private final LiveData<Listing> allPostsLiveData =
            Transformations.switchMap(repository.getAllPostsLiveData(),
                    new Function<Listing, LiveData<Listing>>() {
                        @Override
                        public LiveData<Listing> apply(Listing input) {
                            final MutableLiveData<Listing> listing = new MutableLiveData<>();
                            listing.setValue(input);
                            return listing;
                        }
                    });

    private final LiveData<Listing> homePostsLiveData =
            Transformations.switchMap(repository.getHomePostsLiveData(),
                    new Function<Listing, LiveData<Listing>>() {
                        @Override
                        public LiveData<Listing> apply(Listing input) {
                            final MutableLiveData<Listing> listing = new MutableLiveData<>();
                            listing.setValue(input);
                            return listing;
                        }
                    });

    private final LiveData<List<Listing>> commentsLiveData =
            Transformations.switchMap(repository.getCommentsLiveData(),
                    new Function<List<Listing>, LiveData<List<Listing>>>() {
                        @Override
                        public LiveData<List<Listing>> apply(List<Listing> input) {
                            final MutableLiveData<List<Listing>> commentListing = new MutableLiveData<>();
                            commentListing.setValue(input);
                            return commentListing;
                        }
                    });

    private final LiveData<String> userOAuthRefreshTokenLiveData =
            Transformations.switchMap(repository.getUserOAuthRefreshTokenLiveData(),
                    new Function<String, LiveData<String>>() {
                        @Override
                        public LiveData<String> apply(String input) {
                            final MutableLiveData<String> userOAuthRefreshTokenLiveData = new MutableLiveData<>();
                            userOAuthRefreshTokenLiveData.setValue(input);
                            return userOAuthRefreshTokenLiveData;
                        }
                    });

    public NoSurfViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Listing> getAllPostsLiveData() {
        return allPostsLiveData;
    }

    public LiveData<Listing> getHomePostsLiveData() {
        return homePostsLiveData;
    }

    public SingleLiveEvent<List<Listing>> getCommentsLiveData() {
        return repository.getCommentsLiveData();
    }

    public LiveData<String> getUserOAuthRefreshTokenLiveData() {
        return userOAuthRefreshTokenLiveData;
    }

    public void initApp() {
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

    public void requestPostCommentsListing(String id) {
        repository.requestPostCommentsListing(id, isUserLoggedIn());
    }

    public void requestUserOAuthToken(String code) {
        repository.requestUserOAuthToken(code);
    }

    public void logout() {
        repository.logout();
    }

    public void insertReadPostId(String id) {
        repository.insertReadPostId(new ReadPostId(id));
    }

    public LiveData<List<ReadPostId>> getReadPostIdLiveData() {
        return repository.getReadPostIdLiveData();
    }

    public void setCommentsLiveDataToNull() {
        repository.setCommentsLiveDataToNull();
    }
}
