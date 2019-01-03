package com.aaronhalbert.nosurfforreddit.ui.prefs;

import android.os.Bundle;

import com.aaronhalbert.nosurfforreddit.R;

import androidx.preference.PreferenceFragmentCompat;

public class NoSurfPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
