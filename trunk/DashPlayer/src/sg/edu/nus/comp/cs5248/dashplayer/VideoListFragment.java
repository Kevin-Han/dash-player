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

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class VideoListFragment extends Fragment {
		private OnItemSelected listener;
		
		//Interface to be implemented on VideoListActivity
		public interface OnItemSelected {
			public void onVideoPlaySelected (VideoSegment segment);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
		  
			View view = inflater.inflate(R.layout.activity_video_list,
					container, false);
			return view;
		}

	/*
	  private OnItemSelectedListener listener;
	  
	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {
		  
		  View view = inflater.inflate(R.layout.activity_video_list,
	        container, false);
		  Button button = (Button) view.findViewById(R.id.button1);
		  button.setOnClickListener(new View.OnClickListener() {
			  //The following is what to do when button is clicked:
			  @Override
			  public void onClick(View v) {
				  updateDetail();
			  }
		  });
		  return view;
	  }

	  public interface OnItemSelectedListener {
		  public void onRssItemSelected(String link);
	  }
	  
	  @Override
	    public void onAttach(Activity activity) {
	      super.onAttach(activity);
	      if (activity instanceof OnItemSelectedListener) {
	        listener = (OnItemSelectedListener) activity;
	      } else {
	        throw new ClassCastException(activity.toString()
	            + " must implemenet MyListFragment.OnItemSelectedListener");
	      }
	    }
	  
	  
	  // May also be triggered from the Activity
	  public void updateDetail() {
	    // create fake data
	    String newTime = String.valueOf(System.currentTimeMillis());
	    // Send data to Activity
	    listener.onRssItemSelected(newTime);
	  }
	  */
} 
