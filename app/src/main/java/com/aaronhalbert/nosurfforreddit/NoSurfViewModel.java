package com.aaronhalbert.nosurfforreddit;

import android.arch.core.util.Function;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.LiveData;

import com.aaronhalbert.nosurfforreddit.reddit.Listing;

public class NoSurfViewModel extends ViewModel {
    private NoSurfRepository repository = NoSurfRepository.getInstance();

    private final LiveData<Listing> listing =
            Transformations.switchMap(repository.getListingLiveData(),
                    new Function<Listing, LiveData<Listing>>() {
                        @Override
                        public LiveData<Listing> apply(Listing input) {
                            final MutableLiveData<Listing> listing = new MutableLiveData<>();
                            listing.setValue(input);
                            return listing;
                        }
                    });

    public void initApp() {

        repository.requestAppOnlyOAuthToken();

    }

    public LiveData<Listing> getListing() {
        return listing;
    }

    public void requestSubRedditListing() {
        repository.requestSubRedditListing();
    }

}
