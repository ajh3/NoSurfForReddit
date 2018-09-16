package com.aaronhalbert.nosurfforreddit.fragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaronhalbert.nosurfforreddit.NoSurfViewModel;
import com.aaronhalbert.nosurfforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContainerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContainerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG_SUBSCRIBED_POSTS_FRAGMENT = "subscribedPostsFragment";
    private static final String TAG_LOGIN_FRAGMENT = "loginFragment";

    private NoSurfViewModel viewModel;

    public ContainerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContainerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContainerFragment newInstance(String param1, String param2) {
        ContainerFragment fragment = new ContainerFragment();
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

        viewModel = ViewModelProviders.of(this).get(NoSurfViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_container, container, false);

        boolean isUserLoggedIn = viewModel.isUserLoggedIn();
        setContainerChildFragment(isUserLoggedIn);

        viewModel.getUserOAuthRefreshTokenLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                boolean isUserLoggedIn = (s != null) && !(s.equals(""));
                setContainerChildFragment(isUserLoggedIn);
            }
        });

        return v;
    }

    public void setContainerChildFragment(boolean isUserLoggedIn) {
        FragmentManager fm = getChildFragmentManager();
        Fragment loginFragment = fm.findFragmentByTag(TAG_LOGIN_FRAGMENT);
        Fragment subscribedPostsFragment = fm.findFragmentByTag(TAG_SUBSCRIBED_POSTS_FRAGMENT);

        if (isUserLoggedIn) {
            if (loginFragment != null) {
                if (subscribedPostsFragment != null) { // if both subscribedPostsFragment and loginFragment exist
                    fm.beginTransaction()
                            .remove(loginFragment)
                            .commit();
                } else { // if only loginFragment exists
                    fm.beginTransaction()
                            .remove(loginFragment)
                            .add(R.id.container_fragment_base_view, SubscribedPostsFragment.newInstance("a", "b"), TAG_SUBSCRIBED_POSTS_FRAGMENT)
                            .commit();
                }
            } else {
                if (subscribedPostsFragment == null) { // if neither subscribedPostsFragment nor loginFragment exist
                    fm.beginTransaction()
                            .add(R.id.container_fragment_base_view, SubscribedPostsFragment.newInstance("a", "b"), TAG_SUBSCRIBED_POSTS_FRAGMENT)
                            .commit();
                } else { // if only subscribedPostsFragment exists
                    //do nothing
                }
            }
        } else { // if not logged in
            if (loginFragment != null) {
                if (subscribedPostsFragment != null) { // if both subscribedPostsFragment and loginFragment exist
                    fm.beginTransaction()
                            .remove(subscribedPostsFragment)
                            .commit();
                } else { // if only loginFragment exists
                    //do nothing
                }
            } else {
                if (subscribedPostsFragment == null) { // if neither subscribedPostsFragment nor loginFragment exist
                    fm.beginTransaction()
                            .add(R.id.container_fragment_base_view, LoginFragment.newInstance("a", "b"), TAG_LOGIN_FRAGMENT)
                            .commit();
                } else { // if only subscribedPostsFragment exists
                    fm.beginTransaction()
                            .remove(subscribedPostsFragment)
                            .add(R.id.container_fragment_base_view, LoginFragment.newInstance("a", "b"), TAG_LOGIN_FRAGMENT)
                            .commit();
                }
            }
        }
    }
}
