package com.rcumis.vigilance.vicpl.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Arjun on 10/29/2016.
 * Shared preferences to store the email of the logged in user
 */

public class VigilancePreferenceManager {

    private static String Preference_Name = "VigilanceSharedPreferences";

    static SharedPreferences prefs;

    private static String EMAIL = "E-mail";

    public static void setEmialOfuser(Context context, String email) {
        if (context != null) {
            getSharedPreference(context).edit().putString(EMAIL, email).apply();
        }
    }

    public static String getEmailOfUser(Context context) {
        if (context != null) {
            return  getSharedPreference(context).getString(EMAIL,"");
        }
        return "";
    }

    public static void clear(Context context) {
        if (context != null) {
            getSharedPreference(context).edit().clear().commit();
        }
    }

    private static SharedPreferences getSharedPreference(Context context) {
        if(prefs==null){
            prefs = context.getSharedPreferences(Preference_Name, Context.MODE_PRIVATE);
        }
        return prefs;
    }
}
