package nus.cs5248.group1.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.SharedPreferences;

public class SharedPreferencesUtils {

	public static void setStringArrayPref(SharedPreferences prefs, String key, ArrayList<String> values) {
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } 
        else {
            editor.putString(key, null);
        }
        SharedPreferencesCompat.apply(editor);
    }

    public static ArrayList<String> getStringArrayPref(SharedPreferences prefs, String key) {
        String json = prefs.getString(key, null);
        ArrayList<String> values = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String val = a.optString(i);
                    values.add(val);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return values;
    }
    
}