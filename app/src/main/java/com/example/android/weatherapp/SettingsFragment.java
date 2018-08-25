package com.example.android.weatherapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.example.android.weatherapp.data.SunshinePreferences;
import com.example.android.weatherapp.data.WeatherContract;
import com.example.android.weatherapp.sync.SunshineSyncUtils;


public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue( stringValue );
            if (prefIndex >= 0) {
                preference.setSummary( listPreference.getEntries()[prefIndex] );
            }
        } else {
            preference.setSummary( stringValue );
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource( R.xml.pref_general );

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference( i );
            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString( p.getKey(), "" );
                setPreferenceSummary( p, value );
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener( this );
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener( this );
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity;
        activity = getActivity();

        if (key.equals( getString( R.string.pref_location_key ) )) {
            SunshinePreferences.resetLocationCoordinates( activity );
            assert activity != null;
            SunshineSyncUtils.startImmediateSync( activity );
        } else if (key.equals( getString( R.string.pref_units_key ) )) {
            assert activity != null;
            activity.getContentResolver().notifyChange( WeatherContract.WeatherEntry.CONTENT_URI, null );
        }
        Preference preference = findPreference( key );
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary( preference, sharedPreferences.getString( key, "" ) );
            }
        }
    }
}
