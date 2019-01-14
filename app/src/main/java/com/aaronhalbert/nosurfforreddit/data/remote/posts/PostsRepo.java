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

package com.aaronhalbert.nosurfforreddit.data.remote.posts;

import com.aaronhalbert.nosurfforreddit.data.remote.posts.model.Data_;
import com.aaronhalbert.nosurfforreddit.data.remote.posts.model.Listing;
import com.aaronhalbert.nosurfforreddit.data.remote.auth.model.UserOAuthToken;
import com.aaronhalbert.nosurfforreddit.data.remote.auth.NoSurfAuthenticator;
import com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.model.ClickedPostId;
import com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.ClickedPostIdDao;
import com.aaronhalbert.nosurfforreddit.data.local.clickedpostids.ClickedPostIdRoomDatabase;
import com.aaronhalbert.nosurfforreddit.ui.viewstate.CommentsViewState;
import com.aaronhalbert.nosurfforreddit.ui.viewstate.PostsViewState;

import java.util.List;
import java.util.concurrent.ExecutorService;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;
import retrofit2.HttpException;

public class PostsRepo {
    private static final String BEARER = "Bearer ";
    private static final String ZERO = "zero";

    private final RetrofitContentInterface ri;
    private final ClickedPostIdDao clickedPostIdDao;
    private final ExecutorService executor;
    private final NoSurfAuthenticator authenticator;
    private final PostsRepoUtils postsRepoUtils;

    public PostsRepo(RetrofitContentInterface ri,
                     ClickedPostIdRoomDatabase db,
                     ExecutorService executor,
                     NoSurfAuthenticator authenticator,
                     PostsRepoUtils postsRepoUtils) {
        this.ri = ri;
        clickedPostIdDao = db.clickedPostIdDao();
        this.executor = executor;
        this.authenticator = authenticator;
        this.postsRepoUtils = postsRepoUtils;
    }

    // region network auth calls -------------------------------------------------------------------

    //TODO: Move auth-related calls out of here and into an AuthRepo which uses TokenStore
    public Single<UserOAuthToken> fetchUserOAuthTokenASync(String code) {
        return authenticator.fetchUserOAuthTokenASync(code);
    }

    // endregion network auth calls ----------------------------------------------------------------

    // region network data calls -------------------------------------------------------------------

    /* gets posts from r/all, with either a user (logged-in) or anonymous (logged-out, aka
     * "app-only") token based on the user's login status. Works for both logged-in and logged-out
     * users. */
    public Single<PostsViewState> fetchAllPostsASync() {
        String bearerAuth = chooseAllPostsAndCommentsBearerAuth();

        /* fetches/refreshes expired auth tokens when there's a 401 error, and retries the request
         * after the token has been refreshed.
         *
         * We "react" to expired tokens dynamically with onErrorResumeNext() instead of calculating
         * the date/time of token expiration and trying to time refreshes pre-emptively. */
        return ri.fetchAllPostsASync(bearerAuth)
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof HttpException) {
                        if (((HttpException) throwable).code() == 401 && (authenticator.isUserLoggedInCache())) {
                            return authenticator.refreshExpiredUserOAuthTokenASync()
                                    .flatMap(userOAuthToken ->
                                            ri.fetchAllPostsASync(chooseAllPostsAndCommentsBearerAuth()));
                        } else if (((HttpException) throwable).code() == 401) {
                            return authenticator.fetchAppOnlyOAuthTokenASync()
                                    .flatMap(appOnlyOAuthToken ->
                                            ri.fetchAllPostsASync(chooseAllPostsAndCommentsBearerAuth()));
                        }
                    }
                    return Single.error(throwable);
                })
                .map(this::cleanRawPostsMapper);
    }

    /* gets posts from the user's subscribed subreddits; only works for logged-in users. See
     * fetchAllPostsASync() for more comments that also apply to this method. */
    public Single<PostsViewState> fetchSubscribedPostsASync() {
        if (authenticator.isUserLoggedInCache()) {
            String bearerAuth = chooseSubscribedPostsBearerAuth();

            return ri.fetchSubscribedPostsASync(bearerAuth)
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof HttpException) {
                            if (((HttpException) throwable).code() == 401) {
                                return authenticator.refreshExpiredUserOAuthTokenASync()
                                        .flatMap(userOAuthToken ->
                                                ri.fetchSubscribedPostsASync(chooseSubscribedPostsBearerAuth()));
                            }
                        }
                        return Single.error(throwable);
                    })
                    .map(this::cleanRawPostsMapper);
        }

        // do nothing if user is logged out, as subscribed posts are only for logged-in users
        return Single.just(new PostsViewState());
    }

    /* get a post's comments. This is called every time a user clicks a post, and works for both
     * logged-in and logged-out users. Works the same regardless of whether the post is a
     * link post or a self post. */
    public Single<CommentsViewState> fetchPostCommentsASync(final String id) {
        String bearerAuth = chooseAllPostsAndCommentsBearerAuth();

        return ri.fetchPostCommentsASync(bearerAuth, id)
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof HttpException) {
                        if (((HttpException) throwable).code() == 401 && (authenticator.isUserLoggedInCache())) {
                            return authenticator.refreshExpiredUserOAuthTokenASync()
                                    .flatMap(userOAuthToken ->
                                            ri.fetchPostCommentsASync(chooseAllPostsAndCommentsBearerAuth(), id));
                        } else if (((HttpException) throwable).code() == 401) {
                            return authenticator.fetchAppOnlyOAuthTokenASync()
                                    .flatMap(appOnlyOAuthToken ->
                                            ri.fetchPostCommentsASync(chooseAllPostsAndCommentsBearerAuth(), id));
                        }
                    }
                    return Single.error(throwable);
                })
                .map(this::cleanRawCommentsMapper);
    }

    // endregion network data calls ----------------------------------------------------------------

    // region viewstate Transformations ------------------------------------------------------------

    /* Cleans dirty/raw post data from the Reddit API. Prepares it to be merged with the list
     * of clicked post IDs from the Room database, before being passed to the UI. */
    private PostsViewState cleanRawPostsMapper(Listing input) {
        PostsViewState postsViewState = new PostsViewState();

        for (int i = 0; i < 25; i++) {
            PostsViewState.Post post = new PostsViewState.Post();

            Data_ data = input.getData().getChildren().get(i).getData();

            // both link posts and self posts share these attributes
            post.isSelf = data.isIsSelf();
            post.id = data.getId();
            post.title = input.decodeHtml(data.getTitle()).toString(); // some titles contain HTML special entities
            post.author = data.getAuthor();
            post.subreddit = data.getSubreddit();
            post.score = data.getScore();
            post.numComments = data.getNumComments();
            post.thumbnailUrl = input.pickThumbnailUrl(data.getThumbnail());
            post.isNsfw = data.isNsfw();
            post.permalink = data.getPermalink();

            // assign link- and self-post specific attributes
            if (post.isSelf) {
                post.selfTextHtml = input.formatSelfPostSelfTextHtml(data.getSelfTextHtml());
            } else {
                post.url = input.decodeHtml(data.getUrl()).toString();
                post.imageUrl = input.decodeHtml(input.pickImageUrl(i)).toString();
            }

            postsViewState.postData.set(i, post);
        }

        return postsViewState;
    }

    /* Cleans dirty/raw comment data from the Reddit API. */
    private CommentsViewState cleanRawCommentsMapper(List<Listing> input) {
        CommentsViewState commentsViewState;
        int autoModOffset;

        //check if there is at least 1 comment
        if (input.get(1).getNumTopLevelComments() > 0) {

            //calculate the number of valid comments after checking for & excluding AutoMod
            autoModOffset = input.get(1).calculateAutoModOffset();
            int numComments = input.get(1).getNumTopLevelComments() - autoModOffset;

            // only display first 3 top-level comments
            if (numComments > 3) numComments = 3;

            commentsViewState = new CommentsViewState(numComments, input.get(0).getCommentId());

            // construct the viewstate object
            for (int i = 0; i < numComments; i++) {
                String commentAuthor = input.get(1).getCommentAuthor(autoModOffset + i);
                int commentScore = input.get(1).getCommentScore(autoModOffset, i);

                commentsViewState.getCommentBodies()[i] = input.get(1).formatCommentBodyHtml(autoModOffset, i);
                commentsViewState.getCommentDetails()[i] = input.get(0).formatCommentDetails(commentAuthor, commentScore);
            }
        } else { //if zero comments
            commentsViewState = new CommentsViewState(0, ZERO);
        }

        return commentsViewState;
    }

    // endregion viewstate Transformations ---------------------------------------------------------

    // region helper methods -----------------------------------------------------------------------

    /* used by fetchAllPostsASync() and fetchPostCommentsASync() - they both take the same
     * auth token. */
    private String chooseAllPostsAndCommentsBearerAuth() {
        String accessToken;

        if (authenticator.isUserLoggedInCache()) {
            // use a user auth token, if one exists
            accessToken = authenticator.getUserOAuthAccessTokenCache();
        } else {
            if (!"".equals(authenticator.getAppOnlyOAuthTokenCache())) {
                // if none exists, then fall-back to a logged-out app-only auth token
                accessToken = authenticator.getAppOnlyOAuthTokenCache();
            } else {
                /* If user is logged out and there's no app only auth token in the cache,
                 * we need to fetch one. To do so, we just send a garbage value which triggers
                 * a 401 error, onErrorResumeNext(), and subsequently,
                 * fetchAppOnlyOAuthTokenASync() in our Rx chain. */

                accessToken = "xyz";
            }
        }

        return BEARER + accessToken;
    }

    /* used only by fetchSubscribedPostsASync(). Less logic is required compared to
     * chooseAllPostsAndCommentsBearerAuth(), since the user is guaranteed to be logged in
     * when this is called, and so we don't have to deal with the logged-out case. */
    private String chooseSubscribedPostsBearerAuth() {
        return BEARER + authenticator.getUserOAuthAccessTokenCache();
    }

    // endregion helper methods --------------------------------------------------------------------

    // region room methods and classes -------------------------------------------------------------

    /* we could use Rx instead of an ExecutorService to write to the Room database, but let's
     * use the ExecutorService just to demonstrate a different method of asynchronous DB access. */
    public void insertClickedPostId(ClickedPostId id) {
        Runnable runnable = () -> clickedPostIdDao.insertClickedPostId(id);

        executor.execute(runnable);
    }

    /* stream of clicked post IDs, this Observable gets merged into PostsViewState objects */
    public Observable<List<String>> fetchClickedPostIds() {
        return clickedPostIdDao
                .getAllClickedPostIds()
                .map(postsRepoUtils::convertListOfClickedPostIdsToListOfStrings);
    }

    // endregion room methods and classes ----------------------------------------------------------

    // region init/de-init methods -----------------------------------------------------------------

    public void checkIfLoginCredentialsAlreadyExist() {
        authenticator.checkIfLoginCredentialsAlreadyExist();
    }

    public void setUserLoggedOut() {
        authenticator.setUserLoggedOut();
    }

    // endregion init/de-init methods --------------------------------------------------------------

    // region getter methods -----------------------------------------------------------------------

    public BehaviorSubject<Boolean> getIsUserLoggedIn() {
        return authenticator.getIsUserLoggedIn();
    }

    // endregion getter methods --------------------------------------------------------------------
}
