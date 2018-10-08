package com.aaronhalbert.nosurfforreddit;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;

import com.aaronhalbert.nosurfforreddit.db.ReadPostId;
import com.aaronhalbert.nosurfforreddit.network.NoSurfRepository;
import com.aaronhalbert.nosurfforreddit.reddit.Listing;

import java.util.List;

import static android.arch.lifecycle.Transformations.map;
import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class NoSurfViewModel extends AndroidViewModel {
    private NoSurfRepository repository = NoSurfRepository.getInstance(getApplication());

    public NoSurfViewModel(@NonNull Application application) {
        super(application);
    }

    //TODO: need to turn this into a SingleLiveEvent somehow...
    //TODO: maybe turn this into a PostViewState to separate out processing logic from binding, can include all the dirty code for image edge cases
    private LiveData<CommentsViewState> commentsViewStateLiveData =
            Transformations.map(getCommentsLiveData(), new Function<List<Listing>, CommentsViewState>() {
                @Override
                public CommentsViewState apply(List<Listing> input) {
                    CommentsViewState commentsViewState = null;

                    //check if there are any comments at all
                    if (input.get(0).getData().getChildren().get(0).getData().getNumComments() > 0) {
                        int autoModOffset;

                        //skip first comment if it's by AutoMod
                        if ((input.get(1).getData().getChildren().get(0).getData().getAuthor()).equals("AutoModerator")) {
                            autoModOffset = 1;
                        } else {
                            autoModOffset = 0;
                        }

                        //calculate the number of valid comments left after excluding AutoMod
                        int numTopLevelComments = input.get(1).getData().getChildren().size();
                        numTopLevelComments = numTopLevelComments - autoModOffset; // avoid running past array in cases where numTopLevelComments < 4 and one of them is an AutoMod post
                        if (numTopLevelComments > 3) numTopLevelComments = 3;

                        commentsViewState = new CommentsViewState(numTopLevelComments);

                        //for each valid comment, get its body and double unescape it and strip trailing new lines, then get its author and score
                        for (int i = 0; i < numTopLevelComments; i++) {
                            String unescaped = getCommentBodyHtml(input, autoModOffset, i);
                            Spanned escaped = escapeHtml(unescaped);
                            Spanned trailingNewLinesStripped = (Spanned) trimTrailingWhitespace(escaped);

                            String commentAuthor = input.get(1).getData().getChildren().get(autoModOffset + i).getData().getAuthor();
                            int commentScore = input.get(1).getData().getChildren().get(autoModOffset + i).getData().getScore();
                            String commentDetails = "u/" + commentAuthor + " \u2022 " + Integer.toString(commentScore);

                            commentsViewState.commentBodies[i] = trailingNewLinesStripped;
                            commentsViewState.commentDetails[i] = commentDetails;
                        }
                    } else { //if zero comments
                        commentsViewState = new CommentsViewState(0);
                        Log.e(getClass().toString(), "zero comments");
                    }
                    return commentsViewState;
                }
            });



    public LiveData<Listing> getAllPostsLiveData() {
        return repository.getAllPostsLiveData();
    }

    public LiveData<Listing> getHomePostsLiveData() {
        return repository.getHomePostsLiveData();
    }

    public LiveData<List<Listing>> getCommentsLiveData() {
        return repository.getCommentsLiveData();
    }

    public LiveData<String> getUserOAuthRefreshTokenLiveData() {
        return repository.getUserOAuthRefreshTokenLiveData();
    }

    public LiveData<CommentsViewState> getCommentsViewStateLiveData() {
        return commentsViewStateLiveData;
    }

    public SingleLiveEvent<Boolean> getCommentsFinishedLoadingLiveEvent() {
        return repository.getCommentsFinishedLoadingLiveEvent();
    }

    void initApp() {
        repository.initializeTokensFromSharedPrefs();

        if (isUserLoggedIn()) {
            requestAllSubredditsListing();
            requestHomeSubredditsListing();
        } else {
            repository.requestAppOnlyOAuthToken("requestAllSubredditsListing", null);
        }
    }

    public boolean isUserLoggedIn() {
        String userOAuthRefreshToken = repository.getUserOAuthRefreshTokenLiveData().getValue();    // get straight from repository because the switchMap transformation seems to be asynchronous

        return ((userOAuthRefreshToken != null) && !(userOAuthRefreshToken.equals("")));
    }

    public void requestAllSubredditsListing() {
        repository.requestAllSubredditsListing(isUserLoggedIn());
    }

    public void requestHomeSubredditsListing() {
        repository.requestHomeSubredditsListing(isUserLoggedIn());
    }

    void requestPostCommentsListing(String id) {
        repository.requestPostCommentsListing(id, isUserLoggedIn());
    }

    void requestUserOAuthToken(String code) {
        repository.requestUserOAuthToken(code);
    }

    public void logout() {
        repository.logout();
    }

    void insertReadPostId(String id) {
        repository.insertReadPostId(new ReadPostId(id));
    }

    public LiveData<List<ReadPostId>> getReadPostIdLiveData() {
        return repository.getReadPostIdLiveData();
    }

    private String getCommentBodyHtml(List<Listing> input, int autoModOffset, int i) {
        String commentBodyHtml;

        if (Build.VERSION.SDK_INT >= 24) {
            commentBodyHtml = Html.fromHtml(input.get(1)
                    .getData()
                    .getChildren()
                    .get(autoModOffset + i)
                    .getData()
                    .getBodyHtml(), FROM_HTML_MODE_LEGACY).toString();
        } else {
            commentBodyHtml = Html.fromHtml(input.get(1)
                    .getData()
                    .getChildren()
                    .get(autoModOffset + i)
                    .getData()
                    .getBodyHtml()).toString();
        }
        return commentBodyHtml;
    }

    private Spanned escapeHtml(String unescaped) {
        Spanned escaped;

        if (Build.VERSION.SDK_INT >= 24) {
            escaped = Html.fromHtml(unescaped, FROM_HTML_MODE_LEGACY);
        } else {
            escaped = Html.fromHtml(unescaped);
        }

        return escaped;
    }


    private CharSequence trimTrailingWhitespace(CharSequence source) {
        if (source == null) return "";

        int i = source.length();

        //decrement i and check if that character is whitespace
        do { --i; } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        //tick i up by 1 to return the full non-whitespace sequence
        return source.subSequence(0, i+1);
    }

}
