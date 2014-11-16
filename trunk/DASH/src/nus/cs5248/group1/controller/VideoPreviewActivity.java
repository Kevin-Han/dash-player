package nus.cs5248.group1.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import nus.cs5248.group1.R;
import nus.cs5248.group1.model.CustomMultiPartEntity;
import nus.cs5248.group1.model.ProgressListener;
import nus.cs5248.group1.model.Result;
import nus.cs5248.group1.model.SegmentVideoUtils;
import nus.cs5248.group1.model.Server;
import nus.cs5248.group1.model.Storage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import java.util.concurrent.Executor;

public class VideoPreviewActivity extends Activity
{
	private static final String TAG = "VideoPreviewActivity.class";
	private static final double MAX_SEGMENT_LIMIT = 3.0000;
	private static final long PLAY_TIMEOUT = 3000;
	private TextView itemPreview;
	private SurfaceView mediaPreview;
	private SurfaceHolder holder;
	private Bundle extras;
	private String item;
	private String currentSelectedFilePath;
	protected Context mContext;
	Button btnUpload;
	AlertDialog.Builder dialog;
	Executor executor;
	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);

		extras = getIntent().getExtras();
		item = extras.getString("item");
		itemPreview = (TextView) findViewById(R.id.itemPreview);
		mediaPreview = (SurfaceView) findViewById(R.id.videoView1);
		holder = mediaPreview.getHolder();
		itemPreview.setText(item);
		dialog = new AlertDialog.Builder(this);
		mContext = getApplicationContext();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_record:
			Intent call = new Intent(this, RecordActivity.class);
			startActivity(call);
			return true;
		case R.id.menu_list_videos_from_server:
			Intent callServer = new Intent(this, ListServerVideoActivity.class);
			startActivity(callServer);
			return true;
		case R.id.menu_list_local_video:
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void upload(View v)
	{
		final CreateVideoUploadTask cv = new CreateVideoUploadTask();

		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("Uploading ...");
		pd.setIndeterminate(true);
		pd.setCancelable(false);
		pd.setProgress(0);
		pd.setMax(100);
		pd.show();

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					cv.execute(CreateVideoUploadTaskParam.create(item)).get();
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				catch (ExecutionException e)
				{
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
			}
		}).start();

	}

	public void delete(View view)
	{
		new AlertDialog.Builder(this).setTitle(item).setMessage("Are you sure you want to delete this video?").setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				File videoFile = new File(Storage.getMediaFolder(true), item);
				currentSelectedFilePath = videoFile.getAbsolutePath();
				Storage.deleteFile(currentSelectedFilePath);

				Intent intent = new Intent(VideoPreviewActivity.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				// do nothing
			}
		}).setIcon(android.R.drawable.ic_dialog_alert).show();
	}

	public void play(View view)
	{
		File videoFile = new File(Storage.getMediaFolder(true), item);
		currentSelectedFilePath = videoFile.getAbsolutePath();
		MediaPlayer player = new MediaPlayer();

		player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mediaPlayer)
			{
				//MediaController mediaController = new MediaController(mContext);
				if(mediaPlayer != null)
				{
					mediaPlayer.stop();
					mediaPlayer.release();
					mediaPlayer = null;
					//mediaController.setVisibility(View.GONE);
					//mediaController.setAnchorView(mediaPreview);
				}
			}
		});
		
		player.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
		{
			@Override
			public void onPrepared(MediaPlayer mediaPlayer)
			{
				mediaPlayer.start();
			}
		});

		try
		{
			player.setDataSource(currentSelectedFilePath);
			player.setSurface(holder.getSurface());
			player.setDisplay(holder);	
			player.prepare();

			// nextMediaPlayer.setOnPreparedListener(new
			// MediaPlayer.OnPreparedListener() {
			// @Override
			// public void onPrepared(MediaPlayer mediaPlayer) {
			// mediaPlayer.start();
			// mediaPlayer.pause();
			// }
			// });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static class CreateVideoUploadTaskParam
	{
		String video;

		static CreateVideoUploadTaskParam create(String video)
		{
			CreateVideoUploadTaskParam param = new CreateVideoUploadTaskParam();
			param.video = video;
			return param;
		}
	}

	private class CreateVideoUploadTask extends AsyncTask<CreateVideoUploadTaskParam, Integer, Integer>
	{
		Server server;
		public DefaultHttpClient client;
		protected String responseAsText;
		protected List<Cookie> cookies;
		private Integer result;
		private long totalsize;

		protected Integer doInBackground(CreateVideoUploadTaskParam... param)
		{
			server = new Server();
			client = server.BuidlConnection();
			HttpContext httpContext = new BasicHttpContext();

			try
			{
				cookies = client.getCookieStore().getCookies();
				HttpPost httppost = new HttpPost(Server.urlFor(Server.CREATE_VIDEO));

				// MultipartEntity entity = new
				// MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				CustomMultiPartEntity entity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, new ProgressListener()
				{
					@Override
					public void transferred(long num)
					{
						publishProgress((int) ((num / (float) totalsize) * 100));
					}
				});

				// File file = new File(Storage.getMediaFolder(true),
				// param[0].video);

				String fileName = param[0].video;
				String[] fileList = segmentService(fileName);
				File file;

				for (String x : fileList)
				{
					if (x != null)
					{
						file = new File(Storage.getMediaFolder(true), x);
						entity.addPart("async-upload", new FileBody(file));
						totalsize = entity.getContentLength();

						httppost.setEntity(entity);
						HttpResponse response = client.execute(httppost, httpContext);

						HttpEntity resEntity = response.getEntity();

						if (resEntity != null)
						{
							this.responseAsText = EntityUtils.toString(resEntity);
							result = Result.OK;
						}
					}
				}
			}
			catch (UnsupportedEncodingException e)
			{
				Log.e(Server.TAG, "Unsupported encoding exception: " + e.getMessage());
				result = Result.FAIL;
			}
			catch (ClientProtocolException e)
			{
				Log.e(Server.TAG, "Client protocol exception: " + e.getMessage());
				result = Result.FAIL;
			}
			catch (IOException e)
			{
				Log.e(Server.TAG, "IO exception: " + e.getMessage());
				result = Result.FAIL;
			}
			catch (ParseException e)
			{
				Log.e(Server.TAG, "JSON parse exception: " + e.getMessage());
				result = Result.FAIL;
			}
			catch (Exception e)
			{
				Log.e(Server.TAG, "Unexpected exception: " + e.getMessage());
				e.printStackTrace();
				result = Result.FAIL;
			}
			return result;
		}

		@Override
		protected void onProgressUpdate(Integer... progress)
		{
			super.onProgressUpdate(progress);
			pd.setProgress((int) (progress[0]));
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			super.onPostExecute(result);
			pd.dismiss();
			
			if (result == null)
			{
				dialog.setTitle(item);
				dialog.setMessage("Video was NOT uploaded to server due to errors.");
				dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						finish();
					}
				});
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.show();
			}
			else
			{
				if (result == Result.OK)
				{
					dialog.setTitle(item);
					dialog.setMessage("Video was successfully uploaded to server.");
					dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							Intent intent = new Intent(VideoPreviewActivity.this, MainActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					});
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.show();
				}
				else
				{
					dialog.setTitle(item);
					dialog.setMessage("Video was NOT uploaded to server due to errors.");
					dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							// do nothing
						}
					});
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.show();
				}			
			}
		}

		private int getVideoFileDuration(String filename)
		{
			int duration = 0;
			try
			{
				MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(filename));
				duration = mp.getDuration();
				mp.reset();
				mp.release();
				mp = null;
			}
			catch (IllegalStateException e)
			{
				Log.e(TAG, e.getMessage().toString());
			}
			return duration;
		}

		private String[] segmentService(String filename)
		{

			String filePath = new File(Storage.getMediaFolder(true), item).getPath();

			int duration = getVideoFileDuration(filePath);

			String[] segmentList = null;

			if (duration > 0)
			{

				double seconds = duration / 1000; // convert milliseconds to
													// seconds
				int numOfSegments = (int) seconds / 3;
				double remainTime = (double)(duration % 3000) / (double) 1000;
				numOfSegments = (remainTime > 0) ? numOfSegments + 1 : numOfSegments;

				segmentList = new String[numOfSegments];

				double startIndex = 0.0000;
				double endIndex = MAX_SEGMENT_LIMIT;
				int index = 0;
				Log.e(TAG, "remainder : " + remainTime);
				try
				{
					for (int i = 0; i < segmentList.length; i++)
					{
						index = i + 1;
						if (i == segmentList.length - 1 && remainTime > 0)
						{ // last segment and less than 3 seconds
							segmentList[i] = SegmentVideoUtils.startTrim(filePath, Storage.getMediaFolder(true), startIndex, startIndex + remainTime, index);
							break; // exit for loop
						}

						segmentList[i] = SegmentVideoUtils.startTrim(filePath, Storage.getMediaFolder(true), startIndex, endIndex, index);
						startIndex = endIndex;
						endIndex += MAX_SEGMENT_LIMIT;
					}
				}
				catch (IOException e)
				{
					Log.e(TAG, e.getMessage().toString());
				}
			}
			return segmentList;
		}
	}
}