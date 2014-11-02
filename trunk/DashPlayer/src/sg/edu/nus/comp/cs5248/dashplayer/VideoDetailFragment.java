package sg.edu.nus.comp.cs5248.dashplayer;

import java.util.ArrayDeque;
import java.util.Queue;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
//import android.widget.TextView;

public class VideoDetailFragment extends Fragment {
	protected static final String TAG = "VideoDetailFragment";
	private RelativeLayout	mediaContainer;
    private MediaPlayer 	currentMediaPlayer;
    private SurfaceHolder	currentHolder;
    private SurfaceView		currentSurface;
    private MediaPlayer 	nextMediaPlayer;
    private SurfaceHolder	nextHolder;
    private SurfaceView		nextSurface;
    
    final Queue<VideoSegment> readySegments = new ArrayDeque<VideoSegment>();
    VideoSegment activeSegment = null;
	
	public VideoDetailFragment () {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
		
		View rootView 		= inflater.inflate(R.layout.activity_video_detail, container, false);
		this.mediaContainer	= (RelativeLayout) rootView.findViewById(R.id.media_container);
		return rootView;
	}
	
	//Try out:
	//@Override
	//public void streamletDownloadDidFinish(VideoSegmentInfo segmentInfo) {
	//	Log.d(TAG, "Quality=" + segmentInfo.getCacheQuality() + "p, Cache: " + segmentInfo.getCacheFilePath());
	//	queueForPlayback(segmentInfo);
//	}
	//The download need to call back to here so that it can queueForPlayback
	
	
	//private void prepareNextPlayer (final VideoSegment segment) {
	public void prepareNextPlayer (final VideoSegment segment) {
		Log.i(TAG, "Preparing player for: " + segment.getCacheFilePath());
		
    	this.nextSurface = new SurfaceView(this.getActivity());
        this.nextHolder  = this.nextSurface.getHolder();
        mediaContainer.addView(nextSurface, 0);
        
        this.nextHolder.addCallback(new SurfaceHolder.Callback() {
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				try {
					nextMediaPlayer = new MediaPlayer();
			        nextMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
						
						@Override
						public void onCompletion(MediaPlayer mp) {
							mp.release();
							startNextPlayer();
						}
					});
			        nextMediaPlayer.setDataSource(segment.getCacheFilePath());
			        nextMediaPlayer.setSurface(nextHolder.getSurface());
			        nextMediaPlayer.prepare();
			        
			        int videoWidth = nextMediaPlayer.getVideoWidth();
					int videoHeight = nextMediaPlayer.getVideoHeight();
					
					int surfaceWidth = nextHolder.getSurfaceFrame().width();
					ViewGroup.LayoutParams params = nextSurface.getLayoutParams();
					params.width = surfaceWidth;
					params.height = (int) (((float) videoHeight / (float) videoWidth) * (float) surfaceWidth);
					nextSurface.setLayoutParams(params);
					
					nextMediaPlayer.start();
					nextMediaPlayer.pause();
					
					if (currentMediaPlayer == null) {
						startNextPlayer();
					}
				} catch (Exception e) {
		    		e.printStackTrace();
		    	}
			}
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) { }
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }
		});
    }
    
    private void startNextPlayer() {
    	Log.d(TAG, "Next player is starting...");
    	
    	SurfaceView previousSurface = this.currentSurface;
    	
    	this.currentMediaPlayer = this.nextMediaPlayer;
    	this.currentHolder = this.nextHolder;
    	this.currentSurface = this.nextSurface;
    	
    	if (this.currentMediaPlayer != null) {
    		this.currentMediaPlayer.start();
    	}
    	
    	if (previousSurface != null) {
    		try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		this.mediaContainer.removeView(previousSurface);
    	}
    	
    	this.nextMediaPlayer = null;
    	this.nextHolder = null;
    	this.nextSurface = null;
    	
    	scheduleNext();
    }
    
	private synchronized void scheduleNext() {
		this.activeSegment = this.readySegments.poll();
		//bufferContentChanged();
		
		if (this.activeSegment != null) {
			prepareNextPlayer(this.activeSegment);
		} else {
			Log.i(TAG, "Buffer is empty");
			//this.statusText.setText(R.string.ready);
		}
	}
	
	public synchronized void queueForPlayback(final VideoSegment segment) {
		this.readySegments.offer(segment);
		//bufferContentChanged();
		
		if (this.activeSegment == null) {
			this.scheduleNext();
		}
	}
	
	
		
/*	
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.activity_video_detail,
        container, false);
    return view;
  }

  public void setText(String item) {
    TextView view = (TextView) getView().findViewById(R.id.detailsText);
    view.setText(item);
  }
  */
} 
