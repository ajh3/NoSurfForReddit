package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.Event;
import com.aaronhalbert.nosurfforreddit.network.Repository;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.webview.LaunchWebViewParams;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment.ViewPagerFragmentNavigationEvents;

public class MainActivityViewModel extends ViewModel {
    private final Repository repository;
    private final MutableLiveData<Event<Boolean>> recyclerViewClickEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<LaunchWebViewParams>> postFragmentClickEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> loginFragmentClickEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<ViewPagerFragmentNavigationEvents>> viewPagerFragmentClickEventsLiveData = new MutableLiveData<>();
    // caches a few key variables from the most recently clicked/viewed post
    private LastClickedPostMetadata lastClickedPostMetadata;

    MainActivityViewModel(Repository repository) {
        this.repository = repository;
    }

    /* NOTE: refer to Repository for documentation on all methods being called through to it */

    // region login/logout -------------------------------------------------------------------------

    public void logUserIn(String code) {
        repository.fetchUserOAuthTokenASync(code);
    }

    /* this ViewModel and the app in general continue to function normally while user is logged out,
     * but user is limited to viewing posts and comments from r/all. All functionality related
     * to Subscribed posts is disabled */
    public void logUserOut() {
        repository.setUserLoggedOut();
    }

    // endregion login/logout ----------------------------------------------------------------------

    // region event handling -----------------------------------------------------------------------

    public LiveData<Event<Repository.NetworkErrors>> getNetworkErrorsLiveData() {
        return repository.getNetworkErrorsLiveData();
    }
    // no setter for network errors in ViewModel; they are set in repository

    public LiveData<Event<Boolean>> getRecyclerViewClickEventsLiveData() {
        return recyclerViewClickEventsLiveData;
    }

    public void setRecyclerViewClickEventsLiveData(Boolean b) {
        recyclerViewClickEventsLiveData.setValue(new Event<>(b));
    }

    public LiveData<Event<LaunchWebViewParams>> getPostFragmentClickEventsLiveData() {
        return postFragmentClickEventsLiveData;
    }

    public void setPostFragmentClickEventsLiveData(LaunchWebViewParams l) {
        postFragmentClickEventsLiveData.setValue(new Event<>(l));
    }

    public LiveData<Event<Boolean>> getLoginFragmentClickEventsLiveData() {
        return loginFragmentClickEventsLiveData;
    }

    public void setLoginFragmentClickEventsLiveData(Boolean b) {
        loginFragmentClickEventsLiveData.setValue(new Event<>(b));
    }

    public LiveData<Event<ViewPagerFragmentNavigationEvents>> getViewPagerFragmentClickEventsLiveData() {
        return viewPagerFragmentClickEventsLiveData;
    }

    public void setViewPagerFragmentClickEventsLiveData(ViewPagerFragmentNavigationEvents v) {
        viewPagerFragmentClickEventsLiveData.setValue(new Event<>(v));
    }

    // endregion event handling --------------------------------------------------------------------

    // region getter/setter methods ----------------------------------------------------------------

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return repository.getIsUserLoggedInLiveData();
    }

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return repository.getAllPostsViewStateLiveData();
    }

    public LastClickedPostMetadata getLastClickedPostMetadata() {
        return lastClickedPostMetadata;
    }

    public void setLastClickedPostMetadata(LastClickedPostMetadata lastClickedPostMetadata) {
        this.lastClickedPostMetadata = lastClickedPostMetadata;
    }

    // endregion getter/setter methods -------------------------------------------------------------
}
