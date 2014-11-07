package nus.cs5248.group1.model;

import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
	/**
	 * Shortens/Crops a track
	 */
	public class SegmentVideoUtils {
		private static final String TAG = "SegmentVideoUtils";
		
	    public static String startTrim(String src, File dst, double startMs, double endMs, int index) throws IOException {
	    	
	    	String segFileName = null;
	    	Movie movie = MovieCreator.build(src);
	    	
	        // remove all tracks we will create new tracks from the old
	        List<Track> tracks = movie.getTracks();
	        movie.setTracks(new LinkedList<Track>());
	 
	        double startTime = startMs;
	        double endTime = endMs;
	        
	        boolean timeCorrected = false;
	        
	        for (Track track : tracks) {
	            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
	                if (timeCorrected) {
	                    // This exception here could be a false positive in case we have multiple tracks
	                    // with sync samples at exactly the same positions. E.g. a single movie containing
	                    // multiple qualities of the same video (Microsoft Smooth Streaming file)
	                	Log.e(TAG,
	                            "The startTime has already been corrected by another track with SyncSample. Not Supported.");
	                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
	                }
	                startTime = correctTimeToSyncSample(track, startTime, false);
	                endTime = correctTimeToSyncSample(track, endTime, true);
	                
	                timeCorrected = true;
	            }
	        }
	        
	        if (startTime == endTime) return segFileName;
	       
	        for (Track track : tracks) {
	            long currentSample = 0;
	            double currentTime = 0;
	            double lastTime = 0;
	            long startSample = 0;
	            long endSample = -1;
	            
	            for (int i = 0; i < track.getSampleDurations().length; i++) {
	                long delta = track.getSampleDurations()[i];
	                
	                if (currentTime > lastTime && currentTime <= startTime) {
	                    // current sample is still before the new start time
	                    startSample = currentSample;
	                }
	                if (currentTime > lastTime && currentTime <= endTime) {
	                    // current sample is after the new start time and still
	                    // before the new end time
	                    endSample = currentSample;
	                }
	              
	                lastTime = currentTime;
	                currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
	                currentSample++;
	            }
	            movie.addTrack(new CroppedTrack(track, startSample, endSample));
	            
	        }
	        long firstTime = System.currentTimeMillis();
	     
	        Container out = new DefaultMp4Builder().build(movie);
	    
	        long secondTime = System.currentTimeMillis();
	     
	        if (!dst.exists()) {
	        	dst.mkdir();
	        }
	     
	        String srcFileName = src.substring(src.lastIndexOf("/")+1);
	        segFileName = (srcFileName.substring(0, srcFileName.length()-4)) +"_3s"+index+".mp4";

	        
	        FileOutputStream fos = new FileOutputStream(dst.getAbsolutePath()
	        								+ File.separator
	        								+ segFileName);
	        
	        FileChannel fc = fos.getChannel();
	        out.writeContainer(fc);
	    
	        fc.close();
	        fos.close();
	
	     
	        long thirdTime = System.currentTimeMillis();
	        Log.e(TAG, "Building IsoFile took : " + (secondTime - firstTime) + "ms");
	        Log.e(TAG, "Writing IsoFile took : " + (thirdTime - secondTime) + "ms");
	        Log.e(TAG,
	                "Writing IsoFile speed : "
	                        + (new File(String.format("output-%f-%f.mp4",
	                                startTime, endTime)).length()
	                                / (thirdTime - secondTime) / 1000) + "MB/s");
	        
	        return segFileName;
	    }
	    
	    
	    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
	        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
	        long currentSample = 0;
	        double currentTime = 0;
	        for (int i = 0; i < track.getSampleDurations().length; i++) {
	            long delta = track.getSampleDurations()[i];
	            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
	                // samples always start with 1 but we start with zero therefore
	                // +1
	                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(),
	                        currentSample + 1)] = currentTime;
	            }
	            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
	            currentSample++;
	        }
	        double previous = 0;
	        for (double timeOfSyncSample : timeOfSyncSamples) {
	            if (timeOfSyncSample > cutHere) {
	                if (next) 
	                    return timeOfSyncSample;
	                else 
	                    return previous;
	            }
	            previous = timeOfSyncSample;
	        }
	        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
	    }
	    
}

