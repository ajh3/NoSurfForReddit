package com.aaronhalbert.meteorforreddit;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.LiveData;

public class RedditViewModel extends ViewModel {
    private Repository repository = Repository.getInstance();
    private LiveData<String[]> titleCache;

    public void initApp() {
        repository.requestAppOnlyOAuthToken();
        updateTitleCache();
    }

    public void updateTitleCache() {
        final MutableLiveData<String[]> titles = new MutableLiveData<>();

        titles.setValue(repository.getTitles());

        titleCache = titles;

    }

    public LiveData<String[]> getTitles() {
        return titleCache;
    }

}
