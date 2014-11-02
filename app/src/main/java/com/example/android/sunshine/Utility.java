package com.example.android.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by mattconte on 01/11/14.
 */
public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }
}
