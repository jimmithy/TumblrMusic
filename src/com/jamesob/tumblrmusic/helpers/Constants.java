package com.jamesob.tumblrmusic.helpers;

public class Constants {
    // Hey! Get your own!
    // http://www.tumblr.com/oauth/apps
    public static final String CONSUMER_KEY = "";
    public static final String CONSUMER_SECRET = "";

    public static final String OAUTH_CALLBACK_SCHEME = "tumblr-music";
    public static final String OAUTH_CALLBACK_HOST = "oauth";
    public static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

    public static final String REQUEST_URL = "http://www.tumblr.com/oauth/request_token";
    public static final String ACCESS_URL = "http://www.tumblr.com/oauth/access_token";
    public static final String AUTHORIZE_URL = "http://www.tumblr.com/oauth/authorize";
    public static final String DASHBOARD_URL = "http://api.tumblr.com/v2/user/dashboard?type=audio";
}
