package com.video.videomusic;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.video.videomusic.MainActivity.isSelectedMusic;
import static com.video.videomusic.MainActivity.musicName;

public class CustomAdapter extends BaseAdapter{
    private Context context;
    final Dialog dialog;
    private final JSONArray musicData;
    private int layoutToPut;
    MediaPlayer mediaPlayer = new MediaPlayer();

    public CustomAdapter(Context context, JSONArray musicData, int layoutToPut) {
        this.context = context;
        this.musicData = musicData;
        this.layoutToPut = layoutToPut;

        dialog = new Dialog(context);
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView = null;

        if (convertView == null) {
            try {
                gridView = new View(context);

                // get layout from mobile.xml
                JSONObject data = musicData.getJSONObject(position).getJSONObject("data");
                String id = musicData.getJSONObject(position).getString("id");

                gridView = inflater.inflate(this.layoutToPut, null);

                ImageView imageView = gridView.findViewById(R.id.music_thumbnail);
                Glide.with(context)
                        .load(data.getString("thumbnail_link"))
                        .into(imageView);

                // set value into textview
                TextView musicTitle = (TextView) gridView.findViewById(R.id.music_title);
                musicTitle.setText(data.getString("audio_name"));

                TextView musicArtist = (TextView) gridView.findViewById(R.id.music_artist);
                musicArtist.setText(data.getString("artist"));

                TextView musicDuration = (TextView) gridView.findViewById(R.id.music_duration);
                String duration = data.getString("duration");
                musicDuration.setText(duration);

                musicTitle.setOnClickListener(new SelectedMusic(data.getString("audio_link"),data.getString("audio_name")));
                imageView.setOnClickListener(new SelectedMusic(data.getString("audio_link"),data.getString("audio_name")));
                musicArtist.setOnClickListener(new SelectedMusic(data.getString("audio_link"),data.getString("audio_name")));

            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        else{
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return musicData.length();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public class SelectedMusic implements View.OnClickListener {

        String musicLink;
        String name;
        SelectedMusic(String link, String name) {
            this.musicLink = link;
            this.name = name;
        }
        @Override
        public void onClick(View v) {
//            if(mediaPlayer != null) mediaPlayer.stop(); mediaPlayer = null;
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
//                mediaPlayer.setDataSource(musicLink);
//                mediaPlayer.prepare();
//                mediaPlayer.start();
                isSelectedMusic = true;
                musicName = name;
                dialog.setTitle("Loading");
                dialog.setContentView(R.layout.loading_layout);
                dialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
                dialog.show();
                new DownloadMusic().execute(musicLink);
            } catch (Exception e) {
            }
        }
    }
    class DownloadMusic extends AsyncTask<String, String, String>{
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/music/test_1.mp3");

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            System.out.println(e.getMessage() + "bbbbbbbbbbbbbbbbbbbbbbbbb");
        }

        return null;
    }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
        // setting progress percentage

        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {
            dialog.hide();
        }
    }
}
