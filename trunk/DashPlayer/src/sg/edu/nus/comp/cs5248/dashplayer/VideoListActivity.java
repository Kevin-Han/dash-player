package sg.edu.nus.comp.cs5248.dashplayer;
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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class VideoListActivity extends FragmentActivity implements VideoListFragment.OnItemSelected {
	public List<String> list;
	ProgressDialog dialog;
	private static int save = -1;
	VideoDetailFragment vf;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_detail);
       		
		dialog = new ProgressDialog(this);
		vf = new VideoDetailFragment();
		
		Bundle arguments = new Bundle();
		arguments.putString(VideoDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(VideoDetailFragment.ARG_ITEM_ID));
		vf.setArguments(arguments);
		getSupportFragmentManager().beginTransaction().add(R.id.media_container, vf).commit();
		
        final ListView listView = (ListView)findViewById(R.id.listMPDView);
		try
		{
			list = new RetrieveMPDTask().execute().get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_2, android.R.id.text1,
				list);
		listView.setAdapter(adapter);
		listView.setSelection(0);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
			{
        		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				listView.setItemChecked(position, true);
				listView.getChildAt(position).setBackgroundColor(Color.GRAY);
			    if (save != -1 && save != position){
			    	listView.getChildAt(save).setBackgroundColor(Color.TRANSPARENT);
			    }

			    save = position;                

				adapter.notifyDataSetChanged();           
			}
		});
		
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			//The following is what to do when button is clicked:
			@Override
			public void onClick(View v) {	            
				Playback((String)listView.getItemAtPosition(save));
			}
		});
    }
    
	public void Playback(String currentPosition) {
	    // provide the segment to play here:
	    //VideoSegment segment = TO_DO;
	    // Send data to Activity
	    //listener.onVideoPlaySelected(segment);
		
		StreamTask st= new StreamTask();
		StreamVideoTaskParam stParam = new StreamVideoTaskParam(currentPosition, this, new StreamTask.OnSegmentDownloaded(){
			
			@Override
			public void onVideoPlaySelected(VideoSegment segmentInfo) {
				Log.d("test", "Quality=" + segmentInfo.getCacheQuality() + "p, Cache: " + segmentInfo.getCacheFilePath());
				vf.queueForPlayback(segmentInfo);
			}
		});
		
		st.execute(stParam);
	}
    
    @Override
    public void  onVideoPlaySelected (VideoSegment segment) {
    	VideoDetailFragment fragment = (VideoDetailFragment) getSupportFragmentManager()
    		.findFragmentById(R.id.detailFragment);
        if (fragment != null && fragment.isInLayout()) {
        	fragment.prepareNextPlayer(segment);
        } 
    }
    
    class RetrieveMPDTask extends AsyncTask<Void, Void, List<String>> {
    	List<String> list;

    	@Override
    	protected void onPostExecute(List<String> x)
    	{
    		dialog.dismiss();
    	}
    	
        protected List<String> doInBackground(Void...voids) {
    		HttpClient client = new DefaultHttpClient();
    		HttpPost request = new HttpPost(Server.urlFor(Server.LIST_MPD));
    		list = new ArrayList<String>();
    		
            try {
                HttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();

                String content = EntityUtils.toString(entity);
                String filename = "";

                for(int i=0;i<content.length();i++)
                {
                	if(content.charAt(i) != '<')
                	{
                		filename += content.charAt(i);
                	}
                	else
                	{
                		list.add(filename);
                		filename = "";
                		i+=4;
                	}
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
    		return list;
        }
/*
    // if the wizard generated an onCreateOptionsMenu you can delete
    // it, not needed for this tutorial

    @Override
    public void onRssItemSelected(String link) {
    	VideoDetailFragment fragment = (VideoDetailFragment) getSupportFragmentManager()
    		.findFragmentById(R.id.detailFragment);
        if (fragment != null && fragment.isInLayout()) {
        	fragment.setText(link);
        } 
    }
*/
} 


}