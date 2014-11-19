package nus.cs5248.group1.dashplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

class StreamingProgressInfo
{
	long bandwidthBytePerSec;
	VideoSegment lastDownloadedSegment;

	public StreamingProgressInfo(long bandwidth, VideoSegment segment)
	{
		this.lastDownloadedSegment = segment;
		this.bandwidthBytePerSec = bandwidth;
	}
}

class StreamVideoTaskParam
{
	public StreamVideoTaskParam(String title, Context context, StreamingTask.OnSegmentDownloaded callback)
	{
		this.title = title;
		this.context = context;
		this.callback = callback;
	}

	String title;
	Context context;
	StreamingTask.OnSegmentDownloaded callback;
}

public class StreamingTask extends AsyncTask<StreamVideoTaskParam, StreamingProgressInfo, Boolean>
{
	// Networking cannot be done at UI Thread, use AsyncTask
	// AsyncTask has some subclasses:
	// 1.doInBackground (Params...)
	// 2.getPlaylist()
	public static final String TAG = "StreamingTask";
	private StreamingTask.OnSegmentDownloaded callback;
	private String title;
	private Context context;
	File projectDir;
	private long ulEstimatBandwidth;

	// strategies
	public static final int HALT = 0;
	public static final int FASTEST = 1;
	public static final int MIDSPEED = 2;

	private static final int HIGH_BANDWIDTH = 300000; // 2.4Mbps = 300 KB/S
	private static final int MEDIUM_BANDWIDTH = 120000; // 960kbps = 120 KB/S
	private static final int LOW_BANDWIDTH = 30000; // 240kbps = 30 KB/S
	
	public static final int segHaltLimit = 4; 
	public static final int segMidspeedLimit = 2; 
	
	public static Object strategyChangedEvent = new Object();
	public static int strategy = FASTEST;

	public static String CACHE_FOLDER;

	// Constructor
	public StreamingTask()
	{
		super();
		File x = new File(Environment.getExternalStorageDirectory().getPath(), "dash_cache/");
		CACHE_FOLDER = x.getPath();
		if (!x.exists())
		{
			x.mkdir();
		}
	}

	public static interface OnSegmentDownloaded
	{
		public void segmentDownloaded(VideoSegment segment, long bandwidthBytePerSec);
	}

	protected Boolean doInBackground(StreamVideoTaskParam... params)
	{
		this.callback = params[0].callback;
		this.title = params[0].title;
		this.context = params[0].context;

		Playlist playlist = this.getPlaylist();

		if (playlist == null)
		{
			Log.e(TAG, "Fail to get playlist");
			return false;
		}

		int quality = playlist.getQualityForBandwidth(getEstimatedBandwidth(this.context));
		this.ulEstimatBandwidth = 0; // 0 will be treated as uninitialized
		for (VideoSegment segment : playlist)
		{
			// For now set quality to 1.
			// int temp_quality = 480;
			String url = segment.getSegmentUrlForQuality(quality);
			Log.v(TAG, "Next URL: " + url);

			long startTime = System.currentTimeMillis();
			String cacheFilePath = downloadFile(url);
			long endTime = System.currentTimeMillis();

			if (cacheFilePath != null && !cacheFilePath.isEmpty())
			{
				segment.setCache(quality, cacheFilePath);

				long downloadSpeed = 1000 * (new File(cacheFilePath)).length() / (endTime - startTime);
				Log.v(TAG, "Last download speed =" + downloadSpeed + " B/S");

				// After downloading, pass the segment to StreamingProgressInfo.
				// publishProgress(new StreamingProgressInfo(segment));
				this.updateEstimatedBandwidth(downloadSpeed);

				int newQuality = playlist.getQualityForBandwidth(this.ulEstimatBandwidth);
				if (newQuality != quality)
				{
					Log.i(TAG, "Switching quality from " + quality + "p to " + newQuality + "p");
					quality = newQuality;
				}

				publishProgress(new StreamingProgressInfo(this.ulEstimatBandwidth, segment));
				actOnDownloadStrategy(endTime - startTime);
			}
			else
			{
				Log.d(TAG, "Download failed, aborting");
			}
		}
		return true;
	}

	protected void onProgressUpdate(StreamingProgressInfo... info)
	{
		callback.segmentDownloaded(info[0].lastDownloadedSegment, info[0].bandwidthBytePerSec);
	}

	// method getPlaylist
	private Playlist getPlaylist()
	{
		Playlist myPlaylist = null;

		try
		{
			Log.v(TAG, "getPlaylist is called.");
			HttpClient client = new DefaultHttpClient();
			URI getURL = new URI(Server.urlFor(Server.SEGMENT_BASE) + this.title);
			HttpResponse getResponse = client.execute(new HttpGet(getURL));
			StatusLine statusLine = getResponse.getStatusLine();
			HttpEntity responseEntity = getResponse.getEntity();
			if (responseEntity != null)
			{
				// ByteArrayOutputStream out = new ByteArrayOutputStream();
				// getResponse.getEntity().writeTo(out);
				// out.close();
				// String responseString = out.toString();
				myPlaylist = new Playlist();
				boolean initSuccess = myPlaylist.initializeWithMPD(EntityUtils.toString(responseEntity));
				if (initSuccess)
				{
					Log.v(TAG, "Success create a playlist.");
				}
			}
			else
			{
				// Closes the connection.
				getResponse.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		}
		catch (ClientProtocolException e)
		{
			Log.v(TAG, "Client protocol exception: " + e.getMessage());
		}
		catch (IOException e)
		{
			Log.v(TAG, "IO exception: " + e.getMessage());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e(TAG, "Unexpected exception: ", e);
		}
		return myPlaylist;
	}

	// Method downloadFile will take in url, return the location of cachedfile.
	private String downloadFile(String url)
	{
		// The local variable:
		String cachefile = "";
		FileOutputStream fos;

		try
		{
			Log.v(TAG, "downloadFile is called.");
			HttpClient client = new DefaultHttpClient();
			HttpResponse getResponse = client.execute(new HttpGet(url));
			HttpEntity responseEntity = getResponse.getEntity();
			if (responseEntity != null)
			{
				cachefile = getCacheFile(context, url);
				fos = new FileOutputStream(cachefile);
				fos.write(EntityUtils.toByteArray(responseEntity));
				fos.close();
			}
		}
		catch (ClientProtocolException e)
		{
			Log.v(TAG, "Client protocol exception: " + e.getMessage());
			return null;
		}
		catch (IOException e)
		{
			Log.v(TAG, "IO exception: " + e.getMessage());
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e(TAG, "Unexpected exception: ", e);
			return null;
		}
		return cachefile;
	}

	public static String extractFileName(String url)
	{
		// return Uri.parse(url).getLastPathSegment();
		return url.substring(url.lastIndexOf('/') + 1);
	}

	public String getCacheFile(Context context, String url)
	{
		String fileName = extractFileName(url);
		return new File(StreamingTask.CACHE_FOLDER, fileName).getPath();
	}

	public static int getEstimatedBandwidth(Context context)
	{
		int estimatedBandwidth = 0;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();

		if (info == null || !info.isConnected())
		{
			Log.d(TAG, "No network connection");
			estimatedBandwidth = 0;
		}
		else if (info.getType() == ConnectivityManager.TYPE_WIFI)
		{
			WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			int linkSpeedMbps = wm.getConnectionInfo().getLinkSpeed();

			Log.d(TAG, "WiFi link speed is " + linkSpeedMbps + " Mb/s)");

			if (linkSpeedMbps/8 >= HIGH_BANDWIDTH)
			{
				estimatedBandwidth = HIGH_BANDWIDTH;
			}
			else if (linkSpeedMbps/8 >= MEDIUM_BANDWIDTH)
			{
				estimatedBandwidth = MEDIUM_BANDWIDTH;
			}
			else if(linkSpeedMbps/8 >= LOW_BANDWIDTH)
			{
				estimatedBandwidth = LOW_BANDWIDTH;
			}
			else
			{
				estimatedBandwidth = 0;
			}
		}        
		else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
		{
			Log.d(TAG, "Connection type is Mobile (" + info.getSubtypeName() + ")");
			switch (info.getSubtype())
			{
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
			case TelephonyManager.NETWORK_TYPE_LTE:
				estimatedBandwidth = HIGH_BANDWIDTH;
				break;

			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
				estimatedBandwidth = MEDIUM_BANDWIDTH;
				break;

			default:
				estimatedBandwidth = LOW_BANDWIDTH;
				break;
			}
		}

		return estimatedBandwidth;
	}

	private void updateEstimatedBandwidth(final long lastDownloadBandwidth)
	{
		if (this.ulEstimatBandwidth == 0)
		{
			this.ulEstimatBandwidth = lastDownloadBandwidth;
		}
		else
		{
			this.ulEstimatBandwidth = (long) (0.5 * this.ulEstimatBandwidth + 0.5 * lastDownloadBandwidth);
		}
	}

	private void actOnDownloadStrategy(long lastDownloadTime)
	{
		while (StreamingTask.strategy == StreamingTask.HALT)
		{
			try
			{
				Log.d(TAG, "HALT. Waiting for strategy change event.");
				synchronized (StreamingTask.strategyChangedEvent)
				{
					StreamingTask.strategyChangedEvent.wait();
				}
			}
			catch (InterruptedException e)
			{
				Log.e(TAG, "Interrupted while on HALT mode.");
			}
		}

		if (StreamingTask.strategy == StreamingTask.MIDSPEED)
		{
			long sleepTime = 4000 - lastDownloadTime;
			if (sleepTime > 0)
			{
				try
				{
					Log.d(TAG, "Sleeping for " + sleepTime + " ms before next download.");
					Thread.sleep(sleepTime);
				}
				catch (InterruptedException e)
				{
					Log.e(TAG, "Interrupted while on AT_LEAST_THREE_SECONDS mode.");
				}
			}
		}
	}
}
