package sg.edu.nus.comp.cs5248.dashplayer;

import java.util.ArrayDeque;
import java.util.Queue;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class VideoDetailFragment extends Fragment {
	protected static final String TAG = "VideoDetailFragment";
	public static final String ARG_ITEM_ID = "item_id";
    private SurfaceHolder	currentHolder;
    private SurfaceView		currentSurface;
    public static TextView 		bandwidthText;
    public static TextView 		bufferText;
    public static TextView 		statusText;
    
    final Queue<VideoSegment> readySegments = new ArrayDeque<VideoSegment>();
    VideoSegment activeSegment = null;
    final MediaPlayer mediaPlayer = new MediaPlayer();
	
	public VideoDetailFragment () {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
		
		View rootView 		= inflater.inflate(R.layout.activity_video_detail, container, false);

    	this.currentSurface = (SurfaceView) rootView.findViewById(R.id.videoView1);
        this.currentHolder  = this.currentSurface.getHolder();
        VideoDetailFragment.bandwidthText 	= (TextView) rootView.findViewById(R.id.bandwidth_text);
        VideoDetailFragment.bufferText 	= (TextView) rootView.findViewById(R.id.buffer_text);
        VideoDetailFragment.statusText 	= (TextView) rootView.findViewById(R.id.status_text);
        
		return rootView;
	}
	
	//Try out:
	//@Override
	//public void streamletDownloadDidFinish(VideoSegment segmentInfo) {
	//	Log.d(TAG, "Quality=" + segmentInfo.getCacheQuality() + "p, Cache: " + segmentInfo.getCacheFilePath());
	//	queueForPlayback(segmentInfo);
	//}
	//The download need to call back to here so that it can queueForPlayback
	
	
	//private void prepareNextPlayer (final VideoSegment segment) {
	public void prepareNextPlayer (final VideoSegment segment) {
		Log.i(TAG, "Preparing player for: " + segment.getCacheFilePath());
        
		String st = segment.getCacheFilePath();

		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mediaPlayer)
			{
				//MediaController mediaController = new MediaController(mContext);
				if(mediaPlayer != null)
				{
					mediaPlayer.stop();
					mediaPlayer.reset();
					scheduleNext();
				}
			}
		});
		
		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
		{
			@Override
			public void onPrepared(MediaPlayer mediaPlayer)
			{
				mediaPlayer.start();
			}
		});

		try
		{
			mediaPlayer.setDataSource(st);
			mediaPlayer.setSurface(currentHolder.getSurface());
			mediaPlayer.setDisplay(currentHolder);	
			mediaPlayer.prepare();
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
        
	private synchronized void scheduleNext() {
		this.activeSegment = this.readySegments.poll();
		
		int count = this.readySegments.size();
		
		if (count > 8) {
			setStrategy(StreamTask.HALT);
		} else if (count > 4) {
			setStrategy(StreamTask.AT_LEAST_FOUR_SECONDS);
		} else {
			setStrategy(StreamTask.AS_FAST_AS_POSSIBLE);
		}
		
		if (this.readySegments != null) {
			VideoDetailFragment.bufferText.setText(Integer.toString(this.readySegments.size()));
		} else {
			VideoDetailFragment.bufferText.setText("0");
		}
		
		if (this.activeSegment != null) {
			prepareNextPlayer(this.activeSegment);
		} else {
			Log.i(TAG, "Buffer is empty");
			//this.statusText.setText(R.string.ready);
		}
	}
	
	public synchronized void setStrategy(final int newStrategy) {
		if (StreamTask.strategy != newStrategy) {
			if (newStrategy != StreamTask.HALT && 
					newStrategy != StreamTask.AS_FAST_AS_POSSIBLE && 
					newStrategy != StreamTask.AT_LEAST_FOUR_SECONDS) {
				throw new RuntimeException("Invalid strategy: " + newStrategy);
			}
			
			StreamTask.strategy = newStrategy;
			
			synchronized (StreamTask.strategyChangedEvent) {
				StreamTask.strategyChangedEvent.notify();
			}
		}
	}
	
	public synchronized void queueForPlayback(final VideoSegment segment) {
		this.readySegments.offer(segment);
		//bufferContentChanged();
		
		if (this.activeSegment == null) {
			this.scheduleNext();
		}
	}
		
} 
