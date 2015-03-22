package com.scoff.scoffer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
//        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, true);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    updatePreference(preferenceGroup.getPreference(j));
                }
            } else {
                updatePreference(preference);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key));
    }

    private void updatePreference(Preference preference) {
        if ((preference != null) && (preference instanceof ListPreference)) {
            ListPreference listPreference = (ListPreference) preference;
            Log.e(preference.toString(), "\t\t" + listPreference.getValue());
            listPreference.setSummary(listPreference.getValue().toUpperCase());
        }

        if ((preference != null) && (preference instanceof NumberPreference)) {
            NumberPreference numberPreference = (NumberPreference) preference;
            Log.e(preference.toString(), "\t\t" + numberPreference.getEntry());
            String miles = numberPreference.getEntry() == 1 ? " MILE" : " MILES";
            numberPreference.setSummary(String.valueOf(numberPreference.getEntry()) + miles);
        }
    }
}