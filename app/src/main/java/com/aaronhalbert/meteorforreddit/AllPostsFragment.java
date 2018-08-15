package com.aaronhalbert.meteorforreddit;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;


import java.util.Arrays;
import java.util.Objects;


public class AllPostsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RedditViewModel viewModel;

    private RecyclerView rv = null;


    public AllPostsFragment() {
        // Required empty public constructor
    }

    public static AllPostsFragment newInstance(String param1, String param2) {
        AllPostsFragment fragment = new AllPostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        viewModel = ViewModelProviders.of(getActivity()).get(RedditViewModel.class);

        viewModel.getTitles().observe(this, new Observer<String[]>() {

            @Override
            public void onChanged(@Nullable String[] strings) {
                Log.d(getClass().toString(), "Adapter gotten in onChanged() is " + rv.getAdapter().toString());
                //rv.getAdapter().setMTitleArray(strings);
                rv.getAdapter().notifyDataSetChanged();


            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(com.aaronhalbert.meteorforreddit.R.layout.fragment_all_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        rv = Objects.requireNonNull(getView()).findViewById(com.aaronhalbert.meteorforreddit.R.id.all_posts_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new PostsAdapter());
        rv.setHasFixedSize(true);
        Log.d(getClass().toString(), "Adapter set in onViewCreated() is " + rv.getAdapter().toString());

    }


}
