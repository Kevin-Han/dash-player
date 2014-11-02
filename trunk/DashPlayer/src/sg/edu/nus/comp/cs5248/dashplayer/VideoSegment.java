package sg.edu.nus.comp.cs5248.dashplayer;

import android.util.SparseArray;

public class VideoSegment {
	
	public SparseArray<String> segmentURL;
	private int cacheQuality;
	private String cacheFilePath;
	
	public VideoSegment() { 
		//There are 3 different qualities to store
		segmentURL = new SparseArray<String>(3);
		cacheFilePath = "";
	}
	
	public void setSegmentUrlForQuality (int quality, String url) {
        segmentURL.put(quality, url);
    }    
	
	public String getSegmentUrlForQuality(int quality) {
		return this.segmentURL.get(quality);
	}
	
	public void setCacheInfo(int quality, String cacheFilePath) {
		this.cacheQuality = quality;
		this.cacheFilePath = cacheFilePath;
	}
	
	public String getCacheFilePath() {
		return this.cacheFilePath;
	}
	
	public int getCacheQuality() {
		return this.cacheQuality;
	}
}