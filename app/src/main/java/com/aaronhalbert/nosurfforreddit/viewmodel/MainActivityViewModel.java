package com.aaronhalbert.nosurfforreddit.viewmodel;

import android.util.Log;

import com.aaronhalbert.nosurfforreddit.Event;
import com.aaronhalbert.nosurfforreddit.repository.Repository;
import com.aaronhalbert.nosurfforreddit.room.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.viewstate.CommentsViewState;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel.NetworkErrors.FETCH_ALL_POSTS_ERROR;
import static com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel.NetworkErrors.FETCH_POST_COMMENTS_ERROR;
import static com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel.NetworkErrors.FETCH_SUBSCRIBED_POSTS_ERROR;

public class MainActivityViewModel extends ViewModel {
    private static final String FETCH_ALL_POSTS_CALL_FAILED = "fetchAllPostsASync call failed: ";
    private static final String FETCH_SUBSCRIBED_POSTS_CALL_FAILED = "fetchSubscribedPostsASync call failed: ";
    private static final String FETCH_POST_COMMENTS_CALL_FAILED = "fetchPostCommentsASync call failed: ";

    private final Repository repository;

    private final CompositeDisposable disposables = new CompositeDisposable();

    /* these MutableLiveData feed the UI directly. */
    private final MutableLiveData<PostsViewState> allPostsViewStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<PostsViewState> subscribedPostsViewStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<CommentsViewState> commentsViewStateLiveData = new MutableLiveData<>();

    private final MutableLiveData<Event<NetworkErrors>> networkErrorsLiveData = new MutableLiveData<>();

    // caches a few key variables from the most recently clicked/viewed post
    // TODO: pass this around as a fragment argument instead of sharing it via a ViewModel
    private LastClickedPostMetadata lastClickedPostMetadata;

    MainActivityViewModel(Repository repository) {
        this.repository = repository;

        /* combines the latest list of all/subscribed posts with the latest list of clicked post
         * IDs from Room, so the UI knows which posts to gray/X-out. */
        combinePostsWithClickedPostIds(
                repository.getAllPosts(),
                allPostsViewStateLiveData,
                FETCH_ALL_POSTS_ERROR,
                FETCH_ALL_POSTS_CALL_FAILED);

        combinePostsWithClickedPostIds(
                repository.getSubscribedPosts(),
                subscribedPostsViewStateLiveData,
                FETCH_SUBSCRIBED_POSTS_ERROR,
                FETCH_SUBSCRIBED_POSTS_CALL_FAILED);
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


    /* no setter for network errors in ViewModel; they are set in repository */

    // endregion event handling --------------------------------------------------------------------

    // region getter/setter methods ----------------------------------------------------------------

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return repository.getIsUserLoggedInLiveData();
    }



    public LastClickedPostMetadata getLastClickedPostMetadata() {
        return lastClickedPostMetadata;
    }

    public void setLastClickedPostMetadata(LastClickedPostMetadata lastClickedPostMetadata) {
        this.lastClickedPostMetadata = lastClickedPostMetadata;
    }

    // endregion getter/setter methods -------------------------------------------------------------

    // region misc ---------------------------------------------------------------------------------

    public void insertClickedPostId(String id) {
        repository.insertClickedPostId(new ClickedPostId(id));
    }

    // endregion misc ------------------------------------------------------------------------------

    public void fetchPostCommentsASync(String id) {
        disposables.add(
                repository
                .fetchPostCommentsASync(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        commentsViewStateLiveData::setValue,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(FETCH_POST_COMMENTS_ERROR));
                            Log.d(getClass().toString(), FETCH_POST_COMMENTS_CALL_FAILED, throwable);
                        }));
    }

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return allPostsViewStateLiveData;
    }

    public LiveData<PostsViewState> getSubscribedPostsViewStateLiveData() {
        return subscribedPostsViewStateLiveData;
    }

    public LiveData<CommentsViewState> getCommentsViewStateLiveData() {
        return commentsViewStateLiveData;
    }

    public void fetchAllPostsASync() {
        repository.fetchAllPostsASync();
    }

    public void fetchSubscribedPostsASync() {
        repository.fetchSubscribedPostsASync();
    }

    /* see comments on Repository.getArrayOfClickedPostIds() */
    private final BiFunction<PostsViewState, String[], PostsViewState> mergeClickedPosts
            = (postsViewState, ids) -> {
        List list = Arrays.asList(ids);

        for (int i = 0; i < 25; i++) {
            if (list.contains(postsViewState.postData.get(i).id)) {
                postsViewState.hasBeenClicked[i] = true;
            }
        }

        return postsViewState;
    };

    @Override
    protected void onCleared() {
        super.onCleared();

        disposables.clear();
    }


    // region enums --------------------------------------------------------------------------------

    public enum NetworkErrors {
        FETCH_ALL_POSTS_ERROR,
        FETCH_POST_COMMENTS_ERROR,
        FETCH_SUBSCRIBED_POSTS_ERROR,
        APP_ONLY_AUTH_CALL_ERROR,
        USER_AUTH_CALL_ERROR,
        REFRESH_AUTH_CALL_ERROR
    }

    // endregion enums -----------------------------------------------------------------------------

    // region event handling -----------------------------------------------------------------------

    private void setNetworkErrorsLiveData(Event<NetworkErrors> n) {
        networkErrorsLiveData.setValue(n);
    }

    public LiveData<Event<NetworkErrors>> getNetworkErrorsLiveData() {
        return networkErrorsLiveData;
    }

    //endregion event handling ---------------------------------------------------------------------

    private void combinePostsWithClickedPostIds(
            PublishSubject<PostsViewState> postsSource,
            MutableLiveData<PostsViewState> postsTarget,
            NetworkErrors networkError,
            String errorMessage) {
        disposables.add(Observable.combineLatest(
                postsSource,
                repository.fetchClickedPostIds(),
                mergeClickedPosts)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        postsTarget::setValue,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(networkError));
                            Log.d(getClass().toString(), errorMessage, throwable);
                        }));
    }



}
