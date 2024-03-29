package nus.cs5248.group1.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import nus.cs5248.group1.R;
import nus.cs5248.group1.model.Storage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class RecordActivity extends Activity {
	private static final int MEDIA_TYPE_VIDEO = 2;
	
	private static final String TAG = "RecorderActivity";
	private static final String FILE_PREFIX = "cs5248";

	private Camera myCamera;
	Camera.Parameters parameters;
	
	private SurfaceView surfaceView;
	private MediaRecorder mediaRecorder;
	private SurfaceHolder mHolder;
	CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
	
	boolean recording;
	private TextView statusView;
	private ImageButton startButton;
	private Handler	recordingTimerHandler;
	private long	recordingLength;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		recording = false;
		setContentView(R.layout.activity_record);
		statusView = (TextView) findViewById(R.id.textViewStatus);

		// Get Camera for preview
		myCamera = getCameraInstance();
		if (myCamera == null) {
			statusView.setText("Error getting Camera");
		}

		surfaceView = new SurfaceView(this);
		surfaceView.setKeepScreenOn(true);
		mHolder = surfaceView.getHolder();
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.videoView1);
		frameLayout.addView(surfaceView);

		startButton = (ImageButton)findViewById(R.id.imageButton1);
		this.recordingTimerHandler = new Handler();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseCamera();
		releaseMediaRecorder();
	}
/*
	@Override
	protected void onPause() {
		super.onPause();
		
		if(recording){
			stopRecording();
		}else{
			statusView.setText("OnPause");
			releaseMediaRecorder();
			 // release the camera immediately on pause event
		}
		releaseCamera();
	}
	*/

	@Override
	protected void onResume(){
		super.onResume();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void startButtonClicked(View view) {
		if (!recording) {
			releaseCamera();

			if (!prepareMediaRecorder()) {
				Log.d(TAG, "fail in prepareMediaRecorder()");
				finish();
			}

			mediaRecorder.start();
			startRecordingTimer();
			recording = true;
			statusView.setText("Starting recording...");
			startButton.setImageResource(R.drawable.record);
		} else {
			stopRecording();
		}
	}
		
	private void stopRecording(){
		if (recording) 
		{
			try {
				mediaRecorder.stop(); // stop the recording
			} catch(RuntimeException e) {
				e.printStackTrace();
			}
			
			statusView.setText("Recording Stopped");
			recording = false;
			releaseMediaRecorder(); // release the MediaRecorder object
			
			this.recordingTimerHandler.removeCallbacks(this.recordingTimerUpdater);
			
			try
			{
				Thread.sleep(500);
				Intent intent = new Intent(RecordActivity.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			System.out.println(e);
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private boolean prepareMediaRecorder() {
	
		if(myCamera == null)
		{
			try
			{
				myCamera = getCameraInstance();
				//parameters = myCamera.getParameters();
				//parameters.setPreviewSize(mProfile.videoFrameWidth,mProfile.videoFrameHeight);
				//myCamera.setParameters(parameters);
				myCamera.setPreviewDisplay(mHolder);
				myCamera.startPreview();
				myCamera.unlock();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		mediaRecorder = new MediaRecorder();

		mediaRecorder.setCamera(myCamera);

		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		
		mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
		/*mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		//encoder
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
		//video
		mediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
		mediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);
	    mediaRecorder.setVideoSize(mProfile.videoFrameWidth,mProfile.videoFrameHeight);
		mediaRecorder.setVideoEncodingBitRate(3);
		//audio
	    mediaRecorder.setAudioChannels(mProfile.audioChannels);	
		mediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
		mediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);*/
		mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
		mediaRecorder.setPreviewDisplay(mHolder.getSurface());

		try {
			mediaRecorder.prepare();
		} catch (IllegalStateException e) {
			releaseMediaRecorder();
			Log.e(TAG, e.getMessage());
			return false;
		} catch (IOException e) {
			releaseMediaRecorder();
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}

	private void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset(); // clear recorder configuration
			mediaRecorder.release(); // release the recorder object
			mediaRecorder = null;
			myCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {

		if (myCamera != null) {
			myCamera.stopPreview();
			myCamera.release(); // release the camera for other applications
			myCamera = null;
		}
	}

	@SuppressLint("SimpleDateFormat")
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = Storage.getMediaFolder(true);

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		
		if (type == MEDIA_TYPE_VIDEO) {
			return new File(mediaStorageDir.getPath() + File.separator
					+ FILE_PREFIX + "_" + timeStamp + ".mp4");
		}
		
		return null;
	}
	
	private void startRecordingTimer() {
		this.recordingLength = 0;
		this.recordingTimerUpdater.run();
	}
	
	private void onRecordingTimerTick() {
		final long h = TimeUnit.SECONDS.toHours(this.recordingLength);
		final long m = TimeUnit.SECONDS.toMinutes(this.recordingLength) - (TimeUnit.SECONDS.toHours(this.recordingLength)* 60);
		final long s = TimeUnit.SECONDS.toSeconds(this.recordingLength) - (TimeUnit.SECONDS.toMinutes(this.recordingLength) *60);
		++this.recordingLength;
		updateRecordingLength(h, m, s);
	}
	
	private void updateRecordingLength(final long h, final long m, final long s) {
		if (h > 0) {
			this.statusView.setText(String.format("Recording %02d:%02d:%02d", h, m, s));
		} else {
			this.statusView.setText(String.format("Recording %02d:%02d", m, s));
		}
	}
	
	Runnable recordingTimerUpdater = new Runnable() {
		public void run() {
			onRecordingTimerTick();
			recordingTimerHandler.postDelayed(recordingTimerUpdater, 1000);
		}
	};
}
