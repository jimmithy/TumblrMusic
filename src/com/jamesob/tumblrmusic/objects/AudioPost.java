package com.jamesob.tumblrmusic.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Object representation of tumblr's post json response
 *
 * @author jobrien
 *
 */
public class AudioPost implements Parcelable {
    private final static String KEY_PLAYER = "player";
    private static final String KEY_BLOGNAME = "blog_name";
    private static final String KEY_TRACKNAME = "track_name";
    private static final String KEY_ARTIST = "artist";

    public String player;
    public String blogname;
    public String trackname;
    public String artist;

    /**
     * Default Constructor
     */
    public AudioPost() {
    }

    /**
     * Parcelable Constructor
     */
    public AudioPost(Parcel in) {
        player = in.readString();
        blogname = in.readString();
        artist = in.readString();
        trackname = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(player);
        dest.writeString(blogname);
        dest.writeString(artist);
        dest.writeString(trackname);
    }

    public final Parcelable.Creator<AudioPost> CREATOR = new Parcelable.Creator<AudioPost>() {
        public AudioPost createFromParcel(Parcel in) {
            return new AudioPost(in);
        }

        public AudioPost[] newArray(int size) {
            return new AudioPost[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Parse json object to create AudioPost object
     *
     * @param item JSONObject to parse
     */
    public static AudioPost parseJson(JSONObject item) {
        AudioPost output = new AudioPost();

        try {
            // Needed variables
            output.player = item.getString(KEY_PLAYER);
            output.blogname = item.getString(KEY_BLOGNAME);
            output.trackname = item.getString(KEY_TRACKNAME);

            // Optional variables
            output.artist = item.optString(KEY_ARTIST);
        } catch (JSONException e) {
            // If we don't have one of the needed variables, a JSON exception will be thrown.
            // In this case, we don't want this object so null is returned
            return null;
        }

        return output;
    }

    /**
     * String to display in the listview for the track name
     */
    public String getTrackString() {
        StringBuilder sb = new StringBuilder();

        // Append Artist
        if (!TextUtils.isEmpty(artist)) {
            sb.append(artist).append(" - ");
        }

        // Append Track
        if (!TextUtils.isEmpty(trackname)) {
            sb.append(trackname);
        }

        return sb.toString();
    }

    /**
     * Get html to be displayed in the webview
     */
    public String getHtml() {
        return player;
    }
}
