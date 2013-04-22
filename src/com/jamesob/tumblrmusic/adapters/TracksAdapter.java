package com.jamesob.tumblrmusic.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jamesob.tumblrmusic.objects.AudioPost;

import java.util.ArrayList;
import java.util.Collection;

public class TracksAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<AudioPost> mItems = new ArrayList<AudioPost>();

    public TracksAdapter(Context context) {
        super();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addAll(Collection<? extends AudioPost> collection) {
        mItems.addAll(collection);
    }

    /**
     * Wrapper to cast this to a Collection
     *
     * @param parcelableArrayList List of retained item
     */
    @SuppressWarnings("unchecked")
    public void addAll(ArrayList<Parcelable> parcelableArrayList) {
        addAll((Collection<? extends AudioPost>) parcelableArrayList);
    }

    /**
     * Get all items in listview
     *
     * @return
     */
    public ArrayList<AudioPost> getItems() {
        return mItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(android.R.layout.simple_list_item_2, null);
            convertView.setTag(holder);

            holder.user = (TextView) convertView.findViewById(android.R.id.text1);
            holder.track = (TextView) convertView.findViewById(android.R.id.text2);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AudioPost post = getItem(position);

        if (holder.user != null) {
            holder.user.setText(post.blogname);
        }

        if (holder.track != null) {
            holder.track.setText(post.getTrackString());
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView user;
        public TextView track;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public AudioPost getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).hashCode();
    }

    public void clear() {
        mItems.clear();
    }
}
