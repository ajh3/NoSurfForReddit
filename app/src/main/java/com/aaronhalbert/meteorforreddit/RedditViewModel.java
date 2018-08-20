package com.aaronhalbert.meteorforreddit;

import android.arch.core.util.Function;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.LiveData;

public class RedditViewModel extends ViewModel {
    private Repository repository = Repository.getInstance();

    private final LiveData<String[]> titles =
            Transformations.switchMap(repository.getTitleLiveData(),
                    new Function<String[], LiveData<String[]>>() {
                        @Override
                        public LiveData<String[]> apply(String[] input) {
                            final MutableLiveData<String[]> titles = new MutableLiveData<>();
                            titles.setValue(input);
                            return titles;
                        }
                    });

    public void initApp() {

        repository.requestAppOnlyOAuthToken();

    }

    public LiveData<String[]> getTitles() {
        return titles;
    }

    public void requestSubRedditListing() {
        repository.requestSubRedditListing();
    }

}
