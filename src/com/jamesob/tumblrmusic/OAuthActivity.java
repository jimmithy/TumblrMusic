package com.jamesob.tumblrmusic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.jamesob.tumblrmusic.helpers.Constants;
import com.jamesob.tumblrmusic.helpers.OAuthPreferenceHelper;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

public class OAuthActivity extends Activity {
    protected static final String TAG = "OAuth";

    private OAuthConsumer mConsumer = null;
    private OAuthProvider mProvider = null;
    private OAuthHandler mHandler = null;

    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oauth);

        mConsumer = new CommonsHttpOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
        mProvider = new CommonsHttpOAuthProvider(Constants.REQUEST_URL, Constants.ACCESS_URL, Constants.AUTHORIZE_URL);
        mHandler = new OAuthHandler();

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                new Thread(requestUrlThread).start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check response from
        Uri uri = getIntent().getData();
        if (uri != null && uri.getScheme().equals(Constants.OAUTH_CALLBACK_SCHEME)) {
            new Thread(handleResultThread).start();
        } else {
            btnLogin.setEnabled(true);
        }
    }

    /**
     * Thread to launch browser for OAuth Permission
     */
    private Runnable requestUrlThread = new Runnable() {
        @Override
        public void run() {
            String url;
            try {
                // Get Request URL
                url = mProvider.retrieveRequestToken(mConsumer, Constants.OAUTH_CALLBACK_URL);

                // Save Request Information
                OAuthPreferenceHelper.saveOAuthRequestInformation(OAuthActivity.this, mConsumer.getToken(), mConsumer.getTokenSecret());

                // Launch Browser
                mHandler.sendMessage(mHandler.obtainMessage(OAuthHandler.MESSAGE_LAUNCH_BROWSER, url));
            } catch (Exception e) {
                Log.e(TAG, "Error requesting browser url", e);
            }
        };
    };

    /**
     * Thread to handle OAuth result
     */
    private Runnable handleResultThread = new Runnable() {
        @Override
        public void run() {
            Uri intentResult = getIntent().getData();

            String token = OAuthPreferenceHelper.getOAuthRequestToken(OAuthActivity.this);
            String secret = OAuthPreferenceHelper.getOAuthRequestSecret(OAuthActivity.this);

            try {
                if (token != null && secret != null) {
                    mConsumer.setTokenWithSecret(token, secret);
                }

                String verifier = intentResult.getQueryParameter("oauth_verifier");

                mProvider = new CommonsHttpOAuthProvider(Constants.REQUEST_URL,
                        Constants.ACCESS_URL + "?oauth_verifier=" + verifier,
                        Constants.AUTHORIZE_URL);
                mProvider.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                mProvider.retrieveAccessToken(mConsumer, verifier);

                token = mConsumer.getToken();
                secret = mConsumer.getTokenSecret();
                OAuthPreferenceHelper.saveOAuthInformation(OAuthActivity.this, token, secret);

                // Launch Main Activity
                mHandler.sendEmptyMessage(OAuthHandler.MESSAGE_LAUNCH_MAIN_ACTIVITY);
            } catch (Exception e) {
                Log.e(TAG, "Error retrieving access token", e);
                mHandler.sendEmptyMessage(OAuthHandler.MESSAGE_LOGIN_FAILED);
            }
        };
    };

    /**
     * @return Intent to launch the OAuth activity
     */
    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, OAuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    @SuppressLint("HandlerLeak")
    private class OAuthHandler extends Handler {
        protected static final int MESSAGE_LOGIN_FAILED = 0;
        protected static final int MESSAGE_LAUNCH_BROWSER = 1;
        protected static final int MESSAGE_LAUNCH_MAIN_ACTIVITY = 2;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_LAUNCH_BROWSER:
                    if (msg.obj instanceof String) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) msg.obj));
                        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(browserIntent);
                    }
                    break;

                case MESSAGE_LAUNCH_MAIN_ACTIVITY:
                    startActivity(TumblrMusicActivity.createIntent(OAuthActivity.this));
                    finish();
                    break;

                case MESSAGE_LOGIN_FAILED:
                    Toast.makeText(OAuthActivity.this, R.string.login_failed, Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                    break;
            }

            super.handleMessage(msg);
        };
    };
}
