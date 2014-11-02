package sg.edu.nus.comp.cs5248.dashplayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class VideoListActivity extends FragmentActivity implements VideoListFragment.OnItemSelected {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_detail);
    }
    
    @Override
    public void  onVideoPlaySelected (VideoSegment segment) {
    	VideoDetailFragment fragment = (VideoDetailFragment) getSupportFragmentManager()
    		.findFragmentById(R.id.detailFragment);
        if (fragment != null && fragment.isInLayout()) {
        	fragment.prepareNextPlayer(segment);
        } 
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
