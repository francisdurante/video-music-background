package com.video.videomusic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MusicGallery extends AppCompatActivity {

    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    private GridView gridView;
    private String [] MUSICS = {"https://mercercognitivepsychology.pbworks.com/f/1384897684/colorful_note.jpg",
    "https://mercercognitivepsychology.pbworks.com/f/1384897684/colorful_note.jpg","https://mercercognitivepsychology.pbworks.com/f/1384897684/colorful_note.jpg",
    "https://mercercognitivepsychology.pbworks.com/f/1384897684/colorful_note.jpg","https://mercercognitivepsychology.pbworks.com/f/1384897684/colorful_note.jpg",
    "https://mercercognitivepsychology.pbworks.com/f/1384897684/colorful_note.jpg"};
    ImageView musicBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //show the activity in full screen
        setContentView(R.layout.activity_music_gallery);
        initiateMusic();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }


    class DownloadMusic extends AsyncTask<String, String, String> {
            @Override
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
                    OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Android/data/" + getPackageName() + "/music/");

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
                    Log.e("Error: ", e.getMessage());
                }

                return null;
            }

            /**
             * Updating progress bar
             */
            protected void onProgressUpdate(String... progress) {
                // setting progress percentage
                pDialog.setProgress(Integer.parseInt(progress[0]));
            }

            /**
             * After completing background task Dismiss the progress dialog
             **/
            @Override
            protected void onPostExecute(String file_url) {
                // dismiss the dialog after the file was downloaded
                dismissDialog(progress_bar_type);

            }
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
            gridView.setAdapter(new CustomAdapter(this, MUSICS,R.layout.music_details_list));

            gridView = (GridView) findViewById(R.id.dubs_grid);
            gridView.setAdapter(new CustomAdapter(this, MUSICS,R.layout.music_dub_details_list));
        }

    }
