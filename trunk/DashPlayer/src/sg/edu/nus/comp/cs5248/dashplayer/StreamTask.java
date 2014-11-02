package sg.edu.nus.comp.cs5248.dashplayer;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

class StreamingProgressInfo {
	public StreamingProgressInfo(VideoSegment segment) {
		this.lastDownloadedSegment = segment;
	}
	
	VideoSegment lastDownloadedSegment;
}

public class StreamTask extends AsyncTask <String, StreamingProgressInfo ,Boolean> {
	//Networking cannot be done at UI Thread, use AsyncTask
	//AsyncTask has some subclasses:
	//1.doInBackground (Params...)
	//2.getPlaylist()
	public static final String TAG = "StreamTask";
	
	//In this class, onSegmentDownloaded will be interface
	private OnSegmentDownloaded streamListener;
	
	Context myContext;
	
	//public static final String CACHE_FOLDER	= new File(Environment.getExternalStorageDirectory().getPath(), "dash_cache/").getPath();
	//Constructor
	public StreamTask () {
		super();
	}
	
	//The interface to be implemented at class that needs this callback:
	public interface OnSegmentDownloaded {
		public void onVideoPlaySelected (VideoSegment segment);
	}
	
	public void setContext (Context context) {
		this.myContext = context;
	}
	
	protected Boolean doInBackground (String... someString) {
		Playlist playlist = this.getPlaylist();
		if (playlist == null) {
			Log.e(TAG, "Fail to get playlist=");
		}
		
		for (VideoSegment segment : playlist) {
			//For now set quality to 1.
			int temp_quality = 240;
			String url = segment.getSegmentUrlForQuality(temp_quality);
			Log.v(TAG, "Next URL: " + url);
			
			long startTime = System.currentTimeMillis();
			String cacheFilePath = downloadFile(url);
			long endTime = System.currentTimeMillis();
			
			if (cacheFilePath != null && !cacheFilePath.isEmpty()) {
				segment.setCacheInfo(temp_quality, cacheFilePath);
				
				long downloadSpeed = 1000 * (new File(cacheFilePath)).length() / (endTime - startTime);
				Log.v(TAG, "Last download speed=" + downloadSpeed);
				
				//After downloading, pass the segment to StreamingProgressInfo.
				publishProgress(new StreamingProgressInfo(segment));
				//this.updateEstimatedBandwidth(downloadSpeed);
				
				//int newQuality = playlist.getQualityForBandwidth(this.estimatedBandwidth);
				//if (newQuality != quality) {
					//Log.i(TAG, "Switching quality from " + quality + "p to " + newQuality + "p");
					//quality = newQuality;
				//}
				
				//publishProgress(new StreamingProgressInfo(this.estimatedBandwidth, segment));
				//actOnDownloadStrategy(endTime - startTime);
			}
			else {
				Log.d(TAG, "Download failed, aborting");
			}
		}
		return true;
	}
	
	protected void onProgressUpdate(StreamingProgressInfo... info) {
        	streamListener.onVideoPlaySelected(info[0].lastDownloadedSegment);
    }
	
	//method getPlaylist
	private Playlist getPlaylist () {
		Playlist myPlaylist = null;
		
		try {
			Log.v(TAG, "getPlaylist is called.");
			HttpClient client = new DefaultHttpClient();
			URI getURL = new URI ("http://www-itec.uni-klu.ac.at/ftp/datasets/mmsys12/BigBuckBunny/MPDs/BigBuckBunny_10s_isoffmain_DIS_23009_1_v_2_1c2_2011_08_30.mpd");
			HttpResponse getResponse = client.execute(new HttpGet(getURL));
			StatusLine statusLine = getResponse.getStatusLine();
			HttpEntity responseEntity = getResponse.getEntity();
		    if(responseEntity != null){
		       // ByteArrayOutputStream out = new ByteArrayOutputStream();
		        //getResponse.getEntity().writeTo(out);
		        //out.close();
		       // String responseString = out.toString();
		        myPlaylist = new Playlist();
		        boolean initSuccess = myPlaylist.initializeWithMPD (EntityUtils.toString(responseEntity));
				if (initSuccess) {
					Log.v(TAG, "Success create a playlist.");
				}
		    } else {
		        //Closes the connection.
		        getResponse.getEntity().getContent().close();
		        throw new IOException(statusLine.getReasonPhrase());
		    }
		} catch (ClientProtocolException e) {
			Log.v (TAG, "Client protocol exception: " + e.getMessage());
		} catch (IOException e) {
			Log.v (TAG, "IO exception: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e (TAG, "Unexpected exception: ",e);
		}
		return myPlaylist;
	}
	
	//Method downloadFile will take in url, return the location of cachedfile.
	private String downloadFile (String url){
		//The local variable:
		File cachefile = null;
		FileOutputStream fos;
		
		try {
			Log.v(TAG, "downloadFile is called.");
			HttpClient client = new DefaultHttpClient();
			HttpResponse getResponse = client.execute(new HttpGet(url));
			HttpEntity responseEntity = getResponse.getEntity();
		    if(responseEntity != null){
		    	cachefile = getCacheFile (myContext, url);
		    	fos = new FileOutputStream (cachefile);
		    	fos.write(EntityUtils.toByteArray(responseEntity));
				fos.close();
		    }
		} catch (ClientProtocolException e) {
			Log.v (TAG, "Client protocol exception: " + e.getMessage());
			return null;
		} catch (IOException e) {
			Log.v (TAG, "IO exception: " + e.getMessage());
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e (TAG, "Unexpected exception: ",e);
			return null;
		} 
		return cachefile.getPath();
	}
	
	public static String extractFileName(String url) {
		return Uri.parse(url).getLastPathSegment();
		//return url.substring(url.lastIndexOf('/') + 1);
	}
	
	public File getCacheFile(Context context, String url) {
		File file = null;
		try {
			String fileName = extractFileName(url);
			file = File.createTempFile(fileName, null, context.getCacheDir());
		}
		catch (IOException e) {
	        // Error while creating file
			e.printStackTrace();
			Log.e (TAG, "Error when creating cache file: ",e);
	    }
		return file;
		//return new File(DashStreamer.CACHE_FOLDER, fileName).getPath();
	}

	
}

