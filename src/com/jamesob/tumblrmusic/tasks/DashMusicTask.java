package com.jamesob.tumblrmusic.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.jamesob.tumblrmusic.objects.AudioPost;

import oauth.signpost.OAuthConsumer;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DashMusicTask extends AsyncTask<String, Void, List<AudioPost>> {
    private static final String TAG = "DashMusicTask";

    // Tumblr Response Keys
    private static final String KEY_RESPONSE = "response";
    private static final String KEY_POSTS = "posts";

    OAuthConsumer mConsumer = null;
    DashMusicTaskListener getMusicListener;

    public DashMusicTask(OAuthConsumer consumer) {
        mConsumer = consumer;
    }

    public void setListener(DashMusicTaskListener listener) {
        getMusicListener = listener;
    }

    /**
     * Listener interface to recieve task callback
     */
    public interface DashMusicTaskListener {
        /**
         * Result of AsyncTask
         *
         * @param posts List of audio posts
         */
        void onResultComplete(List<AudioPost> posts);
    }

    @Override
    protected List<AudioPost> doInBackground(String... params) {

        // Confirm we are requesting a url
        if (params == null || params.length == 0) {
            return null;
        }

        List<AudioPost> entries = new ArrayList<AudioPost>();
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        HttpClient client = new DefaultHttpClient();
        HttpGet getMusic = new HttpGet(params[0]);

        String responseBody = null;

        try {
            mConsumer.sign(getMusic);
            responseBody = client.execute(getMusic, responseHandler);

            JSONObject jObject = new JSONObject(responseBody);
            JSONObject responseObject = jObject.getJSONObject(KEY_RESPONSE);
            JSONArray postsArray = responseObject.getJSONArray(KEY_POSTS);

            AudioPost post;

            for (int i = 0; i < postsArray.length(); i++) {
                JSONObject item = postsArray.getJSONObject(i);

                // Create new post object
                post = AudioPost.parseJson(item);

                if (post != null) {
                    // add to output list
                    entries.add(post);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting dashboard posts", e);
        }

        return entries;
    }

    @Override
    protected void onPostExecute(List<AudioPost> result) {
        super.onPostExecute(result);

        if (getMusicListener != null) {
            getMusicListener.onResultComplete(result);
        }
    }
}
