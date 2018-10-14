package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.aaronhalbert.nosurfforreddit.R;

public class NoSurfPreferenceFragment extends PreferenceFragmentCompat {

    public NoSurfPreferenceFragment() { }

    public static NoSurfPreferenceFragment newInstance() {
        NoSurfPreferenceFragment fragment = new NoSurfPreferenceFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
