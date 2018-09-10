package com.aaronhalbert.nosurfforreddit;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.List;

public class NoSurfViewModel extends AndroidViewModel {
    private static final String KEY_USER_ACCESS_REFRESH_TOKEN = "userAccessRefreshToken";

    private NoSurfRepository repository = NoSurfRepository.getInstance(getApplication());
    private SharedPreferences preferences = getApplication().getSharedPreferences(getApplication().getPackageName() + "oauth", getApplication().MODE_PRIVATE);

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

    public NoSurfViewModel(@NonNull Application application) {
        super(application);
    }

    public void initApp() {
        if (isUserLoggedIn()) {
            requestAllSubredditsListing();
            requestHomeSubredditsListing();
        } else {
            repository.requestAppOnlyOAuthToken("requestAllSubredditsListing", null);
        }
    }

    public boolean isUserLoggedIn() {
        return ((preferences.getString(KEY_USER_ACCESS_REFRESH_TOKEN, null)) != null);
    }

    public LiveData<Listing> getAllPostsLiveData() {
        return allPostsLiveData;
    }

    public LiveData<Listing> getHomePostsLiveData() {
        return homePostsLiveData;
    }

    public LiveData<List<Listing>> getCommentsLiveData() {
        return commentsLiveData;
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
}
