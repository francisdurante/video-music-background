package com.video.videomusic;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class PreviewVideoActivity extends AppCompatActivity {
    String videoPath;
    ImageButton delete;
    ImageButton save;
    Context context = this;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        videoPath = bundle.getString("path");
        setContentView(R.layout.activity_preview_video);
        initVideoPreview();
    }

    public void initVideoPreview()
    {
        final VideoView mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setVideoPath(videoPath);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.start();
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mVideoView.start();
            }
        });
        delete = findViewById(R.id.delete);
        save = findViewById(R.id.save);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopup().show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return to web video path
            }
        });
    }

    Dialog initPopup(){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_delete_video);
        TextView no = (TextView) dialog.findViewById(R.id.no);
        TextView yes = (TextView) dialog.findViewById(R.id.yes);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(videoPath);
                if(file.exists()){
                    file.delete();
                }
                dialog.hide();
                finish();
            }
        });
       return dialog;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        initPopup().show();
    }
}
