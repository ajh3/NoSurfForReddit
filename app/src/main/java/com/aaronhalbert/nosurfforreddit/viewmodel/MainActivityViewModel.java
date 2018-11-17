package com.aaronhalbert.nosurfforreddit.viewmodel;

import com.aaronhalbert.nosurfforreddit.Event;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragment;
import com.aaronhalbert.nosurfforreddit.network.Repository;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.viewstate.CommentsViewState;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.webview.LaunchWebViewParams;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    private final Repository repository;

    private final MutableLiveData<Event<Boolean>> recyclerViewClickEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<LaunchWebViewParams>> postFragmentClickEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> loginFragmentClickEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<ViewPagerFragment.ViewPagerFragmentNavigationEvents>> viewPagerFragmentClickEventsLiveData = new MutableLiveData<>();

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

    // region network data calls -------------------------------------------------------------------

    public void fetchAllPostsASync() {
        repository.fetchAllPostsASync();
    }

    public void fetchSubscribedPostsASync() {
        repository.fetchSubscribedPostsASync();
    }

    public void fetchPostCommentsASync(String id) {
        repository.fetchPostCommentsASync(id);
    }

    // endregion network data calls ----------------------------------------------------------------

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

    public LiveData<Event<ViewPagerFragment.ViewPagerFragmentNavigationEvents>> getViewPagerFragmentClickEventsLiveData() {
        return viewPagerFragmentClickEventsLiveData;
    }

    public void setViewPagerFragmentClickEventsLiveData(ViewPagerFragment.ViewPagerFragmentNavigationEvents v) {
        viewPagerFragmentClickEventsLiveData.setValue(new Event<>(v));
    }

    // endregion event handling --------------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return repository.getAllPostsViewStateLiveData();
    }

    public LiveData<PostsViewState> getSubscribedPostsViewStateLiveData() {
        return repository.getSubscribedPostsViewStateLiveData();
    }

    public LiveData<CommentsViewState> getCommentsViewStateLiveData() {
        return repository.getCommentsViewStateLiveData();
    }

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return repository.getIsUserLoggedInLiveData();
    }

    public LastClickedPostMetadata getLastClickedPostMetadata() {
        return lastClickedPostMetadata;
    }

    // endregion getter methods --------------------------------------------------------------------

    // region setter methods -----------------------------------------------------------------------

    public void setLastClickedPostMetadata(LastClickedPostMetadata lastClickedPostMetadata) {
        this.lastClickedPostMetadata = lastClickedPostMetadata;
    }

    // endregion setter methods --------------------------------------------------------------------

    // region room methods and classes -------------------------------------------------------------

    public void insertClickedPostId(String id) {
        repository.insertClickedPostId(new ClickedPostId(id));
    }

    // endregion room methods and classes ----------------------------------------------------------
}
