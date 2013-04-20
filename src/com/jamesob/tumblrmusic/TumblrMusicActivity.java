package com.jamesob.tumblrmusic;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jamesob.tumblrmusic.adapters.TracksAdapter;
import com.jamesob.tumblrmusic.helpers.Constants;
import com.jamesob.tumblrmusic.helpers.OAuthPreferenceHelper;
import com.jamesob.tumblrmusic.objects.AudioPost;
import com.jamesob.tumblrmusic.tasks.DashMusicTask;
import com.jamesob.tumblrmusic.tasks.DashMusicTask.DashMusicTaskListener;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import java.util.List;

/**
 * Main Activity
 *
 * @author jobrien
 *
 */
public class TumblrMusicActivity extends Activity implements DashMusicTaskListener, OnItemClickListener {
    private static final String TAG = "TumblrMusic";

    // startActivityForResult Request Codes
    private static final int REQUEST_LOGIN = 1;

    // SavedInstanceState Keys
    private static final String KEY_TRACKS = "retainedTracks";

    // Variables
    private OAuthConsumer mConsumer;
    private TracksAdapter mTracksListAdapter;
    private DashMusicTask mGetMusicTask;

    // Views
    private WebView wvPlayer;
    private ProgressBar pbWebView;
    private ListView lstTracks;
    private ProgressBar pbLoadingTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up loading dialog
        pbLoadingTracks = (ProgressBar) findViewById(R.id.pbLoading);

        // Set Up Webview
        wvPlayer = (WebView) findViewById(R.id.wvPlayer);
        pbWebView = (ProgressBar) findViewById(R.id.pbWebView);
        initWebView();

        // Set Up ListView
        lstTracks = (ListView) findViewById(R.id.lstPlaylist);
        mTracksListAdapter = new TracksAdapter(this);
        lstTracks.setAdapter(mTracksListAdapter);
        lstTracks.setOnItemClickListener(this);

        // Create OAuth Consumer
        mConsumer = new CommonsHttpOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);

        // Retain Tracks
        if (savedInstanceState != null) {
            mTracksListAdapter.addAll(savedInstanceState.getParcelableArrayList(KEY_TRACKS));
            mTracksListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mTracksListAdapter.isEmpty() && (mGetMusicTask == null || mGetMusicTask.getStatus() != Status.FINISHED)) {
            fetchAudioPosts();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_TRACKS, mTracksListAdapter.getItems());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tumblr_music, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                mTracksListAdapter.clear();
                mTracksListAdapter.notifyDataSetChanged();
                fetchAudioPosts();
                break;

            case R.id.menu_logout:
                OAuthPreferenceHelper.clearPreferences(this);
                startActivityForResult(OAuthActivity.createIntent(this), REQUEST_LOGIN);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetch Audio posts from the tumblr api. If we do not have an authentication, the login screen
     * is shown.
     */
    private void fetchAudioPosts() {
        if (OAuthPreferenceHelper.hasOAuthTokenAndSecret(this)) {
            // Init Comsumer
            mConsumer.setTokenWithSecret(OAuthPreferenceHelper.getOAuthToken(this), OAuthPreferenceHelper.getOAuthSecret(this));
            // Get Posts
            mGetMusicTask = new DashMusicTask(mConsumer);
            mGetMusicTask.setListener(this);
            mGetMusicTask.execute(Constants.DASHBOARD_URL);

            // Show Loading Dialog
            if (pbLoadingTracks != null) {
                pbLoadingTracks.setVisibility(View.VISIBLE);
            }
        } else {
            startActivityForResult(OAuthActivity.createIntent(this), REQUEST_LOGIN);
        }
    }

    /**
     * Create and initalise web view object
     */
    private void initWebView() {
        if (wvPlayer == null) {
            Log.e(TAG, "Can't initiating WebView when the view is null");
            return;
        }

        wvPlayer.getSettings().setPluginsEnabled(true);
        wvPlayer.getSettings().setPluginState(PluginState.ON);
        wvPlayer.getSettings().setJavaScriptEnabled(true);

        // Fix Width Hack
        wvPlayer.getSettings().setUseWideViewPort(true);
        wvPlayer.setInitialScale(140);

        wvPlayer.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Launch all URLs externally
                launchUrl(url);
                return true;
            }
        });

        wvPlayer.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress > 95) {
                    // Nearly done, hide the progressbar if visible
                    if (pbWebView.isShown()) {
                        pbWebView.setVisibility(View.GONE);
                    }
                } else {
                    if (!pbWebView.isShown()) {
                        // Show progress bar if it is not currently visible
                        pbWebView.setVisibility(View.VISIBLE);
                    }

                    // Update progress
                    pbWebView.setProgress(newProgress);
                }

                super.onProgressChanged(view, newProgress);
            }
        });
    }

    protected void launchUrl(String url) {
        Intent spotifyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        spotifyIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            startActivity(spotifyIntent);
        } catch (ActivityNotFoundException anfe) {
            Log.e(TAG, "Unable to launch URL", anfe);
            Toast.makeText(this, R.string.no_activity_found, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Helper method to create intent for this activity
     *
     * @param context Activity Context
     * @return
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, TumblrMusicActivity.class);
    }

    @Override
    public void onResultComplete(List<AudioPost> posts) {
        if (pbLoadingTracks != null) {
            pbLoadingTracks.setVisibility(View.GONE);
        }

        if (posts != null && !posts.isEmpty() && mTracksListAdapter != null) {
            mTracksListAdapter.addAll(posts);
            mTracksListAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "No results returned or adapter was null");
            Toast.makeText(this, R.string.no_audio_posts, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mTracksListAdapter != null && wvPlayer != null) {
            AudioPost track = mTracksListAdapter.getItem(position);
            wvPlayer.loadData(track.getHtml(), "text/html", "UTF-8");
        }
    }

}
