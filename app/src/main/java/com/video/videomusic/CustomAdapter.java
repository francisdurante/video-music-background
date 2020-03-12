package com.video.videomusic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

public class CustomAdapter extends BaseAdapter {
    private Context context;
    private final String[] musicThumbnail;
    private int layoutToPut;

    public CustomAdapter(Context context, String[] musicThumbnail,int layoutToPut) {
        this.context = context;
        this.musicThumbnail = musicThumbnail;
        this.layoutToPut = layoutToPut;
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(context);

            // get layout from mobile.xml
            gridView = inflater.inflate(this.layoutToPut, null);
            ImageView imageView = gridView.findViewById(R.id.music_thumbnail);
            Picasso.with(context)
                    .load(musicThumbnail[position])
                    .into(imageView);
            // set value into textview
            TextView musicTitle = (TextView) gridView.findViewById(R.id.music_title);
            musicTitle.setText("Music Title" + position);

            TextView musicArtist = (TextView) gridView.findViewById(R.id.music_artist);
            musicArtist.setText("Music Artist" + position);

            TextView musicDuration = (TextView) gridView.findViewById(R.id.music_duration);
            musicDuration.setText("1:0" + position);

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return musicThumbnail.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
