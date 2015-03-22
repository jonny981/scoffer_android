package com.scoff.scoffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class PrefsHelper {

    private SharedPreferences sharedPref;
    private String defaultFilter;
    private String defaultTransportType;
    private Pair<Integer, Integer> defaultTransportSettings;
    private Pair<String, Integer> defaultFilterSettings;
    private int walkingDistance, cyclingDistance, drivingDistance;
    private SharedPreferences.Editor editor;

    public PrefsHelper(Context context) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPref.edit();
        defaultFilter = sharedPref.getString("default_filter", "All").toLowerCase();
        defaultTransportType = sharedPref.getString("default_transport", "Driving").toLowerCase();
        walkingDistance = Integer.parseInt(sharedPref.getString("walking_value", "1"));
        cyclingDistance = Integer.parseInt(sharedPref.getString("cycling_value", "15"));
        drivingDistance = Integer.parseInt(sharedPref.getString("driving_value", "30"));
        getSavedSettingsForSpinners();
    }

    public void getSavedSettingsForSpinners() {
        switch (defaultTransportType) {
            case "walking":
                defaultTransportSettings = new Pair<>(0, walkingDistance);
                break;
            case "cycling":
                defaultTransportSettings = new Pair<>(1, cyclingDistance);
                break;
            case "driving":
                defaultTransportSettings = new Pair<>(2, drivingDistance);
                break;
            default:
                defaultTransportSettings = new Pair<>(2, 50);
                break;
        }

        switch (defaultFilter) {
            case "cafe":
                defaultFilterSettings = new Pair<>("Cafe", 1);
                break;
            case "restaurant":
                defaultFilterSettings = new Pair<>("Restaurant", 2);
                break;
            case "bar":
                defaultFilterSettings = new Pair<>("Bar", 3);
                break;
            default:
                defaultFilterSettings = new Pair<>("All", 0);
                break;
        }
    }

    public String getFilterType() {
        return defaultFilterSettings.first;
    }

    public int getFilterSpinnerSetting() {
        return defaultFilterSettings.second;
    }

    public int getTransportSpinnerSetting() {
        return defaultTransportSettings.first;
    }

    public int getTransportRadiusSetting() {
        return defaultTransportSettings.second;
    }

    public int getWalkingDistance() {
        return Integer.parseInt(sharedPref.getString("walking_value", "1"));
    }

    public int getCyclingDistance() {
        return Integer.parseInt(sharedPref.getString("cycling_value", "15"));
    }

    public int getDrivingDistance() {
        return Integer.parseInt(sharedPref.getString("driving_value", "30"));
    }

    public boolean checkIfFavourite(int companyID) {
        boolean isFavourite = false;
        ArrayList<Integer> favourites = new ArrayList<>();
        Set<String> set = sharedPref.getStringSet("key", null);
        ArrayList<Integer> favesFromPrefs = new ArrayList<>();

        if ((set!=null) && (set.size() > 0)) {
            for (String retrieved : set) {
                favesFromPrefs.add(Integer.parseInt(retrieved));
            }
            favourites = favesFromPrefs;
            Collections.sort(favourites);
        }

        if (favourites.contains(companyID)) {
            isFavourite = true;
        }
        return isFavourite;
    }

    public void saveLatLong(double latitude, double longitude) {
        float latt = (float) latitude;
        float longg = (float) longitude;
        editor.putFloat("lat", latt);
        editor.putFloat("long", longg);
        editor.commit();
    }

    public double getSavedLatitude() {
        return sharedPref.getFloat("lat", 0);
    }

    public double getSavedLongitude() {
        return sharedPref.getFloat("long", 0);
    }

    public boolean useSavedLocation() {
        return sharedPref.getBoolean("saved_gps", false);
    }
}
