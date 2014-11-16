package sg.edu.nus.comp.cs5248.dashplayer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayDeque;
import java.util.Queue;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

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
	public static final String ARG_ITEM_ID = "item_id";
	private RelativeLayout	mediaContainer;
    private SurfaceHolder	currentHolder;
    private SurfaceView		currentSurface;
    
    final Queue<VideoSegment> readySegments = new ArrayDeque<VideoSegment>();
    VideoSegment activeSegment = null;
    final MediaPlayer mediaPlayer = new MediaPlayer();
	
	public VideoDetailFragment () {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
		
		View rootView 		= inflater.inflate(R.layout.activity_video_detail, container, false);
		//this.mediaContainer	= (RelativeLayout) rootView.findViewById(R.id.media_container);
		
    	this.currentSurface = (SurfaceView) rootView.findViewById(R.id.videoView1);
        this.currentHolder  = this.currentSurface.getHolder();
        //mediaContainer.addView(currentSurface, 0);
        
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
