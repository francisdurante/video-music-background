package com.video.videomusic;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class MainActivity extends Activity {

//Starter Tutorial: http://sandyandroidtutorials.blogspot.co.uk/2013/05/android-video-capture-tutorial.html
    private Camera myCamera;
    private MyCameraSurfaceView myCameraSurfaceView;
    private MediaRecorder mediaRecorder;
    public static int orientation;
    Button myButton;
    SurfaceHolder surfaceHolder;
    boolean recording;
    String tempPath = "";
    Context context = this;
    final String CACHE_PATH = Environment.getExternalStorageDirectory() + "/Android/data/com.video.videomusic/cache";
    final String MUSIC_PATH = Environment.getExternalStorageDirectory() + "/Android/data/com.video.videomusic/music/";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recording = false;

        setContentView(R.layout.activity_main);

        //Get Camera for preview
        myCamera = getCameraInstance();

        //myCamera.setDisplayOrientation(90); //Doesn't error here, but doesn't affect video.

        if(myCamera == null){
            Toast.makeText(MainActivity.this,
                    "Fail to get Camera",
                    Toast.LENGTH_LONG).show();
        }

        myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera,this);
        FrameLayout myCameraPreview = (FrameLayout)findViewById(R.id.CameraView);
        myCameraPreview.addView(myCameraSurfaceView);

        myButton = (Button)findViewById(R.id.mybutton);
        myButton.setOnClickListener(myButtonOnClickListener);
    }

    Button.OnClickListener myButtonOnClickListener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            try{
                if(recording){
                    // stop recording and release camera
                    mediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    addMusic(tempPath,MUSIC_PATH + "test_1.mp3",getFile().getPath(),context);
                    //Exit after saved
                    //finish();
                    myButton.setText("REC");
                    recording = false;
                }else{

                    //Release Camera before MediaRecorder start
                    releaseCamera();

                    if(!prepareMediaRecorder()){
                        Toast.makeText(MainActivity.this,
                                "Fail in prepareMediaRecorder()!\n - Ended -",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    mediaRecorder.start();
                    recording = true;
                    myButton.setText("STOP");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }};

    private Camera getCameraInstance(){
        // TODO Auto-generated method stub
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    private boolean prepareMediaRecorder(){
        myCamera = getCameraInstance();

        // set the orientation here to enable portrait recording.
        setCameraDisplayOrientation(this,Camera.CameraInfo.CAMERA_FACING_FRONT,myCamera);

        mediaRecorder = new MediaRecorder();
        myCamera.unlock();

        mediaRecorder.setCamera(myCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        tempPath = tempGetFile().getPath();
        mediaRecorder.setOutputFile(tempPath);
        mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(50000000); // Set max file size 50Mb

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
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            try {
                setCameraDisplayOrientation(mActivity,0,mCamera);
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
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
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
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        MainActivity.orientation = (info.orientation - degrees + 360) % 360;
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
        File folder = new File(CACHE_PATH);
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

    public boolean addMusic(String videoInput, String audioInput, String output, Context context) {
        String command = "-i " + videoInput + " -i " + audioInput + " -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 -shortest " + output ;
        executeCMD(command);
        return true;
    }

    private void executeCMD(String cmd)
    {
        int rc = FFmpeg.execute(cmd);
        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
        }
    }
}