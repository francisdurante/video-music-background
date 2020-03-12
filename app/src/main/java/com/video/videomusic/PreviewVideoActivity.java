package com.video.videomusic;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class PreviewVideoActivity extends AppCompatActivity {
    String videoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        videoPath = bundle.getString("temp_path");
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
    }

}
