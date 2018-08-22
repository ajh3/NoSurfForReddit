package com.aaronhalbert.nosurfforreddit;

import android.arch.core.util.Function;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.LiveData;

import com.aaronhalbert.nosurfforreddit.reddit.RedditListingObject;

public class NoSurfViewModel extends ViewModel {
    private NoSurfRepository repository = NoSurfRepository.getInstance();

    private final LiveData<RedditListingObject> listing =
            Transformations.switchMap(repository.getListingLiveData(),
                    new Function<RedditListingObject, LiveData<RedditListingObject>>() {
                        @Override
                        public LiveData<RedditListingObject> apply(RedditListingObject input) {
                            final MutableLiveData<RedditListingObject> listing = new MutableLiveData<>();
                            listing.setValue(input);
                            return listing;
                        }
                    });

    public void initApp() {

        repository.requestAppOnlyOAuthToken();

    }

    public LiveData<RedditListingObject> getListing() {
        return listing;
    }

    public void requestSubRedditListing() {
        repository.requestSubRedditListing();
    }

}
