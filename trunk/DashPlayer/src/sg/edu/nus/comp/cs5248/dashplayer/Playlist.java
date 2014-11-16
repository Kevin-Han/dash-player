package sg.edu.nus.comp.cs5248.dashplayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;
import android.util.Log;
import java.io.StringReader;
import java.io.IOException;

public class Playlist implements Iterable<VideoSegment> {
	public List<VideoSegment> videoSegments;
	public List<QualitySpec> qualities;
	String duration;
	String minBufferTime;
	
	public static final String TAG = "Playlist";
	static final String MPD_TAG = "MPD";
	static final String MEDIA_PRESENTATION_DURATION = "mediaPresentationDuration";
	static final String MIN_BUFFER_TIME = "minBufferTime";
	static final String BASE_URL = "BaseURL";
	static final String REPRESENTATION = "Representation";
	static final String WIDTH = "width";
	static final String HEIGHT = "height";
	static final String BANDWIDTH = "bandwidth";
	static final String INIT = "Initialization";
	static final String SOURCE_URL = "sourceURL";
	static final String MIMETYPE = "mimeType";
	static final String SEGMENT_LIST = "SegmentList";
	static final String DURATION = "duration";
	static final String SEGMENT_URL = "SegmentURL";
	static final String MEDIA_FILE = "media";
	
	//Constructor:
	public Playlist() {
		videoSegments = new ArrayList<VideoSegment>();
		qualities = new ArrayList<QualitySpec>();
		duration = "PT0S";
		minBufferTime = "PT0S";
	}
	
	//Nested class QualitySpec an array list of quality and the respective bandwidth.
	protected static class QualitySpec implements Comparable<QualitySpec> {
		int verticalResolution;
		int requiredBandwidth;
		
		public QualitySpec(final int verticalQuality, final int requiredBandwidth) {
			this.verticalResolution = verticalQuality;
			this.requiredBandwidth = requiredBandwidth;
		}
		
		@Override
		public int compareTo(QualitySpec rhs) {
			if (this == rhs) return 0;
			return (Integer.valueOf(this.verticalResolution)).compareTo(Integer.valueOf(rhs.verticalResolution));
		}
	}
	
	public void addSegmentSource(int index, int quality, String sourceURL) {
		VideoSegment segment = null;
		
		if (index >= videoSegments.size()) {
			segment = new VideoSegment();
			videoSegments.add(segment);
		} else {
			segment = videoSegments.get(index);
		}
		
		segment.setSegmentUrlForQuality(quality, sourceURL);
	
	}
	
	public boolean initializeWithMPD (String mpd) {
		try {
			String url = null;
			int segmentIndex=0;
			int quality = 0;
			
			//XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        //factory.setNamespaceAware(true);
	        XmlPullParser xpp = Xml.newPullParser();
	        xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
	        xpp.setInput( new StringReader (mpd) );
	        int eventType = xpp.getEventType();
	        
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	        	switch (eventType) {
	        	case (XmlPullParser.START_DOCUMENT):
	        		break;
	        	case (XmlPullParser.START_TAG):
	        		String nameOfTag = xpp.getName();
	        	
	        		if (nameOfTag.equalsIgnoreCase(Playlist.MPD_TAG)) {
	        			this.duration = xpp.getAttributeValue(null, Playlist.MEDIA_PRESENTATION_DURATION);
	        			this.minBufferTime = xpp.getAttributeValue(null, Playlist.MIN_BUFFER_TIME);
	        			Log.v(TAG, "MPD Duration=" + this.duration + "minBufferTime=" + this.minBufferTime);
	        		} 
	        		else if (nameOfTag.equalsIgnoreCase(Playlist.BASE_URL)) {
	        			eventType = xpp.next();
	        			url = xpp.getText();
	        			Log.v(TAG, "BaseURL="+url);
	        		}
	        		else if (nameOfTag.equalsIgnoreCase(Playlist.REPRESENTATION)) {
	        			if(xpp.getAttributeValue(null,Playlist.MIMETYPE).equalsIgnoreCase("video/mp4"))
	        			{
		        			String width = xpp.getAttributeValue(null, Playlist.WIDTH);
		        			String height = xpp.getAttributeValue(null, Playlist.HEIGHT );
		        			String bandwidth = xpp.getAttributeValue(null, Playlist.BANDWIDTH);
		        			quality = Integer.parseInt(height);
							this.qualities.add(new QualitySpec(quality, Integer.parseInt(bandwidth)));
		        			Log.v(TAG, "Width=" + width + "Height=" + height + "Bandwidth=" + bandwidth);
		        			Log.v(TAG, "Quality is" + quality + "Bandwidth is=" + Integer.parseInt(bandwidth));
	        			}
	        			if(xpp.getAttributeValue(null,Playlist.MIMETYPE).equalsIgnoreCase("audio/mp4"))
	        			{
		        			String width = xpp.getAttributeValue(null, Playlist.WIDTH);
		        			String height = xpp.getAttributeValue(null, Playlist.HEIGHT );
		        			String bandwidth = xpp.getAttributeValue(null, Playlist.BANDWIDTH);
		        			quality = Integer.parseInt(height);
							this.qualities.add(new QualitySpec(quality, Integer.parseInt(bandwidth)));
		        			Log.v(TAG, "Width=" + width + "Height=" + height + "Bandwidth=" + bandwidth);
		        			Log.v(TAG, "Quality is" + quality + "Bandwidth is=" + Integer.parseInt(bandwidth));
	        			}
	        		}
	        		else if (nameOfTag.equalsIgnoreCase(Playlist.INIT)) {
						String segmentBaseURI = url;
						segmentBaseURI = segmentBaseURI.concat(xpp.getAttributeValue(null, Playlist.SOURCE_URL));
	        			Log.v(TAG, "Initialization segment URI=" + segmentBaseURI);
	        		}
	        		else if (nameOfTag.equalsIgnoreCase(Playlist.SEGMENT_LIST)) {
	        			String duration = xpp.getAttributeValue(null, Playlist.DURATION);
	        			segmentIndex = 0;
	        			Log.v(TAG, "duration of segment list=" + duration + "segment index=" + segmentIndex);
	        		}
	        		else if (nameOfTag.equalsIgnoreCase(Playlist.SEGMENT_URL)) {
	        			String segmentURI = url;
	        			segmentURI = segmentURI.concat(xpp.getAttributeValue(null, Playlist.MEDIA_FILE));
	        			this.addSegmentSource(segmentIndex++, quality, segmentURI);
	        			Log.v(TAG, "Segment URI is" + segmentURI );
	        		}
	        		break;
	        	}
	        	eventType = xpp.next();
	        }
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Parse exception: " + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.e(TAG, "IO exception: " + e.getMessage());
			return false;
		} catch (NumberFormatException e) {
			Log.e(TAG, "Malformed response: " + e.getMessage());
			return false;
		}   
		return true;
	}
	
	@Override
	public Iterator<VideoSegment> iterator() {
		return this.videoSegments.iterator();
	}
	
}

