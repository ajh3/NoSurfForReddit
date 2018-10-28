package com.aaronhalbert.nosurfforreddit.fragments;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.aaronhalbert.nosurfforreddit.R;

public class NoSurfPreferenceFragment extends PreferenceFragmentCompat {

    public static NoSurfPreferenceFragment newInstance() {
        return new NoSurfPreferenceFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
