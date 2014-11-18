package nus.cs5248.group1.dashplayer;

import android.util.SparseArray;

public class VideoSegment {
	
	public SparseArray<String> segmentURL;
	private int cacheQuality;
	private String cachePath;
	
	public VideoSegment() { 
		//constructor 
		cachePath = "";
		segmentURL = new SparseArray<String>(3);
	}
	
	public void setCache(int quality, String cacheFilePath) {
		this.cacheQuality = quality;
		this.cachePath = cacheFilePath;
	}
	
	public String getCachePath() {
		return this.cachePath;
	}
	
	public int getCacheQuality() {
		return this.cacheQuality;
	}
	
	public void setSegmentUrlForQuality (int quality, String url) {
        segmentURL.put(quality, url);
    }    
	
	public String getSegmentUrlForQuality(int quality) {
		return this.segmentURL.get(quality);
	}
}