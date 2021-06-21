package com.hty.notes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity  implements SharedPreferences.OnSharedPreferenceChangeListener {

    EditTextPreference ETP_landscape_column_number, ETP_portrait_column_number;
    SharedPreferences SP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        ETP_landscape_column_number = (EditTextPreference) findPreference("landscape_columns");
        ETP_portrait_column_number = (EditTextPreference) findPreference("portrait_columns");
        SP = getPreferenceScreen().getSharedPreferences();
        SP.registerOnSharedPreferenceChangeListener(this);
    }

    // 启动时显示
    @Override
    protected void onResume() {
        super.onResume();
        ETP_landscape_column_number.setSummary(SP.getString("landscape_columns", "1"));
        ETP_portrait_column_number.setSummary(SP.getString("portrait_columns", "1"));
    }

    // 修改后显示
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e(Thread.currentThread().getStackTrace()[2] + "", key);
        if (key.equals("landscape_column_number")) {
            ETP_landscape_column_number.setSummary(sharedPreferences.getString(key, ""));
        } else if (key.equals("portrait_column_number")) {
            ETP_portrait_column_number.setSummary(sharedPreferences.getString(key, ""));
        }
    }

}