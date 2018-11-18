package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;

import com.aaronhalbert.nosurfforreddit.R;

import androidx.preference.PreferenceFragmentCompat;

public class NoSurfPreferenceFragment extends PreferenceFragmentCompat {

    public static NoSurfPreferenceFragment newInstance() {
        return new NoSurfPreferenceFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
