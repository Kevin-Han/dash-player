package nus.cs5248.group1.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import nus.cs5248.group1.R;
import nus.cs5248.group1.controller.MainActivity.PlaceholderFragment;
import nus.cs5248.group1.model.Server;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Color;

public class ListServerVideoActivity extends Activity
{
	private ListView listView;
	private List<String> list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_servervideo);
		listView = (ListView) findViewById(R.id.listView1);
		
		try
		{
			list = new RetrieveWebTask().execute().get();
		}
		catch (InterruptedException | ExecutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_2, android.R.id.text1,
				list);
		listView.setAdapter(adapter);
		listView.setSelection(0);
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
		switch(item.getItemId()) {
		case R.id.menu_record:
	        Intent call = new Intent(this,RecordActivity.class); 
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
}

class RetrieveWebTask extends AsyncTask<Void, Void, List<String>> {
	List<String> list;
	
    protected List<String> doInBackground(Void...voids) {
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(Server.urlFor(Server.LIST_VIDEO));
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
}

