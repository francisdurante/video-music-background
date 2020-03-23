package com.video.videomusic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MusicGallery extends AppCompatActivity {
    Context context = this;
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    private GridView musicGridView;
    private GridView dubGridView;
    ImageView musicBack;
    TextView musicNext;
    Dialog dialog;

    MediaPlayer mediaPlayer;
    JSONArray musics;
    JSONArray musicsdub;
    static String MGselectedMusic;
    CustomAdapter musicAdapter;
    CustomAdapter dubAdapter;

    private EditText searchKey;
    private TextView searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //show the activity in full screen
        setContentView(R.layout.activity_music_gallery);
        getMusic(null); // first load
    }

    public void initiateMusic(JSONArray music, JSONArray dub)
    {
        musicBack = findViewById(R.id.music_back);
        musicBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        musicGridView = (GridView) findViewById(R.id.music_grid);
        musicAdapter = new CustomAdapter(this, music,R.layout.music_details_list);
        musicGridView.setAdapter(musicAdapter);


        dubAdapter = new CustomAdapter(this, dub,R.layout.music_dub_details_list);

        dubGridView = (GridView) findViewById(R.id.dubs_grid);
        dubGridView.setAdapter(dubAdapter);

        musicNext = findViewById(R.id.next_button);
        musicNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new Dialog(context);
                dialog.setTitle("Loading");
                dialog.setContentView(R.layout.loading_layout);
                dialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
                dialog.show();
                new DownloadMusic().execute(MGselectedMusic);
            }
        });
        musicAdapter.setMediaPlayer(new MediaPlayer());
        dubAdapter.setMediaPlayer(new MediaPlayer());

        searchKey = findViewById(R.id.search_key);
        searchBtn = findViewById(R.id.search_btn);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMusic(searchKey.getText().toString());
            }
        });
    }

    public void getMusic(final String searchKey){
        dialog = new Dialog(context);
        dialog.setTitle("Loading");
        dialog.setContentView(R.layout.loading_layout);
        dialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
        dialog.show();
        if(musics == null || musicsdub == null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("musics")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                ArrayList musicData = new ArrayList();
                                ArrayList musicDub = new ArrayList();
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    try {
                                        String id = document.getId();
                                        int category = new JSONObject(document.getData()).getInt("category");
                                        if (category == 0) {
                                            JSONObject backgroundMusic = new JSONObject();
                                            backgroundMusic.put("id", id);
                                            backgroundMusic.put("data", new JSONObject(document.getData()));
                                            backgroundMusic.put("title", document.getData().get("audio_name"));
                                            musicData.add(backgroundMusic);
                                        } else {
                                            JSONObject dubMusic = new JSONObject();
                                            dubMusic.put("id", id);
                                            dubMusic.put("data", new JSONObject(document.getData()));
                                            dubMusic.put("title", document.getData().get("audio_name"));
                                            musicDub.add(dubMusic);
                                        }
                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                    }
                                }
                                musics = new JSONArray(musicData);
                                musicsdub = new JSONArray(musicDub);

                                initiateMusic(musics,musicsdub);
                                dialog.hide();
                            } else {
                                Log.w("TEST", "Error getting documents.", task.getException());
                            }
                        }
                    });
        }else{
            JSONArray music = sortItems(musics, searchKey);
            JSONArray dub = sortItems(musicsdub,searchKey);
            initiateMusic(music,dub);
            dialog.hide();
        }
    }

    JSONArray sortItems(JSONArray array, String searchValue){
        if(!"".equals(searchValue)) {
            JSONArray filteredArray = new JSONArray();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = null;
                try {
                    obj = array.getJSONObject(i);
                    if (obj.getString("title").toLowerCase().equals(searchValue.toLowerCase())) {
                        filteredArray.put(obj);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return filteredArray;
        }else{
            return array;
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
            OutputStream output;
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
                File music = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/music/");
                if(!music.exists()){
                    music.mkdir();
                }
                output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/music/test_1.mp3");

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
                //
            }

            return Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/music/test_1.mp3";
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
            if(musicAdapter.getMediaPlayer()!= null){
                musicAdapter.getMediaPlayer().stop();
                musicAdapter.setMediaPlayer(null);
            }if(dubAdapter.getMediaPlayer()!= null){
                dubAdapter.getMediaPlayer().stop();
                dubAdapter.setMediaPlayer(null);
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer = MediaPlayer.create(context, Uri.parse(file_url));
            MainActivity.duration = mediaPlayer.getDuration() + 2000;
            System.out.println(mediaPlayer.getDuration() + " aaaaaaaaaaaaaaa ");
            dialog.hide();
            dialog = null;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(musicAdapter.getMediaPlayer() != null){
            musicAdapter.getMediaPlayer().stop();
            musicAdapter.setMediaPlayer(null);
        }if(dubAdapter.getMediaPlayer() != null){
            dubAdapter.getMediaPlayer().stop();
            dubAdapter.setMediaPlayer(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(musicAdapter.getMediaPlayer() != null){
            musicAdapter.getMediaPlayer().pause();
        }if(dubAdapter.getMediaPlayer() != null){
            dubAdapter.getMediaPlayer().pause();
        }
    }
}
