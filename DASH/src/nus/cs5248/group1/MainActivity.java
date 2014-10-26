package nus.cs5248.group1;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Color;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch(item.getItemId()) {
		case R.id.menu_record:
	        Intent call = new Intent(this,Record.class); 
	        startActivity(call); 
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
   
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private ListView listView;	
		private String[] fileList;
		
		public PlaceholderFragment() {
		}
	    
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			this.listView = (ListView)rootView.findViewById(R.id.listView1);
			this.listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			if (listView.getAdapter() == null) {
				// Assign adapter to ListView
				fillListView();
			}
			
			listView.setOnItemClickListener(new OnItemClickListener() {
		            @Override
		            public void onItemClick(AdapterView<?> adapterView, View view, int position,
		                    long id) {
		            	
					Intent call = new Intent(getActivity(), VideoPreview.class);
					String item = (String)listView.getItemAtPosition(position);
					call.putExtra("item",item);
					startActivity(call);
		            }
		        });
			
			return rootView;
		}
		
		private void fillListView() {
			fileList = getVideoFileList();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_2, android.R.id.text1,
					fileList);
			listView.setAdapter(adapter);
			listView.setSelection(0);
		}
		
		private String[] getVideoFileList() {
			File mediaStorageDir = Storage.getMediaFolder(true);
			return Storage.getMP4FileList(mediaStorageDir);
		}
	}
}

