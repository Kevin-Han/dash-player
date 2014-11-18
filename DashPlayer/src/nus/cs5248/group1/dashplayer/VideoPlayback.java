package nus.cs5248.group1.dashplayer;

import java.util.ArrayDeque;
import java.util.Queue;

import sg.edu.nus.comp.cs5248.dashplayer.R;

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

public class VideoPlayback extends Fragment
{
	protected static final String TAG = "VideoPlayback";
	public static final String ARG_ITEM_ID = "item_id";
	private SurfaceHolder currentHolder;
	private SurfaceView currentSurface;
	public static TextView bandwidthText;
	public static TextView bufferText;
	public static TextView statusText;

	final Queue<VideoSegment> readySegments = new ArrayDeque<VideoSegment>();
	VideoSegment activeSegment = null;
	final MediaPlayer mediaPlayer = new MediaPlayer();

	public VideoPlayback()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		View rootView = inflater.inflate(R.layout.activity_video_detail, container, false);

		this.currentSurface = (SurfaceView) rootView.findViewById(R.id.videoView1);
		this.currentHolder = this.currentSurface.getHolder();
		VideoPlayback.bandwidthText = (TextView) rootView.findViewById(R.id.bandwidth_text);
		VideoPlayback.bufferText = (TextView) rootView.findViewById(R.id.buffer_text);
		VideoPlayback.statusText = (TextView) rootView.findViewById(R.id.status_text);

		return rootView;
	}

	// Try out:
	public void prepareNextSegment(final VideoSegment segment)
	{
		Log.i(TAG, "Preparing segment: " + segment.getCachePath());

		String st = segment.getCachePath();

		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mediaPlayer)
			{
				// MediaController mediaController = new
				// MediaController(mContext);
				if (mediaPlayer != null)
				{
					mediaPlayer.stop();
					mediaPlayer.reset();
					gotoNextSegment();
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

	private synchronized void gotoNextSegment()
	{
		this.activeSegment = this.readySegments.poll();

		int count = this.readySegments.size();

		if (count > 16)
		{
			setStrategy(StreamingTask.HALT);
		}
		else if (count > 8)
		{
			setStrategy(StreamingTask.MIDSPEED);
		}
		else
		{
			setStrategy(StreamingTask.FASTEST);
		}

		if (this.readySegments != null)
		{
			VideoPlayback.bufferText.setText(Integer.toString(this.readySegments.size()));
		}
		else
		{
			VideoPlayback.bufferText.setText("0");
		}

		if (this.activeSegment != null)
		{
			prepareNextSegment(this.activeSegment);
		}
		else
		{
			Log.i(TAG, "Buffer runs out, downloading speed is slow");
		}
	}

	public synchronized void playbackSyncQueue(final VideoSegment segment)
	{
		this.readySegments.offer(segment);

		if (this.activeSegment == null)
		{
			this.gotoNextSegment();
		}
	}

	public synchronized void setStrategy(final int newStrategy)
	{
		if (StreamingTask.strategy != newStrategy)
		{
			if (newStrategy != StreamingTask.HALT && newStrategy != StreamingTask.FASTEST && newStrategy != StreamingTask.MIDSPEED)
			{
				throw new RuntimeException("Invalid strategy: " + newStrategy);
			}

			StreamingTask.strategy = newStrategy;

			synchronized (StreamingTask.strategyChangedEvent)
			{
				StreamingTask.strategyChangedEvent.notify();
			}
		}
	}

}
