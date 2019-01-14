/*
 * Copyright (c) 2018-present, Aaron J. Halbert.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.aaronhalbert.nosurfforreddit.ui.main;

import android.util.Log;

import com.aaronhalbert.nosurfforreddit.data.remote.posts.PostsRepo;
import com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.model.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.ui.viewstate.CommentsViewState;
import com.aaronhalbert.nosurfforreddit.ui.viewstate.PostsViewState;
import com.aaronhalbert.nosurfforreddit.utils.Event;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel.NetworkErrors.FETCH_ALL_POSTS_ERROR;
import static com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel.NetworkErrors.FETCH_POST_COMMENTS_ERROR;
import static com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel.NetworkErrors.FETCH_SUBSCRIBED_POSTS_ERROR;
import static com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel.NetworkErrors.LOGIN_STATUS_CHECK_ERROR;
import static com.aaronhalbert.nosurfforreddit.ui.main.MainActivityViewModel.NetworkErrors.USER_AUTH_CALL_ERROR;
import static com.aaronhalbert.nosurfforreddit.ui.viewstate.PostsViewState.Post;

public class MainActivityViewModel extends ViewModel {
    private static final String FETCH_ALL_POSTS_CALL_FAILED = "fetchAllPostsASync call failed: ";
    private static final String FETCH_SUBSCRIBED_POSTS_CALL_FAILED = "fetchSubscribedPostsASync call failed: ";
    private static final String FETCH_POST_COMMENTS_CALL_FAILED = "fetchPostCommentsASync call failed: ";
    private static final String USER_AUTH_CALL_FAILED = "User auth call failed";
    private static final String LOGIN_STATUS_CHECK_FAILED = "Login status check failed";

    private final PostsRepo postsRepo;

    /* these PublishSubjects act as stable Observers for the ViewModel to subscribe to in order to
     * combineLatest() post data with the list of clicked post IDs from Room. We feed the results
     * of multiple, independent Single Retrofit calls into these */
    private final PublishSubject<PostsViewState> allPosts = PublishSubject.create();
    private final PublishSubject<PostsViewState> subscribedPosts = PublishSubject.create();

    /* these 3 MutableLiveData feed the UI directly */
    private final MutableLiveData<PostsViewState> allPostsViewStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<PostsViewState> subscribedPostsViewStateLiveData = new MutableLiveData<>();
    private final MutableLiveData<CommentsViewState> commentsViewStateLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isUserLoggedInLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event<NetworkErrors>> networkErrorsLiveData = new MutableLiveData<>();

    private final CompositeDisposable disposables = new CompositeDisposable();

    // TODO: pass this around as a fragment argument instead of sharing it via the ViewModel?
    private Post lastClickedPost;

    public MainActivityViewModel(PostsRepo postsRepo) {
        this.postsRepo = postsRepo;

        combinePostsWithClickedPostIds(
                allPosts,
                allPostsViewStateLiveData,
                FETCH_ALL_POSTS_ERROR,
                FETCH_ALL_POSTS_CALL_FAILED);

        combinePostsWithClickedPostIds(
                subscribedPosts,
                subscribedPostsViewStateLiveData,
                FETCH_SUBSCRIBED_POSTS_ERROR,
                FETCH_SUBSCRIBED_POSTS_CALL_FAILED);

        disposables.add(postsRepo
                .getIsUserLoggedIn()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        isUserLoggedInLiveData::setValue,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(LOGIN_STATUS_CHECK_ERROR));
                            Log.e(getClass().toString(), LOGIN_STATUS_CHECK_FAILED, throwable);
                        }));

        // initialize self
        postsRepo.checkIfLoginCredentialsAlreadyExist();
        fetchAllPostsASync();
        fetchSubscribedPostsASync();
    }

    /* NOTE: refer to PostsRepo for additional documentation on methods being called through
     * to it */

    // region login/logout -------------------------------------------------------------------------

    public void logUserIn(String code) {
        disposables.add(postsRepo
                .fetchUserOAuthTokenASync(code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            fetchAllPostsASync();
                            fetchSubscribedPostsASync();
                        },
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(USER_AUTH_CALL_ERROR));
                            Log.e(getClass().toString(), USER_AUTH_CALL_FAILED, throwable);
                        }));
    }

    /* this ViewModel and the app in general continue to function normally while user is logged out,
     * but user is limited to viewing posts and comments from r/all. All functionality related
     * to Subscribed posts is unavailable */
    void logUserOut() {
        postsRepo.setUserLoggedOut();
    }

    // endregion login/logout ----------------------------------------------------------------------

    // region event handling -----------------------------------------------------------------------

    private void setNetworkErrorsLiveData(Event<NetworkErrors> n) {
        networkErrorsLiveData.setValue(n);
    }

    @SuppressWarnings("UnusedReturnValue")
    public LiveData<Event<NetworkErrors>> getNetworkErrorsLiveData() {
        return networkErrorsLiveData;
    }

    //endregion event handling ---------------------------------------------------------------------

    // region getter/setter methods ----------------------------------------------------------------

    public LiveData<PostsViewState> getAllPostsViewStateLiveData() {
        return allPostsViewStateLiveData;
    }

    public LiveData<PostsViewState> getSubscribedPostsViewStateLiveData() {
        return subscribedPostsViewStateLiveData;
    }

    public LiveData<CommentsViewState> getCommentsViewStateLiveData() {
        return commentsViewStateLiveData;
    }

    public LiveData<Boolean> getIsUserLoggedInLiveData() {
        return isUserLoggedInLiveData;
    }

    public PostsViewState.Post getLastClickedPost() {
        return lastClickedPost;
    }

    public void setLastClickedPost(Post post) {
        this.lastClickedPost = post;
    }

    // endregion getter/setter methods -------------------------------------------------------------

    // region network calls ------------------------------------------------------------------------

    /* see PostsAdapter for an explanation of AllPosts vs. SubscribedPosts */

    public void fetchAllPostsASync() {
        disposables.add(postsRepo
                .fetchAllPostsASync()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allPosts::onNext,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(FETCH_ALL_POSTS_ERROR));
                            Log.e(getClass().toString(), FETCH_ALL_POSTS_CALL_FAILED, throwable);
                        }));
    }

    public void fetchSubscribedPostsASync() {
        disposables.add(postsRepo
                .fetchSubscribedPostsASync()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        subscribedPosts::onNext,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(FETCH_SUBSCRIBED_POSTS_ERROR));
                            Log.e(getClass().toString(), FETCH_SUBSCRIBED_POSTS_CALL_FAILED, throwable);
                        }));
    }

    public void fetchPostCommentsASync(String id) {
        disposables.add(
                postsRepo
                .fetchPostCommentsASync(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        commentsViewStateLiveData::setValue,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(FETCH_POST_COMMENTS_ERROR));
                            Log.e(getClass().toString(), FETCH_POST_COMMENTS_CALL_FAILED, throwable);
                        }));
    }

    // endregion network calls ---------------------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    /* combines the latest all/subscribed posts with the latest clicked post IDs from Room,
     * so the UI knows which posts to gray/X-out. */
    private void combinePostsWithClickedPostIds(
            PublishSubject<PostsViewState> postsSource,
            MutableLiveData<PostsViewState> postsTarget,
            NetworkErrors networkError,
            String errorMessage) {
        disposables.add(Observable.combineLatest(
                postsSource,
                postsRepo.fetchClickedPostIds(),
                mergeClickedPosts)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        postsTarget::setValue,
                        throwable -> {
                            setNetworkErrorsLiveData(new Event<>(networkError));
                            Log.e(getClass().toString(), errorMessage, throwable);
                        }));
    }

    // endregion helper methods --------------------------------------------------------------------

    // region enums --------------------------------------------------------------------------------

    public enum NetworkErrors {
        FETCH_ALL_POSTS_ERROR,
        FETCH_POST_COMMENTS_ERROR,
        FETCH_SUBSCRIBED_POSTS_ERROR,
        USER_AUTH_CALL_ERROR,
        LOGIN_STATUS_CHECK_ERROR
    }

    // endregion enums -----------------------------------------------------------------------------

    // region misc ---------------------------------------------------------------------------------

    public void insertClickedPostId(String id) {
        postsRepo.insertClickedPostId(new ClickedPostId(id));
    }

    /* used by combineLatest() to write the latest list of clicked post IDs into a PostsViewState
     * object. */
    private final BiFunction<PostsViewState, List<String>, PostsViewState> mergeClickedPosts
            = (postsViewState, ids) -> {

        for (int i = 0; i < 25; i++) {
            if (ids.contains(postsViewState.postData.get(i).id)) {
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

    // endregion misc ------------------------------------------------------------------------------
}
