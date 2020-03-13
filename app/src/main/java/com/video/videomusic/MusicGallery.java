package com.video.videomusic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class MusicGallery extends AppCompatActivity {
    Context context = this;
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    private GridView gridView;
    ImageView musicBack;

    JSONArray musics;
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
            gridView.setAdapter(new CustomAdapter(this, musics,R.layout.music_details_list));

            gridView = (GridView) findViewById(R.id.dubs_grid);
            gridView.setAdapter(new CustomAdapter(this, musics,R.layout.music_dub_details_list));
        }

        public void getMusic(){
            final Dialog dialog = new Dialog(context);
            dialog.setTitle("Loading");
            dialog.setContentView(R.layout.loading_layout);
            dialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
            dialog.show();
            final JSONObject data = new JSONObject();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("musics")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        ArrayList musicData = new ArrayList();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            JSONObject object = new JSONObject();
                            try {
                                object.put("id",id);
                                object.put("data",new JSONObject(document.getData()));
                                musicData.add(object);
                            }catch(Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                        musics = new JSONArray(musicData);
                        initiateMusic();
                        dialog.hide();
                    } else {
                        Log.w("TEST", "Error getting documents.", task.getException());
                    }
                }
            });
        }
    }
