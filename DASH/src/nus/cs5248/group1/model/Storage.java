package nus.cs5248.group1.model;

import java.io.File;
import java.io.FilenameFilter;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

public class Storage {
	
	public static final String APP_NAME = "CS5248";
	public static final String TAG = "Storage";

	public static File getSegmentFolder(final File videoFile, final boolean createIfNotExist) {
		String filename = videoFile.getName();
		String title = filename.substring(0, filename.lastIndexOf('.'));
		
		return getSegmentFolder(title, createIfNotExist);
	}
	
	public static File getSegmentFolder(final String videoTitle, final boolean createIfNotExist) {
		File segmentsDir = new File(getMediaFolder(createIfNotExist), "segments");
    	File currentVideoDir = new File(segmentsDir, videoTitle);
    	
    	if (createIfNotExist) {
			if (!currentVideoDir.exists()) {
				if (!currentVideoDir.mkdirs()) {
					Log.d(TAG, "failed to create directory: " + currentVideoDir.getPath());
					return null;
				}
			}
    	}
		
		return currentVideoDir;
	}
	
	public static File getMediaFolder(final boolean createIfNotExist) {
		
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), APP_NAME);
		
		if (createIfNotExist) {
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					Log.d(TAG, "failed to create directory: " + mediaStorageDir.getPath());
					return null;
				}
			}
		}
		return mediaStorageDir;
	}
	
	public static File getFileForSegment(final File videoFile, final int segmentIndex) {
		String filename = videoFile.getName();
		String segmentFilename = filename.replace(".mp4", String.format("_%05d.mp4", segmentIndex));
		return new File(getSegmentFolder(videoFile, true), segmentFilename);
	}
	
	public static String[] getMP4FileList(final File folder) {
		FilenameFilter filter = new FilenameFilter() {
			@SuppressLint("DefaultLocale")
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".mp4");
			}
		};
		return folder.list(filter);
	}
	
	public static File getSelectedFile(final File videoFile) {
		String filename = videoFile.getName();
		return new File(getSegmentFolder(videoFile, true), filename);
	}
	
	public static boolean deleteFile(String strFile) {
		return new File(strFile).delete();
	}
	
	
	public static File getTempFolder(final boolean createIfNotExist, String name) {
		
		File tempStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), name);
		
		if (createIfNotExist) {
			if (!tempStorageDir.exists()) {
				if (!tempStorageDir.mkdirs()) {
					Log.d(TAG, "failed to create directory: " + tempStorageDir.getPath());
					return null;
				}
			}
		}
		return tempStorageDir;
	}
	
	public static boolean deleteDir(File dir) {

		if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    return dir.delete();
	}
	
}
