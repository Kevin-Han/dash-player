package nus.cs5248.group1.dashplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import nus.cs5248.group1.dashplayer.R;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class VideoListActivity extends FragmentActivity implements VideoListFragment.OnItemSelected
{
	public List<String> list;
	ProgressDialog dialog;
	private static int selectedPosition = -1;
	VideoPlayback vf;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_video_detail);

		dialog = new ProgressDialog(this);
		vf = new VideoPlayback();

		Bundle arguments = new Bundle();
		arguments.putString(VideoPlayback.ARG_ITEM_ID, getIntent().getStringExtra(VideoPlayback.ARG_ITEM_ID));
		vf.setArguments(arguments);
		getSupportFragmentManager().beginTransaction().add(R.id.media_container, vf).commit();

		final ListView listView = (ListView) findViewById(R.id.listMPDView);
		try
		{
			list = new RetrieveMPDTask().execute().get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, list);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
			{
				listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				if((position >= 0) && (position <= adapterView.getChildCount()))
				{
					try
					{
						listView.setItemChecked(position, true);
						listView.setBackgroundColor(Color.TRANSPARENT);
						listView.getChildAt(position).setBackgroundColor(Color.GRAY);

						selectedPosition = position;
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					adapter.notifyDataSetChanged();
				}
			}
		});

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener()
		{
			// The following is what to do when button is clicked:
			@Override
			public void onClick(View v)
			{
				Playback((String) listView.getItemAtPosition(selectedPosition));
			}
		});
	}
	  
	public void Playback(String currentPosition)
	{
		// provide the segment to play here:
		// VideoSegment segment = TO_DO;
		// Send data to Activity
		// listener.onVideoPlaySelected(segment);

		StreamingTask st = new StreamingTask();
		StreamVideoTaskParam stParam = new StreamVideoTaskParam(currentPosition, this, new StreamingTask.OnSegmentDownloaded()
		{

			@Override
			public void segmentDownloaded(VideoSegment segmentInfo, long bandwidthBytePerSec)
			{
				Log.d("test", "Quality=" + segmentInfo.getCacheQuality() + "p, Cache: " + segmentInfo.getCachePath());
				vf.playbackSyncQueue(segmentInfo);
				updateBandwidthText(byteString(bandwidthBytePerSec, true));
			}
		});

		st.execute(stParam);
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
		case R.id.refresh:
			finish();
			startActivity(getIntent());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateBandwidthText(String text)
	{
		VideoPlayback.bandwidthText.setText(text);
	}

	public static String byteString(long bytes, boolean si)
	{
		long unit = si ? 1000 : 1024;
		if (bytes < unit)
			return ((bytes / 1000) + " KB/s");
		if (bytes / unit > unit)
		{
			return String.format("%.1f MB/s", bytes / unit / Math.pow(unit, 1));
		}
		else
		{
			return String.format("%.1f KB/s", bytes / Math.pow(unit, 1));
		}
	}

	@Override
	public void onVideoPlaySelected(VideoSegment segment, long bandwidthBytePerSec)
	{
		VideoPlayback fragment = (VideoPlayback) getSupportFragmentManager().findFragmentById(R.id.detailFragment);
		if (fragment != null && fragment.isInLayout())
		{
			fragment.prepareNextSegment(segment);
		}
	}

	class RetrieveMPDTask extends AsyncTask<Void, Void, List<String>>
	{
		List<String> list;

		@Override
		protected void onPostExecute(List<String> x)
		{
			dialog.dismiss();
		}

		protected List<String> doInBackground(Void... voids)
		{
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost(Server.urlFor(Server.LIST_MPD));
			list = new ArrayList<String>();

			try
			{
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();

				String content = EntityUtils.toString(entity);
				String filename = "";

				for (int i = 0; i < content.length(); i++)
				{
					if (content.charAt(i) != '<')
					{
						filename += content.charAt(i);
					}
					else
					{
						list.add(filename);
						filename = "";
						i += 4;
					}
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return list;
		}

	}

}