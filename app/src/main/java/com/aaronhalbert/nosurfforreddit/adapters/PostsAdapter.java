package com.aaronhalbert.nosurfforreddit.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aaronhalbert.nosurfforreddit.databinding.RowBinding;
import com.aaronhalbert.nosurfforreddit.fragments.PostsFragment;
import com.aaronhalbert.nosurfforreddit.fragments.ViewPagerFragmentDirections;
import com.aaronhalbert.nosurfforreddit.viewmodel.MainActivityViewModel;
import com.aaronhalbert.nosurfforreddit.viewmodel.PostsFragmentViewModel;
import com.aaronhalbert.nosurfforreddit.viewstate.LastClickedPostMetadata;
import com.aaronhalbert.nosurfforreddit.viewstate.PostsViewState;

import androidx.lifecycle.LiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.GotoUrlGlobalAction;
import static com.aaronhalbert.nosurfforreddit.NavGraphDirections.gotoUrlGlobalAction;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.RowHolder> {

    // we only ever show the first page of posts, which is 25 by default
    private static final int ITEM_COUNT = 25;

    private final MainActivityViewModel mainActivityViewModel;
    private final PostsFragment hostFragment;
    private final LiveData<PostsViewState> postsViewStateLiveData;

    /* this app has two primary screens/modes, a feed of posts from r/all (Reddit's public home
     * page, and a feed of posts from the user's subscribed subreddits (if the user is logged in).
     *
     * any field/method referring to "AllPosts" refers to the former, and any field/method
     * referring to "SubscribedPosts" refers to the latter.
     *        Log.e(getClass().toString(), "PostsFragment view destroyed");
     * Many components, such as this adapter, are easily reused for either feed. For example,
     * all that's necessary to configure this adapter is to pass it the boolean argument
     * isSubscribedPostsAdapter in the constructor, and it sets own its data source
     * (postsViewStateLiveData) and functions accordingly. */
    private final boolean isSubscribedPostsAdapter;

    public PostsAdapter(PostsFragmentViewModel viewModel,
                        MainActivityViewModel mainActivityViewModel,
                        PostsFragment hostFragment,
                        boolean isSubscribedPostsAdapter) {

        this.mainActivityViewModel = mainActivityViewModel;
        this.hostFragment = hostFragment;
        this.isSubscribedPostsAdapter = isSubscribedPostsAdapter;

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

        return new RowHolder(rowBinding);
    }

    @Override
    public void onBindViewHolder(RowHolder rowHolder, int position) {
        rowHolder.bindModel();
    }

    // region helper classes -----------------------------------------------------------------------

    public class RowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RowBinding rowBinding;

        RowHolder(RowBinding rowBinding) {
            super(rowBinding.getRoot());
            this.rowBinding = rowBinding;
            itemView.setOnClickListener(this);
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
            int position = getAdapterPosition();
            NavController navController = Navigation.findNavController(v);

            setLastClickedPostMetadata(position);
            mainActivityViewModel.insertClickedPostId(postsViewStateLiveData.getValue().postData.get(position).id);

            /* if the clicked post is a link post and the user clicked directly on the image
             * thumbnail, then shortcut to the link itself and skip showing the PostFragment */
            if (v instanceof ImageView && !(postsViewStateLiveData.getValue().postData.get(position).isSelf)) {
                gotoUrlDirectly(navController);
            } else {
                launchPost(position, navController);
            }
        }

        /* cache this information in the ViewModel, as it's used by various other components */
        private void setLastClickedPostMetadata(int position) {
            mainActivityViewModel.setLastClickedPostMetadata(new LastClickedPostMetadata(
                    position,
                    postsViewStateLiveData.getValue().postData.get(position).id,
                    postsViewStateLiveData.getValue().postData.get(position).isSelf,
                    postsViewStateLiveData.getValue().postData.get(position).url,
                    isSubscribedPostsAdapter));
        }

        private void launchPost(int position, NavController navController) {
            if (postsViewStateLiveData.getValue().postData.get(position).isSelf) {
                ViewPagerFragmentDirections.ClickSelfPostAction action
                        = ViewPagerFragmentDirections.clickSelfPostAction();

                navController.navigate(action);
            } else {
                ViewPagerFragmentDirections.ClickLinkPostAction action
                        = ViewPagerFragmentDirections.clickLinkPostAction();

                navController.navigate(action);
            }
        }

        private void gotoUrlDirectly(NavController navController) {
            String url = mainActivityViewModel.getLastClickedPostMetadata().lastClickedPostUrl;

            GotoUrlGlobalAction action
                    = gotoUrlGlobalAction(url);

            navController.navigate(action);
        }
    }
    // endregion helper classes---------------------------------------------------------------------
}
