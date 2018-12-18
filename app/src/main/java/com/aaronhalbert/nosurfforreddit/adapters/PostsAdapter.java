package com.aaronhalbert.nosurfforreddit.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aaronhalbert.nosurfforreddit.R;
import com.aaronhalbert.nosurfforreddit.databinding.RowBinding;
import com.aaronhalbert.nosurfforreddit.fragments.PostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragmentDirections;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoUrlGlobalAction;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {

    // we only ever show the first page of posts, which is 25 by default
    // TODO: write a post processing engine so the number of posts isn't hardcoded
    private static final int ITEM_COUNT = 25;

    private final MainActivityViewModel viewModel;
    private final PostsFragment hostFragment;
    private final LiveData<PostsViewState> postsViewStateLiveData;

    /* this app has two primary screens/modes, a feed of posts from r/all (Reddit's public home
     * page, and a feed of posts from the user's subscribed subreddits (if the user is logged in).
     *
     * any field/method referring to "AllPosts" refers to the former, and any field/method
     * referring to "SubscribedPosts" refers to the latter.
     *
     * Many components, such as this adapter, are easily reused for either feed. For example,
     * all that's necessary to configure this adapter is to pass it the boolean argument
     * isSubscribedPostsAdapter in the constructor, and it sets own its data source
     * (postsViewStateLiveData) and works accordingly. */

    public PostsAdapter(MainActivityViewModel viewModel,
                        PostsFragment hostFragment,
                        boolean isSubscribedPostsAdapter) {

        this.viewModel = viewModel;
        this.hostFragment = hostFragment;

        if (isSubscribedPostsAdapter) {
            postsViewStateLiveData = viewModel.getSubscribedPostsViewStateLiveData();
        } else {
            postsViewStateLiveData = viewModel.getAllPostsViewStateLiveData();
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    @Override
    public RowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowBinding rowBinding = RowBinding
                .inflate(hostFragment.getLayoutInflater(), parent, false);

        /* follow the view hierarchy lifecycle of hostFragment instead of its fragment lifecycle.
         * If we follow the fragment lifecycle, the row.xml data binding class does not correctly
         * release its reference to its row controller (RowHolder) when PostsFragment is
         * detached upon being replace()'d. This results in a PostAdapter being leaked on each
         * RecyclerView click, which in turn leads to numerous DTO and Glide objects also
         * being leaked */
        rowBinding.setLifecycleOwner(hostFragment.getViewLifecycleOwner());

        return new RowHolder(rowBinding, parent);
    }

    @Override
    public void onBindViewHolder(RowHolder rowHolder, int position) {
        rowHolder.bindModel();
    }

    // region helper classes -----------------------------------------------------------------------

    public class RowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String VIEW_NSFW_POST = "View NSFW post";
        private static final String GO_BACK = "Go back";
        private final RowBinding rowBinding;
        private final NavController navController;

        RowHolder(RowBinding rowBinding, ViewGroup viewGroup) {
            super(rowBinding.getRoot());
            this.rowBinding = rowBinding;
            navController = Navigation.findNavController(viewGroup);
            itemView.setOnClickListener(this); // itemView is the root View of the ViewHolder
        }

        void bindModel() {
            rowBinding.setController(this);
            rowBinding.executePendingBindings();
        }

        //placed here so data binding class can access it
        public LiveData<PostsViewState> getPostsViewStateLiveData() {
            return postsViewStateLiveData;
        }

        @Override
        public void onClick(View v) {
            setLastClickedPostDatum(getAdapterPosition());
            boolean isShortcutClick = v instanceof ImageView && !(viewModel.getLastClickedPostDatum().isSelf);
            evaluateClick(v, isShortcutClick);
        }

        private void evaluateClick(View v, boolean isShortcutClick) {
            if (isNsfwFilter() && viewModel.getLastClickedPostDatum().isNsfw) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder
                        .setMessage(HtmlCompat.fromHtml(v.getContext().getString(R.string.nsfw_confirmation, viewModel.getLastClickedPostDatum().title, viewModel.getLastClickedPostDatum().subreddit), HtmlCompat.FROM_HTML_MODE_LEGACY))
                        .setPositiveButton(VIEW_NSFW_POST, (dialog, id) -> {
                            evaluateIfShortcutClick(isShortcutClick);
                            viewModel.insertClickedPostId(viewModel.getLastClickedPostDatum().id);
                        })
                        .setNegativeButton(GO_BACK, (dialog, id) -> dialog.cancel())
                        .show();
            } else {
                evaluateIfShortcutClick(isShortcutClick);
                viewModel.insertClickedPostId(viewModel.getLastClickedPostDatum().id);
            }
        }

        /* if the clicked post is a link post and the user clicked directly on the image
         * thumbnail, then shortcut to the link itself and skip showing the PostFragment */
        private void evaluateIfShortcutClick(boolean isShortcutClick) {
            if (isShortcutClick) {
                gotoUrlDirectly();
            } else {
                launchPost();
            }
        }

        private void launchPost() {
            if (viewModel.getLastClickedPostDatum().isSelf) {
                ViewPagerFragmentDirections.ClickSelfPostAction action
                        = ViewPagerFragmentDirections.clickSelfPostAction();

                navController.navigate(action);
            } else {
                ViewPagerFragmentDirections.ClickLinkPostAction action
                        = ViewPagerFragmentDirections.clickLinkPostAction();

                navController.navigate(action);
            }
        }

        private void gotoUrlDirectly() {
            String url = viewModel.getLastClickedPostDatum().url;

            GotoUrlGlobalAction action
                    = gotoUrlGlobalAction(url);

            navController.navigate(action);
        }

        public boolean isNsfwFilter() {
            return hostFragment.settingsStore.isNsfwFilter();
        }

        /* cache this information in the ViewModel, as it's used by various other components */
        private void setLastClickedPostDatum(int position) {
            viewModel.setLastClickedPostDatum(postsViewStateLiveData.getValue().postData.get(position));
        }
    }
    // endregion helper classes---------------------------------------------------------------------
}
