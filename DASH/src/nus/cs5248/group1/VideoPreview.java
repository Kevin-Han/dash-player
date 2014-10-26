package nus.cs5248.group1;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class VideoPreview extends Activity {
	private TextView itemPreview;
    private Bundle extras;
    private String item;	
    private String currentSelectedFilePath;
    AlertDialog.Builder dialog;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
        
        extras = getIntent().getExtras();
        item = extras.getString("item");	
        itemPreview = (TextView) findViewById(R.id.itemPreview);
        itemPreview.setText(item);
        dialog = new AlertDialog.Builder(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.upload, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}
	
	public void upload(View view)
	{	
		int result;
		
		CreateVideoUploadTask cv = new CreateVideoUploadTask();
		result = cv.doInBackground(CreateVideoUploadTaskParam.create(item));
		if(result ==  Result.OK)
		{
			dialog.setTitle(item);
			dialog.setMessage("Video was successfully uploaded to server.");
		    dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	// do nothing
		        }
		     });
		    dialog.setIcon(android.R.drawable.ic_dialog_alert);
		    dialog.show();
		}
		else
		{
			dialog.setTitle(item);
		    dialog.setMessage("Video was NOT uploaded to server due to errors.");
		    dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	// do nothing
		        }
		     });
		    dialog.setIcon(android.R.drawable.ic_dialog_alert);
		    dialog.show();			
		}	
	}
	
	public void delete(View view)
	{	
		new AlertDialog.Builder(this)
	    .setTitle(item)
	    .setMessage("Are you sure you want to delete this video?")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	
				File videoFile = new File(Storage.getMediaFolder(true), item);
				currentSelectedFilePath = videoFile.getAbsolutePath();
				Storage.deleteFile(currentSelectedFilePath);
				
				Intent intent = new Intent(VideoPreview.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
	}
}
