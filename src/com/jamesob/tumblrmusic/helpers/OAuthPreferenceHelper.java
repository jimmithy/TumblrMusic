package com.jamesob.tumblrmusic.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Helper class to save and retrieve settings for this project.
 *
 * @author jobrien
 *
 */
public class OAuthPreferenceHelper {

    public static final String KEY_USER_TOKEN = "user_token";
    public static final String KEY_USER_SECRET = "user_secret";
    public static final String KEY_REQUEST_TOKEN = "request_token";
    public static final String KEY_REQUEST_SECRET = "request_secret";

    private static SharedPreferences mSettings;

    /**
     * Create a singleton instance of the default shared preferences
     *
     * @param context Application Context
     * @return
     */
    private static SharedPreferences getSettings(Context context) {
        if (mSettings == null) {
            mSettings = PreferenceManager.getDefaultSharedPreferences(context);
        }

        return mSettings;
    }

    /**
     * Save the request information for the oauth
     *
     * @param context Activity context
     * @param token Application's token
     * @param secret Application's secret key
     */
    public static void saveOAuthRequestInformation(Context context, String token, String secret) {
        SharedPreferences.Editor editor = getSettings(context).edit();

        // Handle Token
        if (TextUtils.isEmpty(token)) {
            editor.remove(KEY_REQUEST_TOKEN);
        } else {
            editor.putString(KEY_REQUEST_TOKEN, token);
        }

        // Handle Secret
        if (TextUtils.isEmpty(secret)) {
            editor.remove(KEY_REQUEST_SECRET);
        } else {
            editor.putString(KEY_REQUEST_SECRET, secret);
        }

        // Commit changes
        editor.commit();
    }

    /**
     * Save the result information from the successful OAuth authorisation flow
     *
     * @param context Activity context
     * @param token Application's token
     * @param secret Application's secret key
     */
    public static void saveOAuthInformation(Context context, String token, String secret) {
        SharedPreferences.Editor editor = getSettings(context).edit();

        // Handle Token
        if (TextUtils.isEmpty(token)) {
            editor.remove(KEY_USER_TOKEN);
        } else {
            editor.putString(KEY_USER_TOKEN, token);
        }

        // Handle Secret
        if (TextUtils.isEmpty(secret)) {
            editor.remove(KEY_USER_SECRET);
        } else {
            editor.putString(KEY_USER_SECRET, secret);
        }

        // Commit changes
        editor.commit();
    }

    /**
     * Return the OAuth request token
     *
     * @param context Application Context
     * @return the request token, null if unknown
     */
    public static String getOAuthRequestToken(Context context) {
        return getSettings(context).getString(KEY_REQUEST_TOKEN, null);
    }

    /**
     * Return the OAuth request secret
     *
     * @param context Application Context
     * @return the request secret, null if unknown
     */
    public static String getOAuthRequestSecret(Context context) {
        return getSettings(context).getString(KEY_REQUEST_SECRET, null);
    }

    /**
     * Return the OAuth token
     *
     * @param context Application Context
     * @return the request token, null if unknown
     */
    public static String getOAuthToken(Context context) {
        return getSettings(context).getString(KEY_USER_TOKEN, null);
    }

    /**
     * Return the OAuth secret
     *
     * @param context Application Context
     * @return the request token, null if unknown
     */
    public static String getOAuthSecret(Context context) {
        return getSettings(context).getString(KEY_USER_SECRET, null);
    }

    /**
     * Do we have a successful OAuth token and secret?
     *
     * @param context Application Context
     * @return true if we have both the token and secret
     */
    public static boolean hasOAuthTokenAndSecret(Context context) {
        SharedPreferences prefs = getSettings(context);
        return prefs.contains(KEY_USER_TOKEN) && prefs.contains(KEY_USER_SECRET);
    }

    /**
     * Remove all saved preferences
     *
     * @param context Application Context
     */
    public static void clearPreferences(Context context) {
        SharedPreferences.Editor editPrefs = getSettings(context).edit();
        editPrefs.clear();
        editPrefs.commit();
    }
}
