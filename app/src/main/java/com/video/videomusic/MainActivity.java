package com.video.videomusic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class MainActivity extends Activity {
    private Camera myCamera;
    private MyCameraSurfaceView myCameraSurfaceView;
    private MediaRecorder mediaRecorder;
    public static int orientation;
    ImageButton myButton;
    SurfaceHolder surfaceHolder;
    boolean recording;
    String tempPath = "";
    Context context = this;
    ImageButton flipCamera;
    private MediaPlayer mediaPlayer;
    private String musicPath = "";
    int cameraUsing = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_READ_REQUEST_CODE = 101;
    private static final int MY_WRITE_REQUEST_CODE = 102;
    private static final int MY_RECORD_AUDIO_REQUEST_CODE = 103;
    boolean inPreview = false;
    private int selectedDuration = 30000;
    TextView thirtyDuration = null;
    TextView fifteenDuration = null;
    TextView selectedMusic = null;
    CountDownTimer timer;
    ImageButton musicGallery;
    static boolean isSelectedMusic = false;
    static String musicName = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recording = false;

        setContentView(R.layout.activity_main);

        //Get Camera for preview

        //myCamera.setDisplayOrientation(90); //Doesn't error here, but doesn't affect video.
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_READ_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_WRITE_REQUEST_CODE);
        }
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MY_RECORD_AUDIO_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }else{
            initCamera();
        }
    }

    Button.OnClickListener myButtonOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            try{
                if(recording){
                    stopRecording();
                }else{
                    releaseCamera();
                    if(!prepareMediaRecorder()){
                        Toast.makeText(MainActivity.this,
                                "Fail in prepareMediaRecorder()!\n - Ended -",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    mediaRecorder.start();
                    if(mediaPlayer != null && isSelectedMusic) {
                        mediaPlayer.start();
                    }
                    recording = true;
//                    myButton.setText("STOP");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }};

    Button.OnClickListener flipCameraOnClick = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            try{
               flipCamera(cameraUsing);
//               initCamera();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }};

    private Camera getCameraInstance(){
        // TODO Auto-generated method stub
        Camera c = null;
        try {
            c = Camera.open(cameraUsing); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    private boolean prepareMediaRecorder(){
        myCamera = getCameraInstance();

        Camera.Parameters parameters = myCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        parameters.setPreviewSize(optimalSize.width,optimalSize.height);
        myCamera.setParameters(parameters);
        // set the orientation here to enable portrait recording.
        setCameraDisplayOrientation(this,cameraUsing,myCamera);

        mediaRecorder = new MediaRecorder();
        myCamera.unlock();

        mediaRecorder.setCamera(myCamera);
        String path = Environment.getExternalStorageDirectory() + "/Android/data/"+getPackageName()+"/music/";
        musicPath = path + "test_1.mp3";

        int duration = selectedDuration;

        tempPath = getFile().getPath();

        if(!"".equals(musicPath) && isSelectedMusic) {
            mediaPlayer = MediaPlayer.create(context, Uri.parse(musicPath));
            duration = mediaPlayer.getDuration();
            tempPath = tempGetFile().getPath();
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(tempPath);

        timer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                stopRecording();
            }
        }.start();

        mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());
        mediaRecorder.setOrientationHint(MainActivity.orientation);
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = new MediaRecorder();
            myCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (myCamera != null){
            myCamera.release();        // release the camera for other applications
            myCamera = null;
        }
    }

    public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

        private SurfaceHolder mHolder;
        private Camera mCamera;
        private Activity mActivity;

        public MyCameraSurfaceView(Context context, Camera camera, Activity activity) {
            super(context);
            mCamera = camera;
            mActivity=activity;
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            surfaceHolder = mHolder;
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            try {
                setCameraDisplayOrientation(mActivity,cameraUsing,mCamera);
                previewCamera();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void previewCamera()
        {
            try
            {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                inPreview = true;
            }
            catch(Exception e)
            {
                //Log.d(APP_CLASS, "Cannot start preview", e);
            }
        }


        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            // The Surface has been created, now tell the camera where to draw the preview.
            try {

                Camera.Parameters parameters = myCamera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                parameters.setPreviewSize(optimalSize.width,optimalSize.height);
                myCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                inPreview = true;
            } catch (IOException e) {
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub

        }


    }

    public static void setCameraDisplayOrientation(Activity activity,int cameraId, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        int result;
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        MainActivity.orientation =  (info.orientation + degrees) % 360;
        camera.setDisplayOrientation(result);
    }

    public File getFile()
    {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Fleek");
        if(!folder.exists())
        {
            folder.mkdir();
        }

        return new File(folder,fileName());
    }

    public File tempGetFile()
    {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Android/data/"+getPackageName()+"/cache");
        System.out.println(folder.getAbsolutePath());
        if(!folder.exists())
        {
            folder.mkdirs();
        }

        return new File(folder,"temp_video_fleek.mp4");
    }

    public String fileName()
    {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        return "Fleek-" + dateFormat.format(date) + ".mp4";
    }

    public String addMusic(String videoInput, String audioInput, String output, Context context)
    {
        String command;
        if(isSelectedMusic) {
            command = "-i " + videoInput + " -i " + audioInput + " -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 -shortest " + output;
        }else {
            command = "-i " + videoInput +" -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 -shortest " + output;
        }
        String path = "";
        if (executeCMD(command)) {
            path = output;
        }
        return path;
    }

    private boolean executeCMD(String cmd)
    {
        int rc = FFmpeg.execute(cmd);
        if (rc == RETURN_CODE_SUCCESS) {
           return true;
        } else if (rc == RETURN_CODE_CANCEL) {
          return false;
        } else {
            Config.printLastCommandOutput(Log.INFO);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == MY_READ_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read storage denied", Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == MY_WRITE_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Write storage denied", Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == MY_RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initCamera()
    {
        myCamera = getCameraInstance();
        if(myCamera == null){
            Toast.makeText(MainActivity.this,
                    "Fail to get Camera",
                    Toast.LENGTH_LONG).show();
        }

        myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera,this);
        FrameLayout myCameraPreview = (FrameLayout)findViewById(R.id.CameraView);
        myCameraPreview.addView(myCameraSurfaceView);

        myButton = (ImageButton) findViewById(R.id.record);
        myButton.setOnClickListener(myButtonOnClickListener);

        flipCamera = (ImageButton) findViewById(R.id.flip);
        flipCamera.setOnClickListener(flipCameraOnClick);

        thirtyDuration = findViewById(R.id.thirty_duration);
        fifteenDuration = findViewById(R.id.fifteen_duration);

        selectedMusic = findViewById(R.id.music_selected);

        thirtyDuration.setTextSize(15);
        thirtyDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thirtyDuration.setTextSize(15);
                fifteenDuration.setTextSize(12);
                selectedDuration = 30;
            }
        });
        fifteenDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fifteenDuration.setTextSize(15);
                thirtyDuration.setTextSize(12);
                selectedDuration = 15;
            }
        });

        musicGallery = findViewById(R.id.music_gallery);
        musicGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context,MusicGallery.class));
            }
        });
        selectedMusic.setVisibility(View.GONE);
        selectedMusic.setSelected(true);
        if(isSelectedMusic){
            selectedMusic.setVisibility(View.VISIBLE);
            selectedMusic.setText(musicName);
        }
    }

    public void stopRecording()
    {
        // stop recording and release camera
        mediaRecorder.stop();  // stop the recording
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        String path = Environment.getExternalStorageDirectory() + "/Android/data/"+getPackageName()+"/music/";
        musicPath = path + "test_1.mp3";
        String renderedPath;
        if(isSelectedMusic){
            renderedPath = addMusic(tempPath,musicPath,getFile().getPath(),context);
        }else{
            renderedPath = tempPath;
        }
        Intent preview = new Intent(this,PreviewVideoActivity.class);
        preview.putExtra("path",renderedPath);
        startActivity(preview);
        //return to web
        timer.cancel();
        recording = false;
    }

    public void flipCamera(int cameraId)
    {
        if (inPreview) {
            myCamera.stopPreview();
        }
        myCamera.release();
        int cameraToUse;
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraToUse =  Camera.CameraInfo.CAMERA_FACING_BACK;
        }else{
            cameraToUse =  Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        myCamera = Camera.open(cameraToUse);
        cameraUsing = cameraToUse;

        setCameraDisplayOrientation(MainActivity.this, cameraId, myCamera);
        try {
            myCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Parameters parameters = myCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
        parameters.setPreviewSize(optimalSize.width,optimalSize.height);
        myCamera.setParameters(parameters);
        myCamera.startPreview();
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetWidth = 1500;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.width - targetWidth) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.width - targetWidth);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.width - targetWidth) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - targetWidth);
                }
            }
        }
        return optimalSize;
    }
}