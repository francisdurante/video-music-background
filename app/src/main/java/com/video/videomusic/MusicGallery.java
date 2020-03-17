package com.video.videomusic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
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
    private GridView gridView;
    ImageView musicBack;
    TextView musicNext;
    Dialog dialog;

    MediaPlayer mediaPlayer;
    JSONArray musics;
    JSONArray musicsdub;
    static String MGselectedMusic;
    CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //show the activity in full screen
        setContentView(R.layout.activity_music_gallery);
        getMusic();
    }

    public void initiateMusic()
    {
        musicBack = findViewById(R.id.music_back);
        musicBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gridView = (GridView) findViewById(R.id.music_grid);
        adapter = new CustomAdapter(this, musics,R.layout.music_details_list);
        gridView.setAdapter(adapter);

        gridView = (GridView) findViewById(R.id.dubs_grid);
        gridView.setAdapter(new CustomAdapter(this, musicsdub,R.layout.music_dub_details_list));

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
        adapter.setMediaPlayer(new MediaPlayer());
    }

    public void getMusic(){
        dialog = new Dialog(context);
        dialog.setTitle("Loading");
        dialog.setContentView(R.layout.loading_layout);
        dialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
        dialog.show();
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
                            if(category == 0) {
                                JSONObject backgroundMusic = new JSONObject();
                                backgroundMusic.put("id", id);
                                backgroundMusic.put("data", new JSONObject(document.getData()));
                                musicData.add(backgroundMusic);
                            }else{
                                JSONObject dubMusic = new JSONObject();
                                dubMusic.put("id", id);
                                dubMusic.put("data", new JSONObject(document.getData()));
                                musicDub.add(dubMusic);
                            }
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                    musics = new JSONArray(musicData);
                    musicsdub = new JSONArray(musicDub);
                    initiateMusic();
                    dialog.hide();
                } else {
                    Log.w("TEST", "Error getting documents.", task.getException());
                }
            }
        });
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
                //
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
            if(adapter.getMediaPlayer()!= null){
                adapter.getMediaPlayer().stop();
                adapter.setMediaPlayer(null);
            }
            dialog.hide();
            dialog = null;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adapter.getMediaPlayer() != null){
            adapter.getMediaPlayer().stop();
            adapter.setMediaPlayer(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(adapter.getMediaPlayer() != null){
            adapter.getMediaPlayer().pause();
        }
    }
}
